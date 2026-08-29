package com.lanjumaovr.catkeyboard

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONObject

object SettingsStore {

    private const val PREFS = "cat_keyboard_prefs"
    private const val KEY_MODE = "mode"
    private const val KEY_RULESET = "ruleset_json"
    private const val KEY_SUBSCRIPTION_URL = "sub_url"
    private const val KEY_SCRIPT_ENABLED = "script_enabled"

    private fun prefs(ctx: Context): SharedPreferences =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun getMode(ctx: Context): String =
        prefs(ctx).getString(KEY_MODE, "realtime") ?: "realtime"

    fun setMode(ctx: Context, mode: String) {
        prefs(ctx).edit().putString(KEY_MODE, mode).apply()
    }

    fun getRuleSet(ctx: Context): RuleSet {
        val json = prefs(ctx).getString(KEY_RULESET, null)
        return if (json != null) {
            try {
                RuleSet.fromJson(JSONObject(json))
            } catch (e: Exception) {
                DefaultRules.get()
            }
        } else {
            DefaultRules.get()
        }
    }

    fun setRuleSet(ctx: Context, ruleSet: RuleSet) {
        prefs(ctx).edit().putString(KEY_RULESET, ruleSet.toJson().toString()).apply()
    }

    fun getSubscriptionUrl(ctx: Context): String =
        prefs(ctx).getString(KEY_SUBSCRIPTION_URL, "") ?: ""

    fun setSubscriptionUrl(ctx: Context, url: String) {
        prefs(ctx).edit().putString(KEY_SUBSCRIPTION_URL, url).apply()
    }

    fun isScriptEnabled(ctx: Context): Boolean =
        prefs(ctx).getBoolean(KEY_SCRIPT_ENABLED, true)

    fun setScriptEnabled(ctx: Context, enabled: Boolean) {
        prefs(ctx).edit().putBoolean(KEY_SCRIPT_ENABLED, enabled).apply()
    }
}