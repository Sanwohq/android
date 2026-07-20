package com.sanwohq.android

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject

/**
 * Full-screen activity that hosts a WebView for the payment checkout flow.
 *
 * This activity is launched internally by [Sanwo.checkout] and should not be started directly.
 * It receives the rendered HTML via intent extras, loads it in a WebView, listens for
 * JavaScript bridge messages, and returns a [CheckoutResult] to the caller.
 */
class SanwoCheckoutActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val html = intent.getStringExtra(EXTRA_HTML)
        val providerName = intent.getStringExtra(EXTRA_PROVIDER) ?: "unknown"
        val reference = intent.getStringExtra(EXTRA_REFERENCE)

        if (html.isNullOrEmpty()) {
            finishWithError(providerName, reference, "No HTML template provided")
            return
        }

        webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.javaScriptCanOpenWindowsAutomatically = true
            settings.setSupportMultipleWindows(false)

            webViewClient = SanwoWebViewClient()
            webChromeClient = WebChromeClient()

            addJavascriptInterface(
                SanwoJSBridge(this@SanwoCheckoutActivity, providerName, reference),
                "JSBridge",
            )
        }

        setContentView(webView)
        webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
    }

    override fun onBackPressed() {
        val providerName = intent.getStringExtra(EXTRA_PROVIDER) ?: "unknown"
        val reference = intent.getStringExtra(EXTRA_REFERENCE)
        finishWithCancelled(providerName, reference)
    }

    override fun onDestroy() {
        if (::webView.isInitialized) {
            webView.removeJavascriptInterface("JSBridge")
            webView.destroy()
        }
        super.onDestroy()
    }

    // -- Bridge --

    /**
     * JavaScript interface exposed to the WebView as `JSBridge`.
     */
    internal class SanwoJSBridge(
        private val activity: SanwoCheckoutActivity,
        private val providerName: String,
        private val reference: String?,
    ) {
        @JavascriptInterface
        fun showMessageInNative(message: String) {
            try {
                val json = JSONObject(message)
                val type = json.optString("type")
                if (type != "sanwo") return

                val event = json.optString("event")
                val data = json.optJSONObject("data")
                val dataMap = jsonObjectToMap(data)

                // Emit to global listeners.
                SanwoEvent.fromValue(event)?.let { sanwoEvent ->
                    Sanwo.emitter.emit(sanwoEvent, dataMap)
                }

                // Handle terminal events.
                when (event) {
                    "success" -> {
                        val ref = dataMap["reference"]?.toString() ?: reference ?: ""
                        val txId = dataMap["transaction_id"]?.toString()
                        activity.finishWithSuccess(providerName, ref, txId, dataMap)
                    }
                    "cancelled", "closed" -> {
                        activity.finishWithCancelled(providerName, reference)
                    }
                    "error" -> {
                        val errorMsg = dataMap["message"]?.toString() ?: "Unknown error"
                        activity.finishWithError(providerName, reference, errorMsg)
                    }
                    // "loaded" is informational only; no activity finish.
                }
            } catch (e: Exception) {
                activity.finishWithError(providerName, reference, e.message ?: "Bridge parse error")
            }
        }
    }

    // -- Result helpers --

    private fun finishWithSuccess(
        provider: String,
        reference: String,
        transactionId: String?,
        raw: Map<String, Any?>,
    ) {
        val intent = Intent().apply {
            putExtra(RESULT_TYPE, RESULT_SUCCESS)
            putExtra(RESULT_PROVIDER, provider)
            putExtra(RESULT_REFERENCE, reference)
            transactionId?.let { putExtra(RESULT_TRANSACTION_ID, it) }
            putExtra(RESULT_RAW, JSONObject(raw).toString())
        }
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun finishWithCancelled(provider: String, reference: String?) {
        val intent = Intent().apply {
            putExtra(RESULT_TYPE, RESULT_CANCELLED)
            putExtra(RESULT_PROVIDER, provider)
            reference?.let { putExtra(RESULT_REFERENCE, it) }
        }
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun finishWithError(provider: String, reference: String?, error: String) {
        val intent = Intent().apply {
            putExtra(RESULT_TYPE, RESULT_FAILED)
            putExtra(RESULT_PROVIDER, provider)
            reference?.let { putExtra(RESULT_REFERENCE, it) }
            putExtra(RESULT_ERROR, error)
        }
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    // -- WebViewClient --

    private class SanwoWebViewClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            val url = request?.url ?: return false
            // Allow payment gateway URLs to load inside the WebView.
            // Open unknown external URLs in the browser.
            val host = url.host ?: return false
            val allowedHosts = listOf(
                "js.paystack.co",
                "checkout.paystack.com",
                "standard.paystack.co",
                "api.paystack.co",
                "checkout.flutterwave.com",
                "api.flutterwave.com",
                "flutterwave.com",
            )
            return if (allowedHosts.any { host.endsWith(it) }) {
                false // let WebView handle it
            } else {
                view?.context?.startActivity(Intent(Intent.ACTION_VIEW, url))
                true
            }
        }
    }

    companion object {
        internal const val EXTRA_HTML = "com.sanwohq.android.EXTRA_HTML"
        internal const val EXTRA_PROVIDER = "com.sanwohq.android.EXTRA_PROVIDER"
        internal const val EXTRA_REFERENCE = "com.sanwohq.android.EXTRA_REFERENCE"

        internal const val RESULT_TYPE = "com.sanwohq.android.RESULT_TYPE"
        internal const val RESULT_PROVIDER = "com.sanwohq.android.RESULT_PROVIDER"
        internal const val RESULT_REFERENCE = "com.sanwohq.android.RESULT_REFERENCE"
        internal const val RESULT_TRANSACTION_ID = "com.sanwohq.android.RESULT_TRANSACTION_ID"
        internal const val RESULT_RAW = "com.sanwohq.android.RESULT_RAW"
        internal const val RESULT_ERROR = "com.sanwohq.android.RESULT_ERROR"

        internal const val RESULT_SUCCESS = "success"
        internal const val RESULT_CANCELLED = "cancelled"
        internal const val RESULT_FAILED = "failed"

        /**
         * Parse an activity result [Intent] into a [CheckoutResult].
         */
        internal fun parseResult(data: Intent?): CheckoutResult {
            if (data == null) {
                return CheckoutResult.Cancelled(provider = "unknown")
            }

            val provider = data.getStringExtra(RESULT_PROVIDER) ?: "unknown"
            val reference = data.getStringExtra(RESULT_REFERENCE)

            return when (data.getStringExtra(RESULT_TYPE)) {
                RESULT_SUCCESS -> {
                    val rawJson = data.getStringExtra(RESULT_RAW)
                    val raw = if (rawJson != null) {
                        jsonObjectToMap(JSONObject(rawJson))
                    } else {
                        emptyMap()
                    }
                    CheckoutResult.Successful(
                        provider = provider,
                        reference = reference ?: "",
                        transactionId = data.getStringExtra(RESULT_TRANSACTION_ID),
                        raw = raw,
                    )
                }
                RESULT_FAILED -> CheckoutResult.Failed(
                    provider = provider,
                    reference = reference,
                    error = data.getStringExtra(RESULT_ERROR) ?: "Unknown error",
                )
                else -> CheckoutResult.Cancelled(
                    provider = provider,
                    reference = reference,
                )
            }
        }
    }
}

/**
 * Recursively converts a [JSONObject] to a [Map].
 */
internal fun jsonObjectToMap(json: JSONObject?): Map<String, Any?> {
    if (json == null) return emptyMap()
    val map = mutableMapOf<String, Any?>()
    val keys = json.keys()
    while (keys.hasNext()) {
        val key = keys.next()
        val value = json.opt(key)
        map[key] = when (value) {
            is JSONObject -> jsonObjectToMap(value)
            is org.json.JSONArray -> {
                (0 until value.length()).map { i ->
                    when (val item = value.opt(i)) {
                        is JSONObject -> jsonObjectToMap(item)
                        JSONObject.NULL -> null
                        else -> item
                    }
                }
            }
            JSONObject.NULL -> null
            else -> value
        }
    }
    return map
}
