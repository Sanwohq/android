package com.sanwohq.stripe

import com.sanwohq.android.SanwoProvider

/**
 * Stripe provider for Sanwo.
 *
 * Auto-generated from @sanwohq/stripe — do not edit manually.
 */
val stripeProvider = SanwoProvider(
    id = "stripe",
    name = "stripe",
    displayName = "Stripe",
    template = STRIPE_TEMPLATE,
    amountInMinorUnit = true,
    supportedCurrencies = listOf("USD", "EUR", "GBP", "CAD", "AUD", "JPY", "CHF", "SEK", "NOK", "DKK", "NZD", "SGD", "HKD", "MXN", "BRL", "PLN", "CZK", "HUF", "RON", "BGN", "INR", "MYR", "THB", "PHP", "IDR", "KRW", "TWD", "ZAR", "AED", "SAR"),
    supportedCountries = listOf("US", "GB", "CA", "AU", "DE", "FR", "JP", "IT", "ES", "NL", "BE", "AT", "CH", "SE", "NO", "DK", "FI", "IE", "PT", "LU", "SG", "HK", "NZ", "MX", "BR", "IN", "MY", "TH", "PL", "CZ", "RO", "BG", "HU", "AE", "SA")
)

private const val STRIPE_TEMPLATE = """<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Sanwo Checkout</title>
  <style>
    * { margin: 0; padding: 0; box-sizing: border-box; }
    body { background-color: #fff; height: 100vh; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; }
    #payment-form { max-width: 500px; margin: 40px auto; padding: 20px; }
    #payment-element { margin-bottom: 24px; }
    #submit-button {
      width: 100%;
      padding: 12px;
      background-color: #635bff;
      color: #fff;
      border: none;
      border-radius: 4px;
      font-size: 16px;
      cursor: pointer;
    }
    #submit-button:disabled { opacity: 0.5; cursor: not-allowed; }
    #error-message { color: #df1b41; margin-top: 12px; font-size: 14px; }
    .spinner { display: none; }
    .spinner.active { display: inline-block; }
  </style>
</head>
<body onload="initPayment()">
  <script src="https://js.stripe.com/v3/"></script>
  <script>
    {{sanwoBridge}}

    var params = {{params}};

    function initPayment() {
      try {
        var stripe = Stripe(params.publicKey);

        if (params.sessionId) {
          stripe.redirectToCheckout({ sessionId: params.sessionId })
            .then(function(result) {
              if (result.error) {
                sanwoCallback('error', {
                  message: result.error.message,
                  raw: result.error
                });
              }
            })
            .catch(function(e) {
              sanwoCallback('error', { message: e.message });
            });
          return;
        }

        if (params.clientSecret) {
          var appearance = params.appearance || {};
          var elements = stripe.elements({
            clientSecret: params.clientSecret,
            appearance: appearance
          });

          var paymentElement = elements.create('payment');

          var form = document.createElement('form');
          form.id = 'payment-form';

          var paymentDiv = document.createElement('div');
          paymentDiv.id = 'payment-element';
          form.appendChild(paymentDiv);

          var submitButton = document.createElement('button');
          submitButton.id = 'submit-button';
          submitButton.type = 'submit';
          submitButton.textContent = 'Pay Now';
          form.appendChild(submitButton);

          var errorDiv = document.createElement('div');
          errorDiv.id = 'error-message';
          form.appendChild(errorDiv);

          document.body.appendChild(form);
          paymentElement.mount('#payment-element');
          sanwoCallback('loaded', {});

          form.addEventListener('submit', function(event) {
            event.preventDefault();
            submitButton.disabled = true;
            submitButton.textContent = 'Processing...';

            var confirmParams = {};
            if (params.returnUrl) {
              confirmParams.return_url = params.returnUrl;
            }

            var confirmMethod = params.clientSecret.startsWith('seti_')
              ? stripe.confirmSetup
              : stripe.confirmPayment;

            var confirmOptions = {
              elements: elements,
              confirmParams: confirmParams,
              redirect: params.returnUrl ? 'if_required' : 'if_required'
            };

            confirmMethod.call(stripe, confirmOptions)
              .then(function(result) {
                if (result.error) {
                  if (result.error.type === 'card_error' || result.error.type === 'validation_error') {
                    errorDiv.textContent = result.error.message;
                  } else {
                    errorDiv.textContent = 'An unexpected error occurred.';
                  }
                  submitButton.disabled = false;
                  submitButton.textContent = 'Pay Now';

                  if (result.error.code === 'payment_intent_authentication_failure') {
                    sanwoCallback('cancelled', {
                      message: result.error.message,
                      raw: result.error
                    });
                  } else {
                    sanwoCallback('error', {
                      message: result.error.message,
                      code: result.error.code,
                      type: result.error.type,
                      raw: result.error
                    });
                  }
                } else {
                  var paymentIntent = result.paymentIntent || result.setupIntent;
                  sanwoCallback('success', {
                    id: paymentIntent.id,
                    reference: paymentIntent.id,
                    transaction_id: paymentIntent.id,
                    status: paymentIntent.status,
                    raw: paymentIntent
                  });
                }
              })
              .catch(function(e) {
                submitButton.disabled = false;
                submitButton.textContent = 'Pay Now';
                sanwoCallback('error', { message: e.message });
              });
          });

          return;
        }

        sanwoCallback('error', {
          message: 'Either sessionId or clientSecret must be provided.'
        });
      } catch(e) {
        sanwoCallback('error', { message: e.message });
      }
    }
  </script>
</body>
</html>"""
