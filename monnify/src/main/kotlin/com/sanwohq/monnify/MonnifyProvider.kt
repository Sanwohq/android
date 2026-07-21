package com.sanwohq.monnify

import com.sanwohq.android.SanwoProvider

/**
 * Monnify provider for Sanwo.
 *
 * Auto-generated from @sanwohq/monnify — do not edit manually.
 */
val monnifyProvider = SanwoProvider(
    id = "monnify",
    name = "monnify",
    displayName = "Monnify",
    template = MONNIFY_TEMPLATE,
    amountInMinorUnit = false,
    supportedCurrencies = listOf("NGN"),
    supportedCountries = listOf("NG")
)

private const val MONNIFY_TEMPLATE = """<!DOCTYPE html>
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
        var customerName = (params.firstName || '') + (params.lastName ? ' ' + params.lastName : '');
        if (!customerName.trim()) customerName = params.email;

        var config = {
          amount: params.amount,
          currency: params.currency || 'NGN',
          reference: params.reference,
          customerFullName: customerName,
          customerEmail: params.email,
          apiKey: params.publicKey,
          contractCode: params.contractCode,
          paymentDescription: params.description || 'Payment',
          isTestMode: params.isTestMode !== undefined ? params.isTestMode : (params.publicKey && params.publicKey.indexOf('TEST') !== -1),
          onLoadStart: function() {},
          onLoadComplete: function() {
            sanwoCallback('loaded', {});
          },
          onComplete: function(response) {
            sanwoCallback('success', {
              reference: response.paymentReference,
              transaction_id: response.transactionReference,
              transactionReference: response.transactionReference,
              paymentReference: response.paymentReference,
              amountPaid: response.amountPaid,
              paidOn: response.paidOn,
              paymentStatus: response.paymentStatus,
              raw: response
            });
          },
          onClose: function(data) {
            sanwoCallback('cancelled', data || {});
          }
        };

        if (params.metadata) config.metadata = params.metadata;
        if (params.redirectUrl) config.redirectUrl = params.redirectUrl;
        if (params.paymentMethods) config.paymentMethods = params.paymentMethods;
        if (params.incomeSplitConfig) config.incomeSplitConfig = params.incomeSplitConfig;

        MonnifySDK.initialize(config);
      } catch(e) {
        sanwoCallback('error', { message: e.message });
      }
    }

    var mfScript = document.createElement('script');
    mfScript.src = 'https://sdk.monnify.com/plugin/monnify.js';
    mfScript.onload = initPayment;
    mfScript.onerror = function() {
      sanwoCallback('error', { message: 'Failed to load Monnify SDK' });
    };
    document.body.appendChild(mfScript);
  </script>
</body>
</html>"""
