// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.android.library) apply false

    // FireBase (Add the dependency for the Google services Gradle plugin)
    id("com.google.gms.google-services") version "4.4.4" apply false //Firebase Common
    id("com.google.firebase.appdistribution") version "5.0.0" apply false // Firebase App Distribution(Apk 테스트 자동 베포)
    id("com.google.firebase.crashlytics") version "3.0.6" apply false // Firebase Exception Report
    id("org.jetbrains.dokka") version "2.1.0" // Dokka 멀티 모듈 설정 추가
}

// Dokka V2 Multi-Module Configuration
// Dokka V2 멀티 모듈 통합 문서 설정
subprojects {
    apply(plugin = "org.jetbrains.dokka")
}

tasks.register("dokkaHtmlMultiModuleCustom") {
    group = "documentation"
    description = "Generate multi-module Dokka HTML documentation to docs/api"

    dependsOn(":simple_core:dokkaGeneratePublicationHtml")
    dependsOn(":simple_xml:dokkaGeneratePublicationHtml")

    doLast {
        val outputDir = file("docs/api")
        outputDir.deleteRecursively()
        outputDir.mkdirs()

        // Copy simple_core documentation
        copy {
            from("simple_core/build/dokka/html")
            into("docs/api/simple-core")
        }

        // Copy simple_xml documentation
        copy {
            from("simple_xml/build/dokka/html")
            into("docs/api/simple-xml")
        }

        // Create index.html for navigation
        file("docs/api/index.html").writeText("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Simple UI Library - API Documentation</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 40px; background: #f5f5f5; }
                    .container { max-width: 800px; margin: 0 auto; background: white; padding: 40px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
                    h1 { color: #333; border-bottom: 2px solid #4CAF50; padding-bottom: 10px; }
                    .module { margin: 20px 0; padding: 20px; border: 1px solid #ddd; border-radius: 4px; background: #fafafa; }
                    .module h2 { margin-top: 0; color: #4CAF50; }
                    .module p { color: #666; line-height: 1.6; }
                    .module a { display: inline-block; margin-top: 10px; padding: 10px 20px; background: #4CAF50; color: white; text-decoration: none; border-radius: 4px; }
                    .module a:hover { background: #45a049; }
                </style>
            </head>
            <body>
                <div class="container">
                    <h1>Simple UI Library - API Documentation</h1>
                    <p>Welcome to the Simple UI Library documentation. This library provides Android XML UI development tools.</p>

                    <div class="module">
                        <h2>Simple UI Core</h2>
                        <p>Core module with no UI dependencies. Provides fundamental features including:</p>
                        <ul>
                            <li>Extensions (Bundle, Date, String, etc.)</li>
                            <li>Logging system (Logx)</li>
                            <li>System managers (Battery, Location, Network, etc.)</li>
                            <li>Base ViewModel</li>
                        </ul>
                        <a href="simple-core/index.html">View Core Documentation →</a>
                    </div>

                    <div class="module">
                        <h2>Simple UI XML</h2>
                        <p>XML UI module (requires Simple UI Core). Provides:</p>
                        <ul>
                            <li>Base Activity & Fragment classes</li>
                            <li>View extensions (TextView, EditText, ImageView, etc.)</li>
                            <li>Permission management</li>
                            <li>System UI controllers (StatusBar, NavigationBar, etc.)</li>
                        </ul>
                        <a href="simple-xml/index.html">View XML Documentation →</a>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent())

        println("✅ Multi-module documentation generated at: docs/api/index.html")
    }
}