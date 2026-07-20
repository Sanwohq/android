package com.sanwohq.android

/**
 * Defines a payment provider that Sanwo can use.
 *
 * Each provider supplies an HTML template containing `{{sanwoBridge}}` and `{{params}}`
 * placeholders. The engine replaces these at runtime before loading the template in a WebView.
 *
 * @property id A machine-readable identifier for the provider (e.g. "paystack", "flutterwave").
 * @property name A short name for the provider (e.g. "paystack").
 * @property displayName A human-readable display name (e.g. "Paystack").
 * @property template The HTML template string with `{{sanwoBridge}}` and `{{params}}` placeholders.
 * @property amountInMinorUnit Whether the provider expects amounts in minor units (e.g. kobo, cents).
 *   When `false`, the engine will convert from minor units to major units before passing to the template.
 * @property supportedCurrencies ISO 4217 currency codes this provider supports.
 * @property supportedCountries ISO 3166-1 alpha-2 country codes this provider supports.
 */
data class SanwoProvider(
    val id: String,
    val name: String,
    val displayName: String,
    val template: String,
    val amountInMinorUnit: Boolean = true,
    val supportedCurrencies: List<String> = emptyList(),
    val supportedCountries: List<String> = emptyList(),
)
