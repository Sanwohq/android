package com.sanwohq.android

/**
 * Outcome of a completed checkout session.
 */
sealed class CheckoutResult {

    /**
     * The payment succeeded.
     *
     * @property provider Name of the provider that processed the payment.
     * @property reference Provider-assigned transaction reference.
     * @property transactionId Provider-assigned transaction ID, if available.
     * @property raw The full, unprocessed response data from the provider.
     */
    data class Successful(
        val provider: String,
        val reference: String,
        val transactionId: String? = null,
        val raw: Map<String, Any?> = emptyMap(),
    ) : CheckoutResult()

    /**
     * The user cancelled the payment.
     *
     * @property provider Name of the provider.
     * @property reference Transaction reference, if one was assigned before cancellation.
     */
    data class Cancelled(
        val provider: String,
        val reference: String? = null,
    ) : CheckoutResult()

    /**
     * The payment failed due to an error.
     *
     * @property provider Name of the provider.
     * @property reference Transaction reference, if one was assigned before the failure.
     * @property error A human-readable error message.
     */
    data class Failed(
        val provider: String,
        val reference: String? = null,
        val error: String,
    ) : CheckoutResult()
}
