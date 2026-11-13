# Changelog

## 0.2.0

- Version code 2. Same level behavior as 0.1.0, packaged for a Play App Bundle upload.
- `bundleRelease` signs the Android App Bundle with the same release key as the APK, and still refuses to fall back to the debug keystore.
- Release builds are not debuggable, and code shrinking stays off.
- Play listing draft in `play-store/`, and a privacy policy in `PRIVACY.md`. The app reads the gravity sensor, or the accelerometer when gravity is unavailable. It does not use the gyroscope or rotation vector.

## 0.1.0

- First release of Open Level, an ad-free bubble level for Android.
- Surface mode with a bullseye, horizontal and vertical vials, and live X/Y readouts to one decimal place.
- Edge mode with a large tilt angle for a wall or a shelf.
- Calibrate and reset, saved on the device. Hold a reading. Switch between degrees and percent slope.
- Optional haptic tick when the reading is within ±0.5°.
- Low-pass filtering on the gravity sensor, with an accelerometer fallback. The screen stays on while the app is open.
- No ads, no account, and no network permission.
