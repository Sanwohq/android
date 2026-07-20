package com.sanwohq.android

import com.sanwohq.android.templates.FlutterwaveTemplate
import com.sanwohq.android.templates.PaystackTemplate

/**
 * Built-in payment providers shipped with the Sanwo SDK.
 */
object SanwoProviders {

    /**
     * Paystack payment provider.
     *
     * Paystack expects amounts in the smallest currency unit (e.g. kobo for NGN, pesewas for GHS).
     */
    val paystack: SanwoProvider = SanwoProvider(
        name = "paystack",
        template = PaystackTemplate.html,
        amountInMinorUnit = true,
    )

    /**
     * Flutterwave payment provider.
     *
     * Flutterwave expects amounts in the major currency unit (e.g. naira for NGN),
     * so the engine converts from minor units automatically.
     */
    val flutterwave: SanwoProvider = SanwoProvider(
        name = "flutterwave",
        template = FlutterwaveTemplate.html,
        amountInMinorUnit = false,
    )
}
