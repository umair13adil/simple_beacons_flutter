## 2.0.8
- Updated AltBeacon library
- Added support for Flutter 3.0.0

## 2.0.7
- Added support for Android 12 OS (BroadCast receiver)

## 2.0.6
- Added support for Android 12 OS

## 2.0.5
- Fixed issue with callback on 'scannerReady' event

## 2.0.4
- Added callback for prominent disclosure message, to notify Flutter app

## 2.0.3
- Updated flow for showing the prominent disclosure message to be only shown when invoked from method channel

## 2.0.1
- Added method to add custom beacons layout (Android)
- Added method to set foreground scan periods (Android)
- Added method to set background scan periods (Android)
- Updated example app, added list of monitored beacons

## 2.0.0
- Migrated to null safety

## 1.0.20
- Clear added regions feature added

## 1.0.19
- (Android) Added option to set custom title & message for Disclosure dialog
- (Android) FIxed dialog always shown issue

## 1.0.18
- (Android) Removed ACCESS_BACKGROUND_LOCATION permission from plugin to prevent App Rejection
- (iOS) Removed UIBackgroundModes key from info.pList from Plugin to prevent App Rejection

## 1.0.17

* Minor bug fixing

## 1.0.15

* Removed unecessary application attributes (Android)

## 1.0.14

* Fixed missing new activity FLAG issue (Android)

## 1.0.13

* Updated client side code for Android
* Added feature to stop background service on app killed (Android)

## 1.0.12

* Handled initialization exception

## 1.0.10

* Fixed Beacons not detected in iOS.
* Fixed background service not stopped issue on stop scanning event.

## 1.0.9

* Fixed 'Beacon' JSON issue.

## 1.0.7

* Added Background scan mode 
* Fixed iOS build issue

## 1.0.5 - 1.0.6

* Fixed Method channel not found issue.

## 1.0.4

* Updated Android Logic.

## 1.0.1

* Initial Release.
