package com.sanwohq.android

/**
 * Customer details for a checkout session.
 *
 * @property email Customer email address (required by most providers).
 * @property firstName Customer first name.
 * @property lastName Customer last name.
 * @property phone Customer phone number.
 */
data class CheckoutCustomer(
    val email: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val phone: String? = null,
)

/**
 * Options for initiating a checkout session.
 *
 * @property amount Amount in the **smallest currency unit** (e.g. kobo, cents).
 *   The engine converts to the provider's expected unit automatically.
 * @property currency ISO 4217 currency code (e.g. "NGN", "USD", "GHS").
 * @property customer Customer details.
 * @property reference A unique transaction reference. If omitted, the provider may generate one.
 * @property channels Payment channels to enable (provider-specific, e.g. `["card", "bank"]`).
 * @property metadata Arbitrary metadata to attach to the transaction.
 * @property paymentOptions Flutterwave-specific payment options string.
 * @property subaccounts Flutterwave-specific sub-account configuration.
 * @property method Paystack-specific checkout method (defaults to "checkout").
 * @property extra Additional key-value pairs forwarded to the template params as-is.
 * @property onLoad Callback invoked when the payment UI has loaded.
 * @property onError Callback invoked if an error occurs before checkout completes.
 */
data class CheckoutOptions(
    val amount: Long,
    val currency: String,
    val customer: CheckoutCustomer,
    val reference: String? = null,
    val channels: List<String>? = null,
    val metadata: Map<String, Any?>? = null,
    val paymentOptions: String? = null,
    val subaccounts: List<Map<String, Any?>>? = null,
    val method: String? = null,
    val extra: Map<String, Any?>? = null,
    val onLoad: (() -> Unit)? = null,
    val onError: ((Throwable) -> Unit)? = null,
)
