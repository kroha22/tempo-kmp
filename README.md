# Tempo Demo

**A mobile-first European Portuguese learning prototype built with Kotlin Multiplatform and shared Compose UI.**

[Open the web demo](https://tempo-kmp-demo.olga-kroha22.chatgpt.site) · Android · iOS · WebAssembly

> The hosted demo currently uses owner-only access through ChatGPT Sites. The application itself is a lightweight portfolio prototype: no account, backend, or personal data is included.

Tempo Demo explores how one learning experience can run across Android, iOS, and the web without duplicating product logic or interface code. It combines short guided lessons, a reusable card flow, and two visual presentations over the same language content and accepted answers.

## Preview

<p align="center">
  <img src="assets/screenshots/learning-route-mobile.png" width="240" alt="Tempo learning route on a mobile screen" />
  <img src="assets/screenshots/child-lesson-mobile.png" width="240" alt="Tempo child presentation lesson on a mobile screen" />
  <img src="assets/screenshots/cards-mobile.png" width="240" alt="Tempo vocabulary cards on a mobile screen" />
</p>

<p align="center"><sub>WebAssembly build captured at 390 × 844 CSS pixels.</sub></p>

## What is included

- A compact route with three European Portuguese lessons.
- Adult and child presentations backed by the same state and learning rules.
- Lesson examples, a Can-Do goal, a single-choice self-check, and completion progress.
- A six-card review flow with flip, save, and next actions.
- Stable lesson and card identifiers in typed demo content.
- Shared Compose UI and a pure Kotlin state reducer.
- Thin Android, iOS, and WebAssembly hosts.

The demo deliberately excludes the Books reader, the full 1,000-verb deck, accounts, cloud progress, and the larger production curriculum.

## Architecture

```text
shared/
├── commonMain/
│   ├── model/        typed lessons, cards, state, and reducer
│   └── ui/           shared Compose screens and presentation themes
├── commonTest/       platform-independent state tests
└── iosMain/          Compose UIViewController bridge

androidApp/           Android activity host
iosApp/               SwiftUI application host
webApp/               Kotlin/Wasm browser host
```

The shared reducer is intentionally independent from UI widgets. Switching between adult and child presentations therefore keeps lesson completion, card position, revealed state, and saved-card identifiers intact.

## Platform status

| Target | Current state | Verified |
| --- | --- | --- |
| Android | Runnable Compose application using the shared UI | Debug APK build |
| iOS | SwiftUI host embedding the shared Compose controller | Full unsigned Simulator build |
| Web | Kotlin/Wasm application using the same shared UI | Production bundle and mobile browser flow |

This is a portfolio-ready functional prototype, not a store-ready release. State currently lives in memory and resets when the application restarts. Signing, distribution metadata, persistent progress, accessibility hardening, and device-level release QA remain future work.

## Technology

- Kotlin `2.4.20`
- Compose Multiplatform `1.12.1`
- Gradle `9.6.1`
- Android Gradle Plugin `9.1.1`
- Kotlin/Wasm for the browser target
- SwiftUI as the thin iOS application shell

## Build and run

Requirements: JDK 17+, Android SDK for Android builds, and Xcode for iOS builds.

### Web

```bash
./gradlew :webApp:wasmJsBrowserDevelopmentRun
```

Create the production web bundle:

```bash
./gradlew :webApp:wasmJsBrowserDistribution
```

### Android

```bash
./gradlew :androidApp:assembleDebug
```

The debug APK is written under `androidApp/build/outputs/apk/debug/`.

### iOS

Open `iosApp/iosApp.xcodeproj` in Xcode and run the `iosApp` scheme on an Apple Silicon iOS Simulator.

### Shared tests

```bash
./gradlew :shared:iosSimulatorArm64Test
```

The common tests cover presentation-state parity, navigation, card-session wrapping, reveal reset, and stable saved-card IDs.

## Current direction

The next product step is to replace the small hard-coded demo pack with reusable lesson-block definitions, then port selected real Tempo lessons onto that shared model. Persistence and API integration should follow only after the lesson contract is stable.

---

Built as a focused cross-platform portfolio slice of the broader Tempo learning product.
