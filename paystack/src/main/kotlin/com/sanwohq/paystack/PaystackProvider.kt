package com.sanwohq.paystack

import com.sanwohq.android.SanwoProvider

/**
 * Paystack provider for Sanwo.
 *
 * Auto-generated from @sanwohq/paystack — do not edit manually.
 */
val paystackProvider = SanwoProvider(
    id = "paystack",
    name = "paystack",
    displayName = "Paystack",
    template = PAYSTACK_TEMPLATE,
    amountInMinorUnit = true,
    supportedCurrencies = listOf("NGN", "GHS", "ZAR", "USD", "KES"),
    supportedCountries = listOf("NG", "GH", "ZA", "US", "KE")
)

private const val PAYSTACK_TEMPLATE = """<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Sanwo Checkout</title>
</head>
<body onload="initPayment()" style="background-color:#fff;height:100vh">
  <script src="https://js.paystack.co/v2/inline.js"></script>
  <script>
    {{sanwoBridge}}

    var params = {{params}};

    function initPayment() {
      try {
        var paystack = new PaystackPop();
        var config = {
          key: params.publicKey,
          email: params.email,
          amount: params.amount,
          currency: params.currency,
          onSuccess: function(response) {
            sanwoCallback('success', {
              reference: response.reference || response.trxref,
              transaction_id: response.trans || response.transaction,
              message: response.message,
              raw: response
            });
          },
          onCancel: function() {
            sanwoCallback('cancelled', {});
          },
          onClose: function() {
            sanwoCallback('closed', {});
          }
        };

        if (params.reference) config.ref = params.reference;
        if (params.channels) config.channels = params.channels;
        if (params.metadata) config.metadata = params.metadata;
        if (params.firstName) config.firstname = params.firstName;
        if (params.lastName) config.lastname = params.lastName;
        if (params.phone) config.phone = params.phone;
        if (params.label) config.label = params.label;
        if (params.plan) config.plan = params.plan;
        if (params.quantity) config.quantity = params.quantity;
        if (params.subaccount) config.subaccount = params.subaccount;
        if (params.splitCode) config.split_code = params.splitCode;
        if (params.split) config.split = params.split;
        if (params.transactionCharge) config.transaction_charge = params.transactionCharge;
        if (params.invoiceLimit) config.invoice_limit = params.invoiceLimit;

        sanwoCallback('loaded', {});
        var method = params.method || 'checkout';
        paystack[method](config);
      } catch(e) {
        sanwoCallback('error', { message: e.message });
      }
    }
  </script>
</body>
</html>"""
