## 2.2.8 — Yoer fork changes

- Add iOS Swift Package Manager support while retaining CocoaPods support.

## 2.2.1 — Yoer fork changes

* Added the iOS-only `useCachesDirectory` option to `initLogs`. When enabled, logs are stored under the app's Caches directory; otherwise, they use Application Support. Android ignores this option.
* Applied `logsWriteDirectoryName` to the iOS log path and placed logs in its `Logs` subdirectory, matching the configured log root used by Android.
* Aligned the iOS Objective-C and Swift plugin registration names with `FlutterLogsYoerPlugin` so they match the fork's declared plugin class.
