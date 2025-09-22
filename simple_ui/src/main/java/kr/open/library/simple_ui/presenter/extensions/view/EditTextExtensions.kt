package kr.open.library.simple_ui.presenter.extensions.view

import android.widget.EditText

public fun EditText.getTextToString(): String = this.text.toString()

public fun EditText.isTextEmpty(): Boolean = this.getTextToString().isEmpty()

public fun EditText.textToInt(): Int? = this.text.toString().toIntOrNull()

public fun EditText.textToFloat(): Float? = this.text.toString().toFloatOrNull()

public fun EditText.textToDouble(): Double? = this.text.toString().toDoubleOrNull()