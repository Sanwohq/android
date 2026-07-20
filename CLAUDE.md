# Sanwo Android SDK

## Project Overview

Sanwo is a universal payment SDK. This is the Android/Kotlin library that wraps payment provider checkout flows in a WebView, using a JavaScript bridge to communicate results back to native code.

## Architecture

- **Provider pattern**: Each payment provider (Paystack, Flutterwave, etc.) supplies an HTML template with `{{sanwoBridge}}` and `{{params}}` placeholders
- **Engine**: Replaces placeholders with the JS bridge function and JSON parameters at runtime
- **WebView bridge**: `SanwoCheckoutActivity` loads rendered HTML in a WebView with a `@JavascriptInterface` named `JSBridge`
- **Message format**: `{ type: 'sanwo', event: 'success'|'cancelled'|'closed'|'error'|'loaded', data: {...} }`

## Build & Test

```bash
./gradlew :sanwo:build     # Build the library
./gradlew :sanwo:test      # Run unit tests
./gradlew :sanwo:lint      # Run Android lint
```

## Key Files

- `sanwo/src/main/kotlin/com/sanwohq/android/Sanwo.kt` -- Main SDK entry point
- `sanwo/src/main/kotlin/com/sanwohq/android/Engine.kt` -- Template rendering and amount conversion
- `sanwo/src/main/kotlin/com/sanwohq/android/SanwoCheckoutActivity.kt` -- WebView activity with JS bridge
- `sanwo/src/main/kotlin/com/sanwohq/android/templates/` -- Provider HTML templates

## Conventions

- Package: `com.sanwohq.android`
- Min SDK: 24, Target/Compile SDK: 35
- Kotlin with JVM target 17
- Amounts are always passed in **minor units** (kobo, cents) at the API boundary; the Engine converts when needed
- New providers go in `templates/` and are registered in `SanwoProviders.kt`
