package com.sanwohq.android

import org.json.JSONArray
import org.json.JSONObject

/**
 * Template rendering engine.
 *
 * Replaces `{{sanwoBridge}}` and `{{params}}` placeholders in provider HTML templates
 * and converts amounts between minor and major currency units.
 */
internal object Engine {

    /** Currencies with zero decimal places. */
    private val ZERO_DECIMAL_CURRENCIES = setOf(
        "JPY", "KRW", "VND", "CLP", "PYG", "ISK", "UGX", "RWF",
    )

    /** Currencies with three decimal places. */
    private val THREE_DECIMAL_CURRENCIES = setOf(
        "BHD", "KWD", "OMR", "TND", "JOD",
    )

    /**
     * Returns the JavaScript bridge function for Android WebView communication.
     *
     * The bridge calls `JSBridge.showMessageInNative(...)`, where `JSBridge` is the
     * name registered via `@JavascriptInterface` on the native side.
     */
    fun getBridge(): String = """
        function sanwoCallback(event, data) {
          JSBridge.showMessageInNative(JSON.stringify({ type: 'sanwo', event: event, data: data }));
        }
    """.trimIndent()

    /**
     * Converts an amount from the smallest currency unit to the major unit.
     *
     * For example, 500000 kobo (NGN) becomes 5000.0 naira.
     *
     * @param amount The amount in minor units.
     * @param currency ISO 4217 currency code.
     * @return The amount in major units.
     */
    fun fromMinorUnit(amount: Long, currency: String): Double {
        val upper = currency.uppercase()
        return when {
            ZERO_DECIMAL_CURRENCIES.contains(upper) -> amount.toDouble()
            THREE_DECIMAL_CURRENCIES.contains(upper) -> amount.toDouble() / 1000.0
            else -> amount.toDouble() / 100.0
        }
    }

    /**
     * Builds the JSON parameters object that will replace `{{params}}` in the template.
     */
    fun buildTemplateParams(
        options: CheckoutOptions,
        publicKey: String,
        provider: SanwoProvider,
    ): JSONObject {
        val params = JSONObject()

        params.put("publicKey", publicKey)
        params.put("email", options.customer.email)
        params.put("currency", options.currency)

        // Amount: convert from minor units if the provider does not expect minor units.
        if (provider.amountInMinorUnit) {
            params.put("amount", options.amount)
        } else {
            params.put("amount", fromMinorUnit(options.amount, options.currency))
        }

        options.reference?.let { params.put("reference", it) }
        options.customer.firstName?.let { params.put("firstName", it) }
        options.customer.lastName?.let { params.put("lastName", it) }
        options.customer.phone?.let { params.put("phone", it) }
        options.method?.let { params.put("method", it) }
        options.paymentOptions?.let { params.put("paymentOptions", it) }

        options.channels?.let { channels ->
            val arr = JSONArray()
            channels.forEach { arr.put(it) }
            params.put("channels", arr)
        }

        options.metadata?.let { meta ->
            params.put("metadata", mapToJson(meta))
        }

        options.subaccounts?.let { subs ->
            val arr = JSONArray()
            subs.forEach { arr.put(mapToJson(it)) }
            params.put("subaccounts", arr)
        }

        options.extra?.let { extra ->
            for ((key, value) in extra) {
                params.put(key, wrapValue(value))
            }
        }

        return params
    }

    /**
     * Renders a provider template by replacing placeholders with the bridge and params.
     *
     * @param template The raw HTML template containing `{{sanwoBridge}}` and `{{params}}`.
     * @param params The JSON parameters to inject.
     * @param bridge The JavaScript bridge code to inject.
     * @return Fully rendered HTML ready to load in a WebView.
     */
    fun renderTemplate(template: String, params: JSONObject, bridge: String): String {
        return template
            .replace("{{sanwoBridge}}", bridge)
            .replace("{{params}}", params.toString())
    }

    // -- Helpers --

    private fun mapToJson(map: Map<String, Any?>): JSONObject {
        val obj = JSONObject()
        for ((key, value) in map) {
            obj.put(key, wrapValue(value))
        }
        return obj
    }

    @Suppress("UNCHECKED_CAST")
    private fun wrapValue(value: Any?): Any? {
        return when (value) {
            null -> JSONObject.NULL
            is Map<*, *> -> mapToJson(value as Map<String, Any?>)
            is List<*> -> {
                val arr = JSONArray()
                value.forEach { arr.put(wrapValue(it)) }
                arr
            }
            is Array<*> -> {
                val arr = JSONArray()
                value.forEach { arr.put(wrapValue(it)) }
                arr
            }
            else -> value
        }
    }
}
