## 2.2.7
* Refactored the Android plugin to keep channel and context state on each plugin instance and clear it when the Flutter engine detaches
* Removed static embedding state and simplified the example Android activity

## 2.2.6
* Dispatch native-to-Dart channel callbacks on the main thread

## 2.2.5
* Added MethodChannel result callbacks to native-to-Dart export and print notifications, with delivery diagnostics

## 2.2.4
* Fixed nullable native export responses to avoid a Dart runtime type error

## 2.2.3
* Completed native export method results so Dart calls do not remain pending
* Added diagnostics for an unavailable Android method channel during export

## 2.2.2
* Added Android backpressure controls for high-volume logging
* Added Android redaction and masking for built-in and custom patterns
* Added Android keyword search and filtered log export/printing
* Added examples for the new Android features

## 2.2.1
* Updated Android source and target compatibility to Java 8

## 2.2.0
* Updated RxLogs version
* Added support for 'isEnabled' flag to disable logs writing completely

## 2.1.10
* Set min sdkVersion to 16

## 2.1.9
* Added support for Android SDK 33

## 2.1.8
* Fixed build issues from [PR #47]

## 2.1.7
* Added Support for Android Embedding v2
* Updated dependencies
* Fixes PR #28,async functions should return Future + format

## 2.1.6
* Added support for flutter 3 build

## 2.1.5
* Removed double logs printing
* Updated RxLogs versions

## 2.1.4
* Null Safety Added

## 2.1.3
* Fixed file write issue on iOS Release Builds

## 2.1.2
* Added support for logging in iOS apps

## 2.1.1

* Improved logs export flow
* Added example for exporting logs in example app

## 2.1.0

* Updated Error messages Format in case of Errors & Exception logs

## 2.0.6

* Updated PLogs library version
* Removed Runtime permissions for storage

## 0.0.1

* TODO: Describe initial release.
