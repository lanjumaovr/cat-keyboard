package com.lanjumaovr.catkeyboard

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object SubscriptionManager {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private const val MAX_SIZE = 512 * 1024

    fun fetch(url: String): RuleSet? {
        if (url.isBlank()) return null
        return try {
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val body = response.body ?: return null
                val text = body.string()
                if (text.length > MAX_SIZE) return null
                RuleSet.fromJson(JSONObject(text))
            }
        } catch (e: Exception) {
            null
        }
    }

    fun parse(jsonText: String): RuleSet? {
        return try {
            RuleSet.fromJson(JSONObject(jsonText))
        } catch (e: Exception) {
            null
        }
    }

    fun downloadAndSave(ctx: Context, url: String): Boolean {
        val ruleSet = fetch(url) ?: return false
        SettingsStore.setRuleSet(ctx, ruleSet)
        SettingsStore.setSubscriptionUrl(ctx, url)
        return true
    }
}