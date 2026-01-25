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
    override fun onCreate(binding: ActivityExtensionsStyleBinding, savedInstanceState: Bundle?) {
        super.onCreate(binding, savedInstanceState)
        setupViewExtensions()
        setupDisplayExtensions()
        setupResourceExtensions()
        setupStringExtensions()
        setupDateExtensions()
        setupTryCatchExtensions()
        setupPermissionExtensions()
    }

    /**
     * ?뱀뀡 1: View Extensions (Toast/SnackBar/TextView)
     */
    private fun setupViewExtensions() {
        // Toast
        getBinding().btnToastShort.setOnClickListener {
            toastShowShort("Toast Short ?덉젣")
        }

        getBinding().btnToastLong.setOnClickListener {
            toastShowLong("Toast Long ?덉젣 - 議곌툑 ??湲멸쾶 ?쒖떆?⑸땲??)
        }

        // SnackBar
        getBinding().btnSnackBarShort.setOnClickListener {
            getBinding().root.snackBarShowShort("SnackBar ?덉젣?낅땲??")
        }

        getBinding().btnSnackBarAction.setOnClickListener {
            getBinding().root.snackBarShowShort(
                "?≪뀡 踰꾪듉???덈뒗 SnackBar",
                SnackBarOption(
                    actionText = "?뺤씤",
                    action = { toastShowShort("?≪뀡 ?대┃!") },
                ),
            )
        }

        // TextView ?ㅽ??쇰쭅
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
     * ?뱀뀡 2: Display Extensions (?⑥쐞 蹂??
     */
    private fun setupDisplayExtensions() {
        getBinding().btnDpToPx.setOnClickListener {
            val value =
                getBinding()
                    .edtDisplayValue.text
                    .toString()
                    .toFloatOrNull() ?: 0f
            val result = value.dpToPx(this)
            getBinding().tvDisplayResult.text = "寃곌낵: ${value}dp = ${result}px"
        }

        getBinding().btnPxToDp.setOnClickListener {
            val value =
                getBinding()
                    .edtDisplayValue.text
                    .toString()
                    .toFloatOrNull() ?: 0f
            val result = value.pxToDp(this)
            getBinding().tvDisplayResult.text = "寃곌낵: ${value}px = ${result}dp"
        }

        getBinding().btnSpToPx.setOnClickListener {
            val value =
                getBinding()
                    .edtDisplayValue.text
                    .toString()
                    .toFloatOrNull() ?: 0f
            val result = value.spToPx(this)
            getBinding().tvDisplayResult.text = "寃곌낵: ${value}sp = ${result}px"
        }
    }

    /**
     * ?뱀뀡 3: Resource Extensions
     */
    private fun setupResourceExtensions() {
        getBinding().btnGetDrawable.setOnClickListener {
            val drawable = getDrawableCompat(R.drawable.ic_launcher_foreground)
            if (drawable != null) {
                getBinding().tvResourceResult.text = "寃곌낵: Drawable 媛?몄삤湲??깃났 ??
            } else {
                getBinding().tvResourceResult.text = "寃곌낵: Drawable 媛?몄삤湲??ㅽ뙣 ??
            }
        }

        getBinding().btnGetColor.setOnClickListener {
            val color = getColorCompat(android.R.color.holo_blue_dark)
            getBinding().tvResourceResult.text = "寃곌낵: Color = $color (0x${Integer.toHexString(color)})"
            getBinding().tvResourceResult.setTextColor(color)
        }
    }

    /**
     * ?뱀뀡 4: String Extensions (臾몄옄??寃利?
     */
    private fun setupStringExtensions() {
        // ?대찓???ㅼ떆媛?寃利?
        getBinding().edtEmail.addTextChangedListener {
            val email = it.toString()
            if (email.isEmpty()) {
                getBinding().tvEmailResult.text = "?대찓?쇱쓣 ?낅젰?섏꽭??
            } else if (email.isEmailValid()) {
                getBinding().tvEmailResult.text = "???좏슚???대찓?쇱엯?덈떎"
                getBinding().tvEmailResult.setTextColor(getColorCompat(android.R.color.holo_green_dark))
            } else {
                getBinding().tvEmailResult.text = "???좏슚?섏? ?딆? ?대찓?쇱엯?덈떎"
                getBinding().tvEmailResult.setTextColor(getColorCompat(android.R.color.holo_red_dark))
            }
        }

        // ?レ옄 ?ㅼ떆媛?寃利?
        getBinding().edtNumber.addTextChangedListener {
            val number = it.toString()
            if (number.isEmpty()) {
                getBinding().tvNumberResult.text = "?レ옄瑜??낅젰?섏꽭??
            } else if (number.isNumeric()) {
                getBinding().tvNumberResult.text = "???レ옄?낅땲??
                getBinding().tvNumberResult.setTextColor(getColorCompat(android.R.color.holo_green_dark))
            } else {
                getBinding().tvNumberResult.text = "???レ옄媛 ?꾨떃?덈떎"
                getBinding().tvNumberResult.setTextColor(getColorCompat(android.R.color.holo_red_dark))
            }
        }

        // 怨듬갚 ?쒓굅
        getBinding().btnRemoveWhitespace.setOnClickListener {
            val original = getBinding().edtWhitespace.text.toString()
            val removed = original.removeWhitespace()
            getBinding().tvWhitespaceResult.text = "?먮낯: \"$original\"\n寃곌낵: \"$removed\""
        }
    }

    /**
     * ?뱀뀡 5: Date Extensions
     */
    private fun setupDateExtensions() {
        getBinding().btnFormatDate.setOnClickListener {
            val currentTime = System.currentTimeMillis()
            val formatted1 = currentTime.toDateString("yyyy-MM-dd HH:mm:ss", Locale.KOREA)
            val formatted2 = currentTime.toDateString("yyyy??MM??dd??HH??mm遺?, Locale.KOREA)

            getBinding().tvDateResult.text =
                """
                寃곌낵:
                 - $formatted1
                 - $formatted2
                """.trimIndent()
        }
    }

    /**
     * ?뱀뀡 6: TryCatch Extensions
     */
    private fun setupTryCatchExtensions() {
        getBinding().btnSafeCatch.setOnClickListener {
            // ?먮윭 諛쒖깮 媛?ν븳 肄붾뱶
            val result = safeCatch(defaultValue = "?먮윭 諛쒖깮!") {
                val text = getBinding().edtDisplayValue.text.toString()
                require(text.isNotEmpty()) { "text isEmpty" }
                "?깃났: $text"
            }

            getBinding().tvSafeCatchResult.text = "寃곌낵: $result"
        }
    }

    /**
     * ?뱀뀡 7: Permission Extensions
     */
    private fun setupPermissionExtensions() {
        getBinding().btnCheckPermission.setOnClickListener {
            val hasCamera = hasPermission(Manifest.permission.CAMERA)

            if (hasCamera) {
                getBinding().tvPermissionResult.text = "寃곌낵: CAMERA 沅뚰븳 ?덉쓬 ??
                getBinding().tvPermissionResult.setTextColor(getColorCompat(android.R.color.holo_green_dark))
            } else {
                getBinding().tvPermissionResult.text = "寃곌낵: CAMERA 沅뚰븳 ?놁쓬 ??n(沅뚰븳???붿껌?섏? ?딆븯嫄곕굹 嫄곕???"
                getBinding().tvPermissionResult.setTextColor(getColorCompat(android.R.color.holo_red_dark))
            }
        }
    }
}
