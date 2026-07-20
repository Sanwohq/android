# Contributing to Sanwo Android SDK

Thank you for considering contributing to Sanwo! This document outlines the process for contributing to the Android SDK.

## Getting Started

1. Fork the repository
2. Clone your fork locally
3. Create a feature branch from `main`
4. Make your changes
5. Submit a pull request

## Development Setup

### Prerequisites

- Android Studio Ladybug (2024.2.1) or later
- JDK 17
- Android SDK with API level 35

### Building

```bash
./gradlew :sanwo:build
```

### Running Tests

```bash
./gradlew :sanwo:test
```

## Code Style

- Follow the [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful names for classes, functions, and variables
- Add KDoc comments for all public APIs
- Keep functions focused and small

## Adding a New Provider

1. Create a new Gradle module at the project root (e.g. `stripe/`) following the pattern in `paystack/` or `flutterwave/`
2. Add the module to `settings.gradle.kts` with `include(":stripe")`
3. Create a `build.gradle.kts` with `implementation(project(":sanwo"))` as a dependency
4. Create a provider file (e.g. `StripeProvider.kt`) that exports a `SanwoProvider` instance
5. The HTML template must include `{{sanwoBridge}}` and `{{params}}` placeholders
6. Test the provider thoroughly

### Template Requirements

- Include `{{sanwoBridge}}` in a `<script>` tag -- this is replaced with the JS bridge function
- Include `{{params}}` where the JSON parameters should be injected
- Call `sanwoCallback('loaded', {})` when the payment UI is ready
- Call `sanwoCallback('success', { reference, transaction_id, ... })` on success
- Call `sanwoCallback('cancelled', {})` on cancellation
- Call `sanwoCallback('error', { message })` on error

## Pull Request Guidelines

- Keep PRs focused on a single change
- Update documentation if you change public APIs
- Ensure CI passes before requesting review
- Write a clear description of what the PR does and why

## Reporting Issues

- Use GitHub Issues
- Include steps to reproduce
- Include the SDK version, Android version, and device model
- Include relevant logs or stack traces

## License

By contributing, you agree that your contributions will be licensed under the Apache License 2.0.
