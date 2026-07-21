package com.sanwohq.razorpay

import com.sanwohq.android.SanwoProvider

/**
 * Razorpay provider for Sanwo.
 *
 * Auto-generated from @sanwohq/razorpay — do not edit manually.
 */
val razorpayProvider = SanwoProvider(
    id = "razorpay",
    name = "razorpay",
    displayName = "Razorpay",
    template = RAZORPAY_TEMPLATE,
    amountInMinorUnit = true,
    supportedCurrencies = listOf("INR", "USD", "EUR", "GBP", "SGD", "AED", "MYR"),
    supportedCountries = listOf("IN")
)

private const val RAZORPAY_TEMPLATE = """<!DOCTYPE html>
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
        var prefill = {};
        if (params.email) prefill.email = params.email;
        if (params.name) {
          prefill.name = params.name;
        } else if (params.firstName || params.lastName) {
          prefill.name = ((params.firstName || '') + ' ' + (params.lastName || '')).trim();
        }
        if (params.phone) prefill.contact = params.phone;

        var options = {
          key: params.publicKey,
          amount: params.amount,
          currency: params.currency,
          prefill: prefill,
          handler: function(response) {
            sanwoCallback('success', {
              paymentId: response.razorpay_payment_id,
              reference: response.razorpay_payment_id,
              transaction_id: response.razorpay_payment_id,
              orderId: response.razorpay_order_id,
              signature: response.razorpay_signature
            });
          },
          modal: {
            ondismiss: function() {
              sanwoCallback('cancelled', {});
            }
          }
        };

        if (params.orderId) options.order_id = params.orderId;
        if (params.description) options.description = params.description;
        if (params.notes) options.notes = params.notes;
        if (params.theme) options.theme = params.theme;
        if (params.image) options.image = params.image;

        var rzp = new Razorpay(options);
        sanwoCallback('loaded', {});
        rzp.open();
      } catch(e) {
        sanwoCallback('error', { message: e.message });
      }
    }

    var rzpScript = document.createElement('script');
    rzpScript.src = 'https://checkout.razorpay.com/v1/checkout.js';
    rzpScript.onload = initPayment;
    rzpScript.onerror = function() {
      sanwoCallback('error', { message: 'Failed to load Razorpay SDK' });
    };
    document.body.appendChild(rzpScript);
  </script>
</body>
</html>"""
