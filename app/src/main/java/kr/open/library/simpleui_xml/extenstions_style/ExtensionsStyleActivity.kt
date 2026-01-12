package kr.open.library.simpleui_xml.extenstions_style

import android.Manifest
import android.os.Bundle
import androidx.core.widget.addTextChangedListener
import kr.open.library.simple_ui.core.extensions.date.toDateString
import kr.open.library.simple_ui.core.extensions.display.dpToPx
import kr.open.library.simple_ui.core.extensions.display.pxToDp
import kr.open.library.simple_ui.core.extensions.display.spToPx
import kr.open.library.simple_ui.core.extensions.string.isEmailValid
import kr.open.library.simple_ui.core.extensions.string.isNumeric
import kr.open.library.simple_ui.core.extensions.string.removeWhitespace
import kr.open.library.simple_ui.core.extensions.trycatch.safeCatch
import kr.open.library.simple_ui.core.permissions.extentions.hasPermission
import kr.open.library.simple_ui.xml.extensions.resource.getColorCompat
import kr.open.library.simple_ui.xml.extensions.resource.getDrawableCompat
import kr.open.library.simple_ui.xml.extensions.view.SnackBarOption
import kr.open.library.simple_ui.xml.extensions.view.bold
import kr.open.library.simple_ui.xml.extensions.view.italic
import kr.open.library.simple_ui.xml.extensions.view.normal
import kr.open.library.simple_ui.xml.extensions.view.removeStrikeThrough
import kr.open.library.simple_ui.xml.extensions.view.removeUnderline
import kr.open.library.simple_ui.xml.extensions.view.snackBarShowShort
import kr.open.library.simple_ui.xml.extensions.view.strikeThrough
import kr.open.library.simple_ui.xml.extensions.view.toastShowLong
import kr.open.library.simple_ui.xml.extensions.view.toastShowShort
import kr.open.library.simple_ui.xml.extensions.view.underline
import kr.open.library.simple_ui.xml.ui.components.activity.binding.BaseDataBindingActivity
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.databinding.ActivityExtensionsStyleBinding
import java.util.Locale

class ExtensionsStyleActivity : BaseDataBindingActivity<ActivityExtensionsStyleBinding>(R.layout.activity_extensions_style) {
    override fun onViewCreate(binding: ActivityExtensionsStyleBinding, savedInstanceState: Bundle?) {
        super.onViewCreate(binding, savedInstanceState)
        setupViewExtensions()
        setupDisplayExtensions()
        setupResourceExtensions()
        setupStringExtensions()
        setupDateExtensions()
        setupTryCatchExtensions()
        setupPermissionExtensions()
    }

    /**
     * 섹션 1: View Extensions (Toast/SnackBar/TextView)
     */
    private fun setupViewExtensions() {
        // Toast
        getBinding().btnToastShort.setOnClickListener {
            toastShowShort("Toast Short 예제")
        }

        getBinding().btnToastLong.setOnClickListener {
            toastShowLong("Toast Long 예제 - 조금 더 길게 표시됩니다")
        }

        // SnackBar
        getBinding().btnSnackBarShort.setOnClickListener {
            getBinding().root.snackBarShowShort("SnackBar 예제입니다!")
        }

        getBinding().btnSnackBarAction.setOnClickListener {
            getBinding().root.snackBarShowShort(
                "액션 버튼이 있는 SnackBar",
                SnackBarOption(
                    actionText = "확인",
                    action = { toastShowShort("액션 클릭!") },
                ),
            )
        }

        // TextView 스타일링
        getBinding().btnBold.setOnClickListener {
            getBinding().tvSampleText.bold()
        }

        getBinding().btnItalic.setOnClickListener {
            getBinding().tvSampleText.italic()
        }

        getBinding().btnUnderline.setOnClickListener {
            getBinding().tvSampleText.underline()
        }

        getBinding().btnStrikeThrough.setOnClickListener {
            getBinding().tvSampleText.strikeThrough()
        }

        getBinding().btnResetStyle.setOnClickListener {
            getBinding().tvSampleText.normal()
            getBinding().tvSampleText.removeUnderline()
            getBinding().tvSampleText.removeStrikeThrough()
        }
    }

    /**
     * 섹션 2: Display Extensions (단위 변환)
     */
    private fun setupDisplayExtensions() {
        getBinding().btnDpToPx.setOnClickListener {
            val value =
                getBinding()
                    .edtDisplayValue.text
                    .toString()
                    .toFloatOrNull() ?: 0f
            val result = value.dpToPx(this)
            getBinding().tvDisplayResult.text = "결과: ${value}dp = ${result}px"
        }

        getBinding().btnPxToDp.setOnClickListener {
            val value =
                getBinding()
                    .edtDisplayValue.text
                    .toString()
                    .toFloatOrNull() ?: 0f
            val result = value.pxToDp(this)
            getBinding().tvDisplayResult.text = "결과: ${value}px = ${result}dp"
        }

        getBinding().btnSpToPx.setOnClickListener {
            val value =
                getBinding()
                    .edtDisplayValue.text
                    .toString()
                    .toFloatOrNull() ?: 0f
            val result = value.spToPx(this)
            getBinding().tvDisplayResult.text = "결과: ${value}sp = ${result}px"
        }
    }

    /**
     * 섹션 3: Resource Extensions
     */
    private fun setupResourceExtensions() {
        getBinding().btnGetDrawable.setOnClickListener {
            val drawable = getDrawableCompat(R.drawable.ic_launcher_foreground)
            if (drawable != null) {
                getBinding().tvResourceResult.text = "결과: Drawable 가져오기 성공 ✅"
            } else {
                getBinding().tvResourceResult.text = "결과: Drawable 가져오기 실패 ❌"
            }
        }

        getBinding().btnGetColor.setOnClickListener {
            val color = getColorCompat(android.R.color.holo_blue_dark)
            getBinding().tvResourceResult.text = "결과: Color = $color (0x${Integer.toHexString(color)})"
            getBinding().tvResourceResult.setTextColor(color)
        }
    }

    /**
     * 섹션 4: String Extensions (문자열 검증)
     */
    private fun setupStringExtensions() {
        // 이메일 실시간 검증
        getBinding().edtEmail.addTextChangedListener {
            val email = it.toString()
            if (email.isEmpty()) {
                getBinding().tvEmailResult.text = "이메일을 입력하세요"
            } else if (email.isEmailValid()) {
                getBinding().tvEmailResult.text = "✅ 유효한 이메일입니다"
                getBinding().tvEmailResult.setTextColor(getColorCompat(android.R.color.holo_green_dark))
            } else {
                getBinding().tvEmailResult.text = "❌ 유효하지 않은 이메일입니다"
                getBinding().tvEmailResult.setTextColor(getColorCompat(android.R.color.holo_red_dark))
            }
        }

        // 숫자 실시간 검증
        getBinding().edtNumber.addTextChangedListener {
            val number = it.toString()
            if (number.isEmpty()) {
                getBinding().tvNumberResult.text = "숫자를 입력하세요"
            } else if (number.isNumeric()) {
                getBinding().tvNumberResult.text = "✅ 숫자입니다"
                getBinding().tvNumberResult.setTextColor(getColorCompat(android.R.color.holo_green_dark))
            } else {
                getBinding().tvNumberResult.text = "❌ 숫자가 아닙니다"
                getBinding().tvNumberResult.setTextColor(getColorCompat(android.R.color.holo_red_dark))
            }
        }

        // 공백 제거
        getBinding().btnRemoveWhitespace.setOnClickListener {
            val original = getBinding().edtWhitespace.text.toString()
            val removed = original.removeWhitespace()
            getBinding().tvWhitespaceResult.text = "원본: \"$original\"\n결과: \"$removed\""
        }
    }

    /**
     * 섹션 5: Date Extensions
     */
    private fun setupDateExtensions() {
        getBinding().btnFormatDate.setOnClickListener {
            val currentTime = System.currentTimeMillis()
            val formatted1 = currentTime.toDateString("yyyy-MM-dd HH:mm:ss", Locale.KOREA)
            val formatted2 = currentTime.toDateString("yyyy년 MM월 dd일 HH시 mm분", Locale.KOREA)

            getBinding().tvDateResult.text =
                """
                결과:
                 - $formatted1
                 - $formatted2
                """.trimIndent()
        }
    }

    /**
     * 섹션 6: TryCatch Extensions
     */
    private fun setupTryCatchExtensions() {
        getBinding().btnSafeCatch.setOnClickListener {
            // 에러 발생 가능한 코드
            val result = safeCatch(defaultValue = "에러 발생!") {
                val text = getBinding().edtDisplayValue.text.toString()
                require(text.isNotEmpty()) { "text isEmpty" }
                "성공: $text"
            }

            getBinding().tvSafeCatchResult.text = "결과: $result"
        }
    }

    /**
     * 섹션 7: Permission Extensions
     */
    private fun setupPermissionExtensions() {
        getBinding().btnCheckPermission.setOnClickListener {
            val hasCamera = hasPermission(Manifest.permission.CAMERA)

            if (hasCamera) {
                getBinding().tvPermissionResult.text = "결과: CAMERA 권한 있음 ✅"
                getBinding().tvPermissionResult.setTextColor(getColorCompat(android.R.color.holo_green_dark))
            } else {
                getBinding().tvPermissionResult.text = "결과: CAMERA 권한 없음 ❌\n(권한을 요청하지 않았거나 거부됨)"
                getBinding().tvPermissionResult.setTextColor(getColorCompat(android.R.color.holo_red_dark))
            }
        }
    }
}
