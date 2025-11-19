package kr.open.library.simpleui_xml.extenstions_style

import android.Manifest
import android.os.Bundle
import androidx.core.widget.addTextChangedListener
import kr.open.library.simple_ui.extensions.date.toDateString
import kr.open.library.simple_ui.extensions.string.isEmailValid
import kr.open.library.simple_ui.extensions.string.isNumeric
import kr.open.library.simple_ui.extensions.string.removeWhitespace
import kr.open.library.simple_ui.extensions.trycatch.safeCatch
import kr.open.library.simple_ui.permissions.extentions.hasPermission
import kr.open.library.simple_ui.presenter.extensions.display.dpToPx
import kr.open.library.simple_ui.presenter.extensions.display.pxToDp
import kr.open.library.simple_ui.presenter.extensions.display.spToPx
import kr.open.library.simple_ui.presenter.extensions.resource.getColorCompat
import kr.open.library.simple_ui.presenter.extensions.resource.getDrawableCompat
import kr.open.library.simple_ui.presenter.extensions.view.*
import kr.open.library.simple_ui.presenter.ui.activity.BaseBindingActivity
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.databinding.ActivityExtensionsStyleBinding
import java.util.Locale

class ExtensionsStyleActivity : BaseBindingActivity<ActivityExtensionsStyleBinding>(R.layout.activity_extensions_style) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
        binding.btnToastShort.setOnClickListener {
            toastShowShort("Toast Short 예제")
        }

        binding.btnToastLong.setOnClickListener {
            toastShowLong("Toast Long 예제 - 조금 더 길게 표시됩니다")
        }

        // SnackBar
        binding.btnSnackBarShort.setOnClickListener {
            binding.root.snackBarShowShort("SnackBar 예제입니다!")
        }

        binding.btnSnackBarAction.setOnClickListener {
            binding.root.snackBarShowShort(
                "액션 버튼이 있는 SnackBar",
                SnackBarOption(
                    actionText = "확인",
                    action = { toastShowShort("액션 클릭!") }
                )
            )
        }

        // TextView 스타일링
        binding.btnBold.setOnClickListener {
            binding.tvSampleText.bold()
        }

        binding.btnItalic.setOnClickListener {
            binding.tvSampleText.italic()
        }

        binding.btnUnderline.setOnClickListener {
            binding.tvSampleText.underline()
        }

        binding.btnStrikeThrough.setOnClickListener {
            binding.tvSampleText.strikeThrough()
        }

        binding.btnResetStyle.setOnClickListener {
            binding.tvSampleText.normal()
            binding.tvSampleText.removeUnderline()
            binding.tvSampleText.removeStrikeThrough()
        }
    }

    /**
     * 섹션 2: Display Extensions (단위 변환)
     */
    private fun setupDisplayExtensions() {
        binding.btnDpToPx.setOnClickListener {
            val value = binding.edtDisplayValue.text.toString().toFloatOrNull() ?: 0f
            val result = value.dpToPx(this)
            binding.tvDisplayResult.text = "결과: ${value}dp = ${result}px"
        }

        binding.btnPxToDp.setOnClickListener {
            val value = binding.edtDisplayValue.text.toString().toFloatOrNull() ?: 0f
            val result = value.pxToDp(this)
            binding.tvDisplayResult.text = "결과: ${value}px = ${result}dp"
        }

        binding.btnSpToPx.setOnClickListener {
            val value = binding.edtDisplayValue.text.toString().toFloatOrNull() ?: 0f
            val result = value.spToPx(this)
            binding.tvDisplayResult.text = "결과: ${value}sp = ${result}px"
        }
    }

    /**
     * 섹션 3: Resource Extensions
     */
    private fun setupResourceExtensions() {
        binding.btnGetDrawable.setOnClickListener {
            val drawable = getDrawableCompat(R.drawable.ic_launcher_foreground)
            if (drawable != null) {
                binding.tvResourceResult.text = "결과: Drawable 가져오기 성공 ✅"
            } else {
                binding.tvResourceResult.text = "결과: Drawable 가져오기 실패 ❌"
            }
        }

        binding.btnGetColor.setOnClickListener {
            val color = getColorCompat(android.R.color.holo_blue_dark)
            binding.tvResourceResult.text = "결과: Color = $color (0x${Integer.toHexString(color)})"
            binding.tvResourceResult.setTextColor(color)
        }
    }

    /**
     * 섹션 4: String Extensions (문자열 검증)
     */
    private fun setupStringExtensions() {
        // 이메일 실시간 검증
        binding.edtEmail.addTextChangedListener {
            val email = it.toString()
            if (email.isEmpty()) {
                binding.tvEmailResult.text = "이메일을 입력하세요"
            } else if (email.isEmailValid()) {
                binding.tvEmailResult.text = "✅ 유효한 이메일입니다"
                binding.tvEmailResult.setTextColor(getColorCompat(android.R.color.holo_green_dark))
            } else {
                binding.tvEmailResult.text = "❌ 유효하지 않은 이메일입니다"
                binding.tvEmailResult.setTextColor(getColorCompat(android.R.color.holo_red_dark))
            }
        }

        // 숫자 실시간 검증
        binding.edtNumber.addTextChangedListener {
            val number = it.toString()
            if (number.isEmpty()) {
                binding.tvNumberResult.text = "숫자를 입력하세요"
            } else if (number.isNumeric()) {
                binding.tvNumberResult.text = "✅ 숫자입니다"
                binding.tvNumberResult.setTextColor(getColorCompat(android.R.color.holo_green_dark))
            } else {
                binding.tvNumberResult.text = "❌ 숫자가 아닙니다"
                binding.tvNumberResult.setTextColor(getColorCompat(android.R.color.holo_red_dark))
            }
        }

        // 공백 제거
        binding.btnRemoveWhitespace.setOnClickListener {
            val original = binding.edtWhitespace.text.toString()
            val removed = original.removeWhitespace()
            binding.tvWhitespaceResult.text = "원본: \"$original\"\n결과: \"$removed\""
        }
    }

    /**
     * 섹션 5: Date Extensions
     */
    private fun setupDateExtensions() {
        binding.btnFormatDate.setOnClickListener {
            val currentTime = System.currentTimeMillis()
            val formatted1 = currentTime.toDateString("yyyy-MM-dd HH:mm:ss", Locale.KOREA)
            val formatted2 = currentTime.toDateString("yyyy년 MM월 dd일 HH시 mm분", Locale.KOREA)

            binding.tvDateResult.text = """
               결과:
                - ${formatted1}
                - ${formatted2}
            """.trimIndent()
        }
    }

    /**
     * 섹션 6: TryCatch Extensions
     */
    private fun setupTryCatchExtensions() {
        binding.btnSafeCatch.setOnClickListener {
            // 에러 발생 가능한 코드
            val result = safeCatch(defaultValue = "에러 발생!") {
                val text = binding.edtDisplayValue.text.toString()
                if (text.isEmpty()) throw IllegalArgumentException("값이 비어있습니다")
                "성공: $text"
            }

            binding.tvSafeCatchResult.text = "결과: $result"
        }
    }

    /**
     * 섹션 7: Permission Extensions
     */
    private fun setupPermissionExtensions() {
        binding.btnCheckPermission.setOnClickListener {
            val hasCamera = hasPermission(Manifest.permission.CAMERA)

            if (hasCamera) {
                binding.tvPermissionResult.text = "결과: CAMERA 권한 있음 ✅"
                binding.tvPermissionResult.setTextColor(getColorCompat(android.R.color.holo_green_dark))
            } else {
                binding.tvPermissionResult.text = "결과: CAMERA 권한 없음 ❌\n(권한을 요청하지 않았거나 거부됨)"
                binding.tvPermissionResult.setTextColor(getColorCompat(android.R.color.holo_red_dark))
            }
        }
    }
}