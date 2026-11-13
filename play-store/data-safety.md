# Play Data safety answers (draft)

These answers match the app as of version 0.2.0. They have not been submitted in Play Console.

Play treats data as collected only when it is transmitted off the device. Open Level does not do that.

## Does your app collect or share any of the required user data types?

No.

## Data collected

None.

## Data shared

None.

## Data types

No data type is declared. In particular:

- Location: not collected
- Personal info: not collected
- Financial info: not collected
- Health and fitness: not collected
- Messages: not collected
- Photos and videos: not collected
- Audio files: not collected
- Files and docs: not collected
- Calendar: not collected
- Contacts: not collected
- App activity: not collected
- Web browsing: not collected
- App info and performance: not collected
- Device or other IDs: not collected

## On-device processing (not a collection)

The app reads the gravity sensor, or the accelerometer if the device has no gravity sensor, and uses that reading only to draw the level. It does not read the gyroscope, rotation vector, or other sensors. The samples are not saved as a log and are not transmitted.

Calibration, the haptic on/off choice, and the degrees-or-percent choice are stored in app-private preferences on the device. That is not a Play data type and it never leaves the device.

## Security practices

- Is all of the user data collected by your app encrypted in transit? Not applicable. No user data is collected, and the app has no network access.
- Do you provide a way for users to request that their data is deleted? Not applicable. No user data is collected or stored off the device. Uninstalling the app deletes the on-device preferences.
- Account creation: the app does not offer accounts.

## Independent security review

No. The app does not collect the data types that review applies to.

## Ads and SDKs

No advertising SDK. No analytics SDK. No data is sold or shared with third parties.
