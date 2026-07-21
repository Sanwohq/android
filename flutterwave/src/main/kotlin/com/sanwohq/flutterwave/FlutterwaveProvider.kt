package com.sanwohq.flutterwave

import com.sanwohq.android.SanwoProvider

/**
 * Flutterwave provider for Sanwo.
 *
 * Auto-generated from @sanwohq/flutterwave — do not edit manually.
 */
val flutterwaveProvider = SanwoProvider(
    id = "flutterwave",
    name = "flutterwave",
    displayName = "Flutterwave",
    template = FLUTTERWAVE_TEMPLATE,
    amountInMinorUnit = false,
    supportedCurrencies = listOf("NGN", "GHS", "KES", "ZAR", "USD", "EUR", "GBP", "TZS", "UGX", "RWF", "XAF", "XOF"),
    supportedCountries = listOf("NG", "GH", "KE", "ZA", "US", "GB", "TZ", "UG", "RW", "CM", "CI")
)

private const val FLUTTERWAVE_TEMPLATE = """<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Sanwo Checkout</title>
</head>
<body onload="initPayment()" style="background-color:#fff;height:100vh">
  <script src="https://checkout.flutterwave.com/v3.js"></script>
  <script>
    {{sanwoBridge}}

    var params = {{params}};

    function initPayment() {
      try {
        var config = {
          public_key: params.publicKey,
          tx_ref: params.reference,
          amount: params.amount,
          currency: params.currency,
          customer: {
            email: params.email
          },
          callback: function(response) {
            var isSuccess = response.status === 'successful' || response.status === 'completed';
            if (isSuccess) {
              sanwoCallback('success', {
                reference: response.tx_ref,
                transaction_id: response.transaction_id,
                flw_ref: response.flw_ref,
                raw: response
              });
            } else {
              sanwoCallback('error', {
                message: 'Flutterwave checkout returned status: ' + response.status,
                raw: response
              });
            }
            if (typeof FlutterwaveCheckout !== 'undefined') {
              try { FlutterwaveCheckout.close(); } catch(e) {}
            }
          },
          onclose: function() {
            sanwoCallback('cancelled', {});
          }
        };

        if (params.name || params.firstName) {
          config.customer.name = params.name || [params.firstName, params.lastName].filter(Boolean).join(' ');
        }
        if (params.phone) config.customer.phonenumber = params.phone;
        if (params.metadata) config.meta = params.metadata;
        if (params.description) config.title = params.description;
        if (params.paymentOptions) config.payment_options = params.paymentOptions;
        if (params.redirectUrl) config.redirect_url = params.redirectUrl;
        if (params.paymentPlan) config.payment_plan = params.paymentPlan;
        if (params.subaccounts) config.subaccounts = params.subaccounts;
        if (params.customizations) config.customizations = params.customizations;

        sanwoCallback('loaded', {});
        FlutterwaveCheckout(config);
      } catch(e) {
        sanwoCallback('error', { message: e.message });
      }
    }
  </script>
</body>
</html>"""
