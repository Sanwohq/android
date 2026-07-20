package com.sanwohq.android

import android.app.Activity
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts

/**
 * Main entry point for the Sanwo payment SDK.
 *
 * Create an instance with a [SanwoProvider] and your public key, register event listeners,
 * then call the instance directly to present the payment UI.
 *
 * ```kotlin
 * val sanwo = Sanwo(
 *     provider = paystackProvider,  // from com.sanwohq.paystack
 *     publicKey = "pk_test_..."
 * )
 *
 * sanwo.on(SanwoEvent.SUCCESS) { event ->
 *     Log.d("Sanwo", "Payment succeeded: ${event.data}")
 * }
 *
 * sanwo(
 *     activity = this,
 *     options = CheckoutOptions(
 *         amount = 500000,
 *         currency = "NGN",
 *         customer = CheckoutCustomer(email = "user@example.com"),
 *     ),
 *     onResult = { result ->
 *         when (result) {
 *             is CheckoutResult.Successful -> { /* handle success */ }
 *             is CheckoutResult.Cancelled -> { /* handle cancel */ }
 *             is CheckoutResult.Failed -> { /* handle failure */ }
 *         }
 *     }
 * )
 * ```
 *
 * @param provider The payment provider to use.
 * @param publicKey Your provider public/API key.
 */
class Sanwo(
    private val provider: SanwoProvider,
    private val publicKey: String,
) {
    /**
     * Register a global event listener.
     *
     * @param event The event type to listen for.
     * @param listener Callback invoked when the event fires.
     * @return A function that removes this listener when called.
     */
    fun on(event: SanwoEvent, listener: (SanwoEventPayload) -> Unit): () -> Unit {
        return emitter.on(event, listener)
    }

    /**
     * Register an [ActivityResultLauncher] for the checkout flow.
     *
     * **Must be called during or before `onCreate`** of your [ComponentActivity].
     * This follows the AndroidX Activity Result API pattern.
     *
     * ```kotlin
     * class PaymentActivity : AppCompatActivity() {
     *     private val sanwo = Sanwo(paystackProvider, "pk_test_...")
     *     private lateinit var launcher: ActivityResultLauncher<Intent>
     *
     *     override fun onCreate(savedInstanceState: Bundle?) {
     *         super.onCreate(savedInstanceState)
     *         launcher = sanwo.registerForCheckoutResult(this) { result ->
     *             when (result) {
     *                 is CheckoutResult.Successful -> { /* ... */ }
     *                 is CheckoutResult.Cancelled -> { /* ... */ }
     *                 is CheckoutResult.Failed -> { /* ... */ }
     *             }
     *         }
     *     }
     * }
     * ```
     *
     * @param activity The [ComponentActivity] to register with.
     * @param onResult Callback receiving the [CheckoutResult] when the checkout finishes.
     * @return An [ActivityResultLauncher] to be used with [launchCheckout].
     */
    fun registerForCheckoutResult(
        activity: ComponentActivity,
        onResult: (CheckoutResult) -> Unit,
    ): ActivityResultLauncher<Intent> {
        return activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
        ) { activityResult ->
            val result = if (activityResult.resultCode == Activity.RESULT_OK) {
                SanwoCheckoutActivity.parseResult(activityResult.data)
            } else {
                CheckoutResult.Cancelled(provider = provider.name)
            }
            onResult(result)
        }
    }

    /**
     * Launch the checkout UI using a pre-registered [ActivityResultLauncher].
     *
     * Use this when you have registered a launcher via [registerForCheckoutResult].
     *
     * @param activity The current activity context.
     * @param launcher The launcher obtained from [registerForCheckoutResult].
     * @param options Checkout configuration.
     */
    fun launchCheckout(
        activity: Activity,
        launcher: ActivityResultLauncher<Intent>,
        options: CheckoutOptions,
    ) {
        val html = renderHtml(options)

        // Notify onLoad/onError via the event emitter.
        registerLifecycleCallbacks(options)

        val intent = Intent(activity, SanwoCheckoutActivity::class.java).apply {
            putExtra(SanwoCheckoutActivity.EXTRA_HTML, html)
            putExtra(SanwoCheckoutActivity.EXTRA_PROVIDER, provider.name)
            options.reference?.let { putExtra(SanwoCheckoutActivity.EXTRA_REFERENCE, it) }
        }

        launcher.launch(intent)
    }

    /**
     * Convenience method that registers a launcher, starts the checkout, and delivers the result
     * -- all in one call. Supports the callable pattern: `sanwo(activity, options) { result -> }`.
     *
     * **Note:** Because `registerForActivityResult` must be called before `STARTED`, this method
     * uses `startActivityForResult` under the hood (deprecated but functional). For lifecycle-safe
     * usage, prefer [registerForCheckoutResult] + [launchCheckout].
     *
     * @param activity The activity to launch from.
     * @param options Checkout configuration.
     * @param onResult Callback receiving the [CheckoutResult].
     */
    @Suppress("DEPRECATION")
    operator fun invoke(
        activity: Activity,
        options: CheckoutOptions,
        onResult: (CheckoutResult) -> Unit,
    ) {
        val html = renderHtml(options)

        registerLifecycleCallbacks(options)

        // Store the callback for retrieval when the activity returns.
        pendingCallbacks[REQUEST_CODE] = onResult

        val intent = Intent(activity, SanwoCheckoutActivity::class.java).apply {
            putExtra(SanwoCheckoutActivity.EXTRA_HTML, html)
            putExtra(SanwoCheckoutActivity.EXTRA_PROVIDER, provider.name)
            options.reference?.let { putExtra(SanwoCheckoutActivity.EXTRA_REFERENCE, it) }
        }

        activity.startActivityForResult(intent, REQUEST_CODE)
    }

    // -- Internal --

    private fun renderHtml(options: CheckoutOptions): String {
        val bridge = Engine.getBridge()
        val params = Engine.buildTemplateParams(options, publicKey, provider)
        return Engine.renderTemplate(provider.template, params, bridge)
    }

    private fun registerLifecycleCallbacks(options: CheckoutOptions) {
        options.onLoad?.let { onLoad ->
            var removeListener: (() -> Unit)? = null
            removeListener = emitter.on(SanwoEvent.LOADED) {
                onLoad()
                removeListener?.invoke()
            }
        }

        options.onError?.let { onError ->
            var removeListener: (() -> Unit)? = null
            removeListener = emitter.on(SanwoEvent.ERROR) { payload ->
                val message = payload.data["message"]?.toString() ?: "Unknown error"
                onError(RuntimeException(message))
                removeListener?.invoke()
            }
        }
    }

    companion object {
        /** Shared event emitter used by the checkout activity bridge. */
        internal val emitter = SanwoEventEmitter()

        private const val REQUEST_CODE = 0x53_41  // "SA" in hex

        /**
         * Call this from your Activity's `onActivityResult` if you used the callable
         * [invoke] pattern (which uses the deprecated `startActivityForResult`).
         *
         * ```kotlin
         * override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
         *     super.onActivityResult(requestCode, resultCode, data)
         *     Sanwo.handleActivityResult(requestCode, resultCode, data)
         * }
         * ```
         */
        @Suppress("DEPRECATION")
        fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            if (requestCode != REQUEST_CODE) return
            val callback = pendingCallbacks.remove(REQUEST_CODE) ?: return
            val result = if (resultCode == Activity.RESULT_OK) {
                SanwoCheckoutActivity.parseResult(data)
            } else {
                CheckoutResult.Cancelled(provider = "unknown")
            }
            callback(result)
        }

        private val pendingCallbacks = mutableMapOf<Int, (CheckoutResult) -> Unit>()
    }
}
