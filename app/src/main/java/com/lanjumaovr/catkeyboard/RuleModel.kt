package com.lanjumaovr.catkeyboard

import org.json.JSONArray
import org.json.JSONObject

data class ReplaceRule(
    val type: String,
    val match: String = "",
    val replace: String = "",
    val text: String = "",
    val trigger: String = "always"
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("type", type)
        if (match.isNotEmpty()) put("match", match)
        if (replace.isNotEmpty()) put("replace", replace)
        if (text.isNotEmpty()) put("text", text)
        put("trigger", trigger)
    }

    companion object {
        fun fromJson(obj: JSONObject): ReplaceRule = ReplaceRule(
            type = obj.optString("type", "replace"),
            match = obj.optString("match", ""),
            replace = obj.optString("replace", ""),
            text = obj.optString("text", ""),
            trigger = obj.optString("trigger", "always")
        )
    }
}

data class RuleSet(
    val name: String = "未命名规则",
    val version: String = "1.0.0",
    val author: String = "",
    val description: String = "",
    val modeDefault: String = "realtime",
    val rules: List<ReplaceRule> = emptyList(),
    val script: String = ""
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("name", name)
        put("version", version)
        put("author", author)
        put("description", description)
        put("mode_default", modeDefault)
        val arr = JSONArray()
        rules.forEach { arr.put(it.toJson()) }
        put("rules", arr)
        if (script.isNotEmpty()) put("script", script)
    }

    companion object {
        fun fromJson(obj: JSONObject): RuleSet {
            val rulesArr = obj.optJSONArray("rules") ?: JSONArray()
            val list = mutableListOf<ReplaceRule>()
            for (i in 0 until rulesArr.length()) {
                list.add(ReplaceRule.fromJson(rulesArr.getJSONObject(i)))
            }
            return RuleSet(
                name = obj.optString("name", "未命名规则"),
                version = obj.optString("version", "1.0.0"),
                author = obj.optString("author", ""),
                description = obj.optString("description", ""),
                modeDefault = obj.optString("mode_default", "realtime"),
                rules = list,
                script = obj.optString("script", "")
            )
        }
    }
}

object DefaultRules {
    fun get(): RuleSet = RuleSet(
        name = "猫猫默认规则",
        description = "把「我」变「本喵」，「你」变「主人」",
        modeDefault = "realtime",
        rules = listOf(
            ReplaceRule("replace", match = "你", replace = "主人"),
            ReplaceRule("replace", match = "我", replace = "本喵"),
            ReplaceRule("append", text = "喵", trigger = "punct"),
            ReplaceRule("append", text = "🐾", trigger = "punct")
        )
    )
}