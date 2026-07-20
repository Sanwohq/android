package com.sanwohq.paypal

import com.sanwohq.android.SanwoProvider

/**
 * PayPal provider for Sanwo.
 *
 * Auto-generated from @sanwohq/paypal — do not edit manually.
 */
val paypalProvider = SanwoProvider(
    id = "paypal",
    name = "paypal",
    displayName = "PayPal",
    template = PAYPAL_TEMPLATE,
    amountInMinorUnit = false,
    supportedCurrencies = listOf("USD", "EUR", "GBP", "CAD", "AUD", "JPY", "BRL", "MXN", "PHP", "THB", "SGD", "HKD", "NZD", "CHF", "SEK", "NOK", "DKK", "PLN", "CZK", "HUF"),
    supportedCountries = listOf("US", "GB", "CA", "AU", "DE", "FR", "IT", "ES", "NL", "BE", "AT", "JP", "BR", "MX")
)

private const val PAYPAL_TEMPLATE = """<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Sanwo Checkout</title>
</head>
<body style="background-color:#fff;height:100vh">
  <div id="paypal-button-container"></div>
  <script>
    {{sanwoBridge}}

    var params = {{params}};

    (function loadPayPalSDK() {
      var script = document.createElement('script');
      script.src = 'https://www.paypal.com/sdk/js?client-id=' + encodeURIComponent(params.publicKey);
      if (params.currency) {
        script.src += '&currency=' + encodeURIComponent(params.currency);
      }
      if (params.intent) {
        script.src += '&intent=' + encodeURIComponent(params.intent);
      }
      if (params.locale) {
        script.src += '&locale=' + encodeURIComponent(params.locale);
      }
      script.onload = function() {
        sanwoCallback('loaded', {});
        initPayment();
      };
      script.onerror = function() {
        sanwoCallback('error', { message: 'Failed to load PayPal SDK' });
      };
      document.head.appendChild(script);
    })();

    function initPayment() {
      try {
        paypal.Buttons({
          createOrder: function(data, actions) {
            return actions.order.create({
              purchase_units: [{
                amount: {
                  value: String(params.amount),
                  currency_code: params.currency || 'USD'
                }
              }]
            });
          },
          onApprove: function(data, actions) {
            return actions.order.capture().then(function(details) {
              sanwoCallback('success', {
                orderId: data.orderID,
                payerId: data.payerID,
                facilitatorAccessToken: data.facilitatorAccessToken,
                payer: details.payer,
                status: details.status,
                raw: details
              });
            });
          },
          onCancel: function(data) {
            sanwoCallback('cancelled', data || {});
          },
          onError: function(err) {
            sanwoCallback('error', { message: (err && err.message) ? err.message : 'PayPal checkout failed' });
          },
          style: params.style || {}
        }).render('#paypal-button-container');
      } catch(e) {
        sanwoCallback('error', { message: e.message });
      }
    }
  </script>
</body>
</html>"""
