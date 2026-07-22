# Sanwo Android SDK

## Project Overview

Sanwo is a universal payment SDK. This is the Android/Kotlin library that wraps payment provider checkout flows in a WebView, using a JavaScript bridge to communicate results back to native code.

## Architecture

- **Provider pattern**: Each payment provider (Paystack, Flutterwave, etc.) lives in its own Gradle module and exports a `SanwoProvider` instance with an HTML template containing `{{sanwoBridge}}` and `{{params}}` placeholders
- **Engine**: Replaces placeholders with the JS bridge function and JSON parameters at runtime
- **WebView bridge**: `SanwoCheckoutActivity` loads rendered HTML in a WebView with a `@JavascriptInterface` named `JSBridge`
- **Message format**: `{ type: 'sanwo', event: 'success'|'cancelled'|'closed'|'error'|'loaded', data: {...} }`

## Build & Test

```bash
./gradlew :sanwo:build          # Build the core library
./gradlew :paystack:build       # Build the Paystack provider module
./gradlew :flutterwave:build    # Build the Flutterwave provider module
./gradlew :sanwo:test           # Run unit tests
./gradlew :sanwo:lint           # Run Android lint
```

## Key Files

- `sanwo/src/main/kotlin/com/sanwohq/android/Sanwo.kt` -- Main SDK entry point
- `sanwo/src/main/kotlin/com/sanwohq/android/Engine.kt` -- Template rendering and amount conversion
- `sanwo/src/main/kotlin/com/sanwohq/android/SanwoCheckoutActivity.kt` -- WebView activity with JS bridge
- `sanwo/src/main/kotlin/com/sanwohq/android/SanwoProvider.kt` -- Provider data class
- `paystack/src/main/kotlin/com/sanwohq/paystack/PaystackProvider.kt` -- Paystack provider module
- `flutterwave/src/main/kotlin/com/sanwohq/flutterwave/FlutterwaveProvider.kt` -- Flutterwave provider module

## CI/CD

### CI (`ci.yml`)
- **Triggers**: push to main, PRs to main
- **Job**: `build` — sets up JDK 17 (temurin), Gradle, runs `./gradlew build`
- No secrets required

### Publishing
- Published via **JitPack** — no CI publish step needed
- JitPack builds automatically from git tags
- To publish: create a GitHub release with a version tag (e.g., `v0.1.0`)
- Users add `implementation 'com.github.Sanwohq:android:<tag>'` to their Gradle dependencies

### Provider templates
- Templates must use dynamic script loading (createElement + onload), not static `<script src>` tags — static tags cause race conditions in WebViews
- When core repo publishes template updates, `sync-native-providers.mjs` pushes changes to this repo automatically (requires `NATIVE_SYNC_TOKEN`)

## Conventions

- Core SDK package: `com.sanwohq.android`
- Provider module packages: `com.sanwohq.paystack`, `com.sanwohq.flutterwave`, etc.
- Min SDK: 24, Target/Compile SDK: 35
- Kotlin with JVM target 17
- Amounts are always passed in **minor units** (kobo, cents) at the API boundary; the Engine converts when needed
- New providers get their own Gradle module (see `paystack/` or `flutterwave/` for the pattern)
