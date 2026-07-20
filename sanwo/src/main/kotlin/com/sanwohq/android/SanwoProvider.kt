package com.sanwohq.android

/**
 * Defines a payment provider that Sanwo can use.
 *
 * Each provider supplies an HTML template containing `{{sanwoBridge}}` and `{{params}}`
 * placeholders. The engine replaces these at runtime before loading the template in a WebView.
 *
 * @property name A unique identifier for the provider (e.g. "paystack", "flutterwave").
 * @property template The HTML template string with `{{sanwoBridge}}` and `{{params}}` placeholders.
 * @property amountInMinorUnit Whether the provider expects amounts in minor units (e.g. kobo, cents).
 *   When `false`, the engine will convert from minor units to major units before passing to the template.
 */
data class SanwoProvider(
    val name: String,
    val template: String,
    val amountInMinorUnit: Boolean = true,
)
