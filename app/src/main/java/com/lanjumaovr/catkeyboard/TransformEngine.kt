package com.lanjumaovr.catkeyboard

import org.mozilla.javascript.Context
import org.mozilla.javascript.ContextFactory
import org.mozilla.javascript.Function

object TransformEngine {

    private const val SCRIPT_TIMEOUT_MS = 200L

    fun transform(ruleSet: RuleSet, input: String, applyScript: Boolean = true): String {
        var result = input
        for (rule in ruleSet.rules) {
            result = applyRule(rule, result)
        }
        if (applyScript && ruleSet.script.isNotBlank()) {
            result = runScript(ruleSet.script, result)
        }
        return result
    }

    private fun applyRule(rule: ReplaceRule, input: String): String {
        return when (rule.type) {
            "replace" -> input.replace(rule.match, rule.replace)
            "replace_regex" -> {
                try {
                    Regex(rule.match).replace(input, rule.replace)
                } catch (e: Exception) {
                    input
                }
            }
            "append" -> {
                if (rule.trigger == "punct") appendAfterPunct(input, rule.text)
                else input + rule.text
            }
            else -> input
        }
    }

    private fun appendAfterPunct(input: String, text: String): String {
        if (input.isEmpty()) return input
        val sb = StringBuilder()
        val punctSet = "。，！？.,!?~～;；:："
        for (c in input) {
            sb.append(c)
            if (c in punctSet) sb.append(text)
        }
        return sb.toString()
    }

    private fun runScript(script: String, input: String): String {
        val factory = SandboxContextFactory()
        return try {
            val cx = factory.enterContext()
            try {
                cx.optimizationLevel = -1
                val scope = cx.initStandardObjects()
                cx.evaluateString(scope, script, "subscription", 1, null)
                val fn = scope.get("transform", scope)
                if (fn is Function) {
                    val out = fn.call(cx, scope, scope, arrayOf<Any>(input))
                    Context.toString(out)
                } else {
                    input
                }
            } finally {
                Context.exit()
            }
        } catch (e: Exception) {
            input
        }
    }

    private class SandboxContextFactory : ContextFactory() {
        private val startTime = System.nanoTime()

        override fun observeInstructionCount(cx: Context, instructionCount: Int) {
            val elapsedMs = (System.nanoTime() - startTime) / 1_000_000
            if (elapsedMs > SCRIPT_TIMEOUT_MS) {
                throw RuntimeException("script timeout")
            }
        }

        override fun makeContext(): Context {
            val cx = super.makeContext()
            cx.instructionObserverThreshold = 10_000
            return cx
        }
    }
}