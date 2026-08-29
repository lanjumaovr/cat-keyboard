package com.lanjumaovr.catkeyboard

import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import kotlin.concurrent.thread

class SettingsActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildUi()
    }

    private fun buildUi() {
        val scroll = ScrollView(this)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
        }

        root.addView(TextView(this).apply {
            text = "猫猫输入法 · 设置"
            textSize = 20f
        })

        val info = TextView(this).apply {
            text = refreshInfo()
            textSize = 14f
        }
        root.addView(info)

        root.addView(TextView(this).apply { text = "\n订阅链接" })
        val urlInput = EditText(this).apply {
            hint = "https://example.com/rules.json"
            setText(SettingsStore.getSubscriptionUrl(this@SettingsActivity))
        }
        root.addView(urlInput)

        root.addView(Button(this).apply {
            text = "下载并应用订阅"
            setOnClickListener {
                val url = urlInput.text.toString().trim()
                if (url.isEmpty()) {
                    toast("请输入订阅链接")
                    return@setOnClickListener
                }
                thread {
                    val ok = SubscriptionManager.downloadAndSave(this@SettingsActivity, url)
                    runOnUiThread {
                        if (ok) {
                            toast("订阅成功")
                            info.text = refreshInfo()
                        } else {
                            toast("下载失败，请检查链接")
                        }
                    }
                }
            }
        })

        root.addView(Button(this).apply {
            text = "从剪贴板导入 JSON"
            setOnClickListener {
                val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = cm.primaryClip
                val text = clip?.getItemAt(0)?.text?.toString()
                if (text.isNullOrBlank()) {
                    toast("剪贴板为空")
                    return@setOnClickListener
                }
                val rs = SubscriptionManager.parse(text)
                if (rs != null) {
                    SettingsStore.setRuleSet(this@SettingsActivity, rs)
                    toast("导入成功：${rs.name}")
                    info.text = refreshInfo()
                } else {
                    toast("JSON 格式错误")
                }
            }
        })

        root.addView(Button(this).apply {
            text = "恢复默认规则"
            setOnClickListener {
                SettingsStore.setRuleSet(this@SettingsActivity, DefaultRules.get())
                info.text = refreshInfo()
                toast("已恢复默认规则")
            }
        })

        val scriptSwitch = Switch(this).apply {
            text = "启用脚本"
            isChecked = SettingsStore.isScriptEnabled(this@SettingsActivity)
            setOnCheckedChangeListener { _, checked ->
                SettingsStore.setScriptEnabled(this@SettingsActivity, checked)
            }
        }
        root.addView(scriptSwitch)

        root.addView(TextView(this).apply {
            text = "\n提示：\n· 在系统「语言与输入法」中启用猫猫输入法\n" +
                    "· 打开系统「输入法切换」选择猫猫输入法\n" +
                    "· 订阅格式见 GitHub 仓库 README"
            textSize = 12f
        })

        scroll.addView(root)
        setContentView(scroll)
    }

    private fun refreshInfo(): String {
        val rs = SettingsStore.getRuleSet(this)
        return "当前规则：${rs.name} (v${rs.version})\n" +
                "规则数：${rs.rules.size}　脚本：${if (rs.script.isBlank()) "无" else "有"}"
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}