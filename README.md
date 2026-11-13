[Download Open Level 0.2.0 (APK)](https://github.com/clint-maples/open-level/releases/download/v0.2.0/OpenLevel-0.2.0.apk)

# Open Level

Open Level is a digital bubble level for Android. It is free, fully usable offline, and has no ads, no account, and no paid tier.

Lay the phone flat to read a surface, or stand it on its edge to check a wall or a shelf. The app reads the gravity sensor when the device has one, and the accelerometer otherwise. A low-pass filter keeps the reading steady without making it feel late.

## Features

- Circular bullseye for a phone laid flat, plus horizontal and vertical tube vials
- Live X and Y angles in degrees, shown to one decimal place
- The bubble turns green, and the phone can give a single haptic tick, when the reading is within ±0.5°
- Edge mode with a large roll/tilt angle, measured from vertical or horizontal
- Calibrate to treat the current position as zero, and reset that calibration later. Both are saved on the device
- Hold to lock the reading on screen
- Switch between degrees and percent slope
- The screen stays on while Open Level is open
- Dark, high-contrast Material 3 layout in portrait and landscape
- A clear empty state on devices that have neither a gravity sensor nor an accelerometer

## Install

Android 8.0 or newer is required. Download the APK linked above and open it. You may need to allow your browser or file manager to install apps.

- Package: `com.clintmaples.openlevel`
- Version: 0.2.0 (versionCode 2)

The signed Android App Bundle for the same version is `OpenLevel-0.2.0.aab` on the [v0.2.0 release](https://github.com/clint-maples/open-level/releases/tag/v0.2.0). A Play listing draft is in [play-store/listing.md](play-store/listing.md). It has not been published to Google Play.

## Privacy

Open Level does not show ads, does not sell a premium upgrade, and does not collect analytics. The manifest does not request the internet permission, and the app does not send or receive network traffic. It reads the gravity sensor, or the accelerometer when the device has no gravity sensor. It does not use the gyroscope or the rotation vector. Calibration, the haptic preference, and the degrees/percent choice stay in app-private storage on the device. The full policy is [PRIVACY.md](PRIVACY.md).

## Build

The project is Kotlin and Jetpack Compose. JDK 17 or newer and Android SDK 35 are required.

```bash
./gradlew test lint assembleDebug
```

A release APK or App Bundle is signed only when these environment variables are set. The path must be the absolute path of the release keystore. If any of them is missing, `assembleRelease` and `bundleRelease` fail and do not fall back to the debug keystore.

- `CLINT_ANDROID_KEYSTORE`
- `CLINT_ANDROID_STORE_PASSWORD`
- `CLINT_ANDROID_KEY_ALIAS`
- `CLINT_ANDROID_KEY_PASSWORD`

```bash
./gradlew assembleRelease bundleRelease
```

The signed APK is written to `app/build/outputs/apk/release/`. The signed App Bundle is written to `app/build/outputs/bundle/release/`. Release builds are not debuggable, and R8 shrinking is off. Keystores, `keystore.properties`, and signing bundles are gitignored. This repository is private and should stay private.

## License

MIT. See [LICENSE](LICENSE).
