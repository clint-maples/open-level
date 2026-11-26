# Changelog

## 0.3.1

- Version code 4. Targets and compiles against API 36 so the Play production upload meets the current target API requirement.
- Android Gradle Plugin bumped from 8.7.3 to 8.10.1, which is the toolchain needed for compileSdk 36. Gradle 8.11.1 is unchanged.
- Same level behavior and application id (`foundation.islf.openlevel`) as 0.3.0.

## 0.3.0

- Version code 3. The application id is now `foundation.islf.openlevel`. Earlier releases used `com.clintmaples.openlevel`, so 0.3.0 installs as a separate app and will not upgrade 0.1.0 or 0.2.0 in place.
- Same level behavior as 0.2.0.

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
