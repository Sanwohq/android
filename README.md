# Sanwo Android SDK

Universal payment SDK for Android. Integrate Paystack, Flutterwave, and other payment providers with a single, consistent API.

## Installation

Add the core SDK and the provider module(s) you need.

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation("com.sanwohq:android:0.1.0")
    // Add one or more provider modules:
    implementation("com.sanwohq:paystack:0.1.0")
    implementation("com.sanwohq:flutterwave:0.1.0")
}
```

### Gradle (Groovy)

```groovy
dependencies {
    implementation 'com.sanwohq:android:0.1.0'
    // Add one or more provider modules:
    implementation 'com.sanwohq:paystack:0.1.0'
    implementation 'com.sanwohq:flutterwave:0.1.0'
}
```

## Quick Start

### 1. Create a Sanwo instance

```kotlin
import com.sanwohq.android.Sanwo
import com.sanwohq.paystack.paystackProvider

val sanwo = Sanwo(
    provider = paystackProvider,
    publicKey = "pk_test_..."
)
```

### 2. Listen for events (optional)

```kotlin
import com.sanwohq.android.SanwoEvent

sanwo.on(SanwoEvent.SUCCESS) { event ->
    Log.d("Sanwo", "Success: ${event.data}")
}

sanwo.on(SanwoEvent.ERROR) { event ->
    Log.e("Sanwo", "Error: ${event.data}")
}
```

### 3. Start checkout

#### Option A: Using Activity Result API (recommended)

Register the launcher in `onCreate`, then launch when ready:

```kotlin
class PaymentActivity : AppCompatActivity() {

    private val sanwo = Sanwo(
        provider = paystackProvider,
        publicKey = "pk_test_..."
    )

    private lateinit var checkoutLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkoutLauncher = sanwo.registerForCheckoutResult(this) { result ->
            when (result) {
                is CheckoutResult.Successful -> {
                    Log.d("Sanwo", "Reference: ${result.reference}")
                }
                is CheckoutResult.Cancelled -> {
                    Log.d("Sanwo", "User cancelled")
                }
                is CheckoutResult.Failed -> {
                    Log.e("Sanwo", "Failed: ${result.error}")
                }
            }
        }
    }

    private fun startPayment() {
        sanwo.launchCheckout(
            activity = this,
            launcher = checkoutLauncher,
            options = CheckoutOptions(
                amount = 500000, // 5000.00 NGN in kobo
                currency = "NGN",
                customer = CheckoutCustomer(
                    email = "customer@example.com",
                    firstName = "John",
                    lastName = "Doe"
                ),
                reference = "txn_${System.currentTimeMillis()}",
                onLoad = { Log.d("Sanwo", "Checkout loaded") },
                onError = { error -> Log.e("Sanwo", "Error: ${error.message}") }
            )
        )
    }
}
```

#### Option B: Callable pattern

```kotlin
sanwo(
    activity = this,
    options = CheckoutOptions(
        amount = 500000,
        currency = "NGN",
        customer = CheckoutCustomer(email = "user@example.com")
    ),
    onResult = { result ->
        when (result) {
            is CheckoutResult.Successful -> Log.d("Sanwo", "Ref: ${result.reference}")
            is CheckoutResult.Cancelled -> Log.d("Sanwo", "Cancelled")
            is CheckoutResult.Failed -> Log.e("Sanwo", "Failed: ${result.error}")
        }
    }
)
```

When using the callable pattern, add this to your Activity:

```kotlin
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    Sanwo.handleActivityResult(requestCode, resultCode, data)
}
```

## Supported Providers

Each provider is shipped as a separate module so you only include what you need.

| Provider | Module | Import | Notes |
|---|---|---|---|
| Paystack | `com.sanwohq:paystack` | `com.sanwohq.paystack.paystackProvider` | Amounts in kobo (minor units) |
| Flutterwave | `com.sanwohq:flutterwave` | `com.sanwohq.flutterwave.flutterwaveProvider` | Amounts auto-converted from minor to major units |
| Razorpay | `com.sanwohq:razorpay` | `com.sanwohq.razorpay.razorpayProvider` | |
| Monnify | `com.sanwohq:monnify` | `com.sanwohq.monnify.monnifyProvider` | |
| Interswitch | `com.sanwohq:interswitch` | `com.sanwohq.interswitch.interswitchProvider` | |

### Switching providers

```kotlin
import com.sanwohq.paystack.paystackProvider
import com.sanwohq.flutterwave.flutterwaveProvider

// Paystack
val sanwo = Sanwo(provider = paystackProvider, publicKey = "pk_test_...")

// Flutterwave
val sanwo = Sanwo(provider = flutterwaveProvider, publicKey = "FLWPUBK_TEST-...")
```

## Checkout Options

| Parameter | Type | Required | Description |
|---|---|---|---|
| `amount` | `Long` | Yes | Amount in smallest currency unit (e.g. kobo, cents) |
| `currency` | `String` | Yes | ISO 4217 currency code |
| `customer` | `CheckoutCustomer` | Yes | Customer details (email required) |
| `reference` | `String?` | No | Unique transaction reference |
| `channels` | `List<String>?` | No | Payment channels (e.g. `["card", "bank"]`) |
| `metadata` | `Map<String, Any?>?` | No | Arbitrary metadata |
| `paymentOptions` | `String?` | No | Flutterwave payment options |
| `subaccounts` | `List<Map<String, Any?>>?` | No | Flutterwave sub-accounts |
| `method` | `String?` | No | Paystack checkout method |
| `onLoad` | `(() -> Unit)?` | No | Called when payment UI loads |
| `onError` | `((Throwable) -> Unit)?` | No | Called on error |

## Checkout Result

```kotlin
sealed class CheckoutResult {
    data class Successful(
        val provider: String,
        val reference: String,
        val transactionId: String? = null,
        val raw: Map<String, Any?> = emptyMap()
    ) : CheckoutResult()

    data class Cancelled(
        val provider: String,
        val reference: String? = null
    ) : CheckoutResult()

    data class Failed(
        val provider: String,
        val reference: String? = null,
        val error: String
    ) : CheckoutResult()
}
```

## Custom Providers

You can create your own provider by supplying an HTML template:

```kotlin
val myProvider = SanwoProvider(
    id = "my-gateway",
    name = "my-gateway",
    displayName = "My Gateway",
    template = """
        <!DOCTYPE html>
        <html>
        <body onload="init()">
        <script>
            {{sanwoBridge}}
            var params = {{params}};
            function init() {
                // Your payment logic here
                sanwoCallback('success', { reference: 'ref123' });
            }
        </script>
        </body>
        </html>
    """.trimIndent(),
    amountInMinorUnit = false // set to true if your provider expects minor units
)

val sanwo = Sanwo(provider = myProvider, publicKey = "your_key")
```

## Requirements

- Android API 24+ (Android 7.0)
- Kotlin 2.1+

## License

Apache License 2.0 - see [LICENSE](LICENSE) for details.
