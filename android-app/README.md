# GuardianAI (Android - Compose)

This folder contains a minimal Android app scaffold (Kotlin + Jetpack Compose) for the Guardian AI mobile UI.

What is included
- Splash screen (placeholder)
- Permissions screen (first-boot) with toggles for notifications, SMS, and call
- Home screen with fall history placeholder and emergency contact button
- Foreground service scaffold `FallDetectionService` for background monitoring (no model integrated yet)
- Dark mode toggle persisted in SharedPreferences

How to open
1. Open `android-app` in Android Studio.
2. Let Android Studio sync Gradle and install recommended plugins.
3. Run the `app` module on a device or emulator.

Notes
- This is a lightweight development scaffold. The model (tflite) and scaler JSON should be added to `app/src/main/assets/` before implementing inference.
- The service is a placeholder; integrate sensor collection, feature extraction, and TFLite inference following the mobile integration guide.

Next steps I can implement if you want
- Add Room DB for persistent fall history
- Implement emergency contact UI and persistence
- Integrate TFLite model and StandardScaler JSON
- Add sound-alert and multi-level alert flow

