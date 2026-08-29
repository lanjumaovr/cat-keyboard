package com.lanjumaovr.catkeyboard

import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Switch
import android.content.Intent

class CatInputMethodService : InputMethodService() {

    private var mode = "realtime"

    override fun onCreate() {
        super.onCreate()
        mode = SettingsStore.getMode(this)
    }

    override fun onCreateInputView(): View {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        root.addView(buildToolbar())
        root.addView(buildKeyboard())
        return root
    }

    private fun buildToolbar(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(0xFFF5F5F5.toInt())

            val modeSwitch = Switch(this@CatInputMethodService).apply {
                text = "实时替换"
                isChecked = mode == "realtime"
                setOnCheckedChangeListener { _, checked ->
                    mode = if (checked) "realtime" else "button"
                    text = if (checked) "实时替换" else "按钮替换"
                    SettingsStore.setMode(this@CatInputMethodService, mode)
                }
            }
            addView(modeSwitch)

            val replaceBtn = Button(this@CatInputMethodService).apply {
                text = "🐾 替换"
                setOnClickListener { doReplaceNow() }
            }
            addView(replaceBtn)

            val settingsBtn = Button(this@CatInputMethodService).apply {
                text = "⚙"
                setOnClickListener {
                    val intent = Intent(this@CatInputMethodService, SettingsActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
            }
            addView(settingsBtn)
        }
    }

    private fun buildKeyboard(): LinearLayout {
        val kb = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val rows = listOf(
            listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
            listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
            listOf("z", "x", "c", "v", "b", "n", "m")
        )

        for (row in rows) {
            val line = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
            }
            for (key in row) {
                line.addView(makeKey(key))
            }
            kb.addView(line)
        }

        val bottom = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        bottom.addView(makeSpecialKey("空格", " "))
        bottom.addView(makeSpecialKey("⌫", null))
        bottom.addView(makeSpecialKey("回车", "\n"))
        kb.addView(bottom)

        return kb
    }

    private fun makeKey(label: String): Button {
        return Button(this).apply {
            text = label
            setOnClickListener { commitText(label) }
        }
    }

    private fun makeSpecialKey(label: String, special: String?): Button {
        return Button(this).apply {
            text = label
            setOnClickListener {
                when (special) {
                    null -> sendBackspace()
                    " " -> commitText(" ")
                    "\n" -> sendEnter()
                    else -> commitText(special)
                }
            }
        }
    }

    private fun commitText(text: String) {
        val ic = currentInputConnection ?: return
        val final = if (mode == "realtime") applyTransform(text) else text
        ic.commitText(final, 1)
    }

    private fun sendBackspace() {
        val ic = currentInputConnection ?: return
        ic.deleteSurroundingText(1, 0)
    }

    private fun sendEnter() {
        val ic = currentInputConnection ?: return
        ic.commitText("\n", 1)
    }

    private fun applyTransform(text: String): String {
        val ruleSet = SettingsStore.getRuleSet(this)
        val scriptEnabled = SettingsStore.isScriptEnabled(this)
        return TransformEngine.transform(ruleSet, text, scriptEnabled)
    }

    private fun doReplaceNow() {
        val ic = currentInputConnection ?: return
        val before = ic.getTextBeforeCursor(1000, 0) ?: return
        val after = ic.getTextAfterCursor(1000, 0) ?: ""
        val full = before.toString() + after.toString()
        val transformed = applyTransform(full)
        ic.deleteSurroundingText(before.length, after.length)
        ic.commitText(transformed, 1)
    }

    override fun onKey(keyCode: Int, event: KeyEvent?): Boolean {
        if (event?.action == KeyEvent.ACTION_DOWN && keyCode >= KeyEvent.KEYCODE_A && keyCode <= KeyEvent.KEYCODE_Z) {
            val ch = event.getUnicodeChar(event.metaState).toChar()
            if (ch.isLetterOrDigit()) {
                commitText(ch.toString())
                return true
            }
        }
        return super.onKey(keyCode, event)
    }
}