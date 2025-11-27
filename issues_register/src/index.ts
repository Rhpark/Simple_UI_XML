import { onNewFatalIssuePublished } from "firebase-functions/v2/alerts/crashlytics";
import { onRequest } from "firebase-functions/v2/https";
import { defineSecret } from "firebase-functions/params";

// Secret Manager에서 GitHub 토큰 가져오기
const githubToken = defineSecret("GITHUB_PAT_SIMPLE_UI_XML");

// GitHub Repository 정보
const GITHUB_OWNER = "Rhpark";
const GITHUB_REPO = "Simple_UI_XML";

// API Key for verification build
const VERIFICATION_API_KEY = "SIMPLE_UI_VER_2025_nR8kL4mX9pT2wQ7vK3sN";

export const createGithubIssueOnCrash = onNewFatalIssuePublished(
  { secrets: [githubToken] },
  async (event) => {
    const issue = event.data.payload.issue;

    // Issue 제목: Exception 첫 줄
    const title = issue.title;

    // Issue 내용: 모든 Crashlytics 정보 포함
    const body = `
## Crash Information

**Issue ID:** ${issue.id}
**Title:** ${issue.title}
**Subtitle:** ${issue.subtitle}

## App Information

**App ID:** ${event.appId}
**App Version:** ${issue.appVersion}

## Device Information

**OS Version:** Android (from Crashlytics)

---
*This issue was automatically created by Firebase Crashlytics*
    `.trim();

    // GitHub API 호출
    const response = await fetch(
      `https://api.github.com/repos/${GITHUB_OWNER}/${GITHUB_REPO}/issues`,
      {
        method: "POST",
        headers: {
          "Authorization": `token ${githubToken.value()}`,
          "Accept": "application/vnd.github.v3+json",
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          title,
          body,
          labels: ["CD-Release"],
        }),
      }
    );

    if (!response.ok) {
      const error = await response.text();
      console.error("Failed to create GitHub issue:", error);
      throw new Error(`GitHub API error: ${response.status}`);
    }

    const result = await response.json();
    console.log("GitHub issue created:", result.html_url);
  }
);

// Verification 빌드용 HTTP 트리거
export const reportTestCrash = onRequest(
  { secrets: [githubToken] },
  async (request, response) => {
    // API Key 검증
    const apiKey = request.headers["x-api-key"];
    if (apiKey !== VERIFICATION_API_KEY) {
      response.status(401).send("Unauthorized");
      return;
    }

    // POST 요청만 허용
    if (request.method !== "POST") {
      response.status(405).send("Method Not Allowed");
      return;
    }

    // 앱에서 전송한 크래시 정보
    const crashData = request.body;

    // Issue 제목: Exception Type + Message
    const title = `${crashData.exceptionType}: ${crashData.message}`;

    // Issue 내용
    const body = `
## Crash Information (Verification Build)

**Exception Type:** ${crashData.exceptionType}
**Message:** ${crashData.message}

## Stack Trace
\`\`\`
${crashData.stackTrace}
\`\`\`

## Device Information

**OS Version:** ${crashData.osVersion}
**Device Model:** ${crashData.deviceModel}
**App Version:** ${crashData.appVersion}

## Additional Info

**Timestamp:** ${crashData.timestamp}

---
*This issue was automatically reported from Verification build*
    `.trim();

    // GitHub API 호출
    try {
      // 1. 기존 열린 이슈 검색 (CD-Test-Exception 라벨)
      const searchResponse = await fetch(
        `https://api.github.com/repos/${GITHUB_OWNER}/${GITHUB_REPO}/issues?state=open&labels=CD-Test-Exception`,
        {
          headers: {
            "Authorization": `token ${githubToken.value()}`,
            "Accept": "application/vnd.github.v3+json",
          },
        }
      );

      if (!searchResponse.ok) {
        throw new Error(`Failed to search issues: ${searchResponse.status}`);
      }

      const existingIssues = await searchResponse.json();

      // 2. 제목과 Stack Trace가 일치하는 이슈 찾기
      // Stack Trace 비교: 10줄 이하면 전체, 10줄 이상이면 첫 10줄만 비교
      let matchingIssue = null;
      for (const issue of existingIssues) {
        if (issue.title === title) {
          // Issue body에서 Stack Trace 추출
          const bodyMatch = issue.body?.match(/## Stack Trace\s*```\s*([\s\S]*?)\s*```/);
          if (bodyMatch) {
            const existingStackTrace = bodyMatch[1].trim();
            const newStackTrace = crashData.stackTrace.trim();

            // Stack Trace를 줄 단위로 분리
            const existingLines = existingStackTrace.split("\n");
            const newLines = newStackTrace.split("\n");

            // 비교할 줄 수 결정 (10줄 이하면 전체, 10줄 이상이면 첫 10줄)
            const compareLineCount = Math.min(10, Math.min(existingLines.length, newLines.length));

            // 지정된 줄 수만큼 비교
            let isMatch = true;
            for (let i = 0; i < compareLineCount; i++) {
              if (existingLines[i].trim() !== newLines[i].trim()) {
                isMatch = false;
                break;
              }
            }

            if (isMatch) {
              matchingIssue = issue;
              break;
            }
          }
        }
      }

      // 3. 일치하는 이슈가 있으면 댓글 추가, 없으면 새 이슈 생성
      if (matchingIssue) {
        // 기존 이슈에 댓글 추가
        const commentBody = `
## New Occurrence

**Timestamp:** ${crashData.timestamp}
**OS Version:** ${crashData.osVersion}
**Device Model:** ${crashData.deviceModel}
**App Version:** ${crashData.appVersion}

---
*Same crash occurred again*
        `.trim();

        const commentResponse = await fetch(
          `https://api.github.com/repos/${GITHUB_OWNER}/${GITHUB_REPO}/issues/${matchingIssue.number}/comments`,
          {
            method: "POST",
            headers: {
              "Authorization": `token ${githubToken.value()}`,
              "Accept": "application/vnd.github.v3+json",
              "Content-Type": "application/json",
            },
            body: JSON.stringify({ body: commentBody }),
          }
        );

        if (!commentResponse.ok) {
          const error = await commentResponse.text();
          console.error("Failed to add comment:", error);
          response.status(500).send("Failed to add comment");
          return;
        }

        console.log("Comment added to existing issue:", matchingIssue.html_url);
        response.status(200).send({ issueUrl: matchingIssue.html_url, action: "comment_added" });
      } else {
        // 새 이슈 생성
        const createResponse = await fetch(
          `https://api.github.com/repos/${GITHUB_OWNER}/${GITHUB_REPO}/issues`,
          {
            method: "POST",
            headers: {
              "Authorization": `token ${githubToken.value()}`,
              "Accept": "application/vnd.github.v3+json",
              "Content-Type": "application/json",
            },
            body: JSON.stringify({
              title,
              body,
              labels: ["CD-Test-Exception"],
            }),
          }
        );

        if (!createResponse.ok) {
          const error = await createResponse.text();
          console.error("Failed to create GitHub issue:", error);
          response.status(500).send("Failed to create issue");
          return;
        }

        const result = await createResponse.json();
        console.log("GitHub issue created:", result.html_url);
        response.status(200).send({ issueUrl: result.html_url, action: "issue_created" });
      }
    } catch (error) {
      console.error("Error:", error);
      response.status(500).send("Internal error");
    }
  }
);
