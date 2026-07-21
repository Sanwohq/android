package com.sanwohq.interswitch

import com.sanwohq.android.SanwoProvider

/**
 * Interswitch provider for Sanwo.
 *
 * Auto-generated from @sanwohq/interswitch — do not edit manually.
 */
val interswitchProvider = SanwoProvider(
    id = "interswitch",
    name = "interswitch",
    displayName = "Interswitch",
    template = INTERSWITCH_TEMPLATE,
    amountInMinorUnit = true,
    supportedCurrencies = listOf("NGN"),
    supportedCountries = listOf("NG")
)

private const val INTERSWITCH_TEMPLATE = """<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Sanwo Checkout</title>
</head>
<body style="background-color:#fff;height:100vh">
  <script>
    {{sanwoBridge}}

    var params = {{params}};

    function initPayment() {
      try {
        var config = {
          merchant_code: params.publicKey,
          amount: params.amount,
          currency: params.currency,
          customer_email: params.email,
          txn_ref: params.reference,
          onComplete: function(response) {
            sanwoCallback('success', {
              reference: response.txnref,
              ...response
            });
          },
          onClose: function() {
            sanwoCallback('cancelled', {});
          }
        };

        if (params.firstName) config.customer_first_name = params.firstName;
        if (params.lastName) config.customer_last_name = params.lastName;
        if (params.payItemId) config.pay_item_id = params.payItemId;
        if (params.payItemName) config.pay_item_name = params.payItemName;
        if (params.siteRedirectUrl) config.site_redirect_url = params.siteRedirectUrl;

        sanwoCallback('loaded', {});
        window.webpayCheckout(config);
      } catch(e) {
        sanwoCallback('error', { message: e.message });
      }
    }

    var iwScript = document.createElement('script');
    iwScript.src = 'https://newwebpay.interswitchng.com/inline-checkout.js';
    iwScript.onload = initPayment;
    iwScript.onerror = function() {
      sanwoCallback('error', { message: 'Failed to load Interswitch SDK' });
    };
    document.body.appendChild(iwScript);
  </script>
</body>
</html>"""
