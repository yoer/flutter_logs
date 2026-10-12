# Flutter Logs 上游升级设计

状态：待审阅

基准：`flutter_logs_yoer` 2.2.1 与下载目录中的 `flutter_logs` 2.2.7（2026-10-12 核对）

## 目标

将上游 2.2.2 至 2.2.7 的功能和修复合入 `flutter_logs_yoer`，同时完整保留 2.2.1 已有的 Yoer 定制行为和插件身份。

## 现状依据

- fork 的 Android 实现依赖 RxLogs `v1.0.23`，上游 2.2.7 使用 `v1.0.27`。
- 上游新增日志限流、脱敏、格式控制和过滤查询，并修复 Android 导出调用返回、回调线程及插件分离后的资源清理。
- fork 的 iOS 初始化支持 `useCachesDirectory` 与自定义 `savePath`。工作区内两个 Zego 日志服务会启用 `useCachesDirectory`。
- 上游的 iOS 代码没有实现新加的 Android 日志功能，并移除了 fork 的缓存目录逻辑。
- 上游把 `setMetaInfo(labels:)` 从字符串改成字符串映射。fork 当前 Android 代码没有把旧字符串写入 MetaInfo，工作区运行时代码也没有调用该参数。
- 当前工作区已有未提交的 Dart 修改和未跟踪的 `example/android/.settings/`，升级不得覆盖或清理它们。

## 设计决定

### 保留 fork 身份与 2.2.1 行为

包名保持 `flutter_logs_yoer`。Dart 类、MethodChannel、EventChannel、Android 包名与插件类、iOS 注册类和 podspec 名称继续使用 fork 标识。

iOS 继续读取 `useCachesDirectory`：`true` 使用 Caches 目录，`false` 使用 Application Support。iOS 继续把 `logsWriteDirectoryName` 用作日志目录前缀并在其下创建 `Logs` 目录。Android 继续忽略 `useCachesDirectory`。

保留已有日志、文件日志、导出、打印、MQTT 和元信息方法。升级时合并原生实现，不以整包覆盖方式替换 fork 代码。保留当前工作区已有的 Dart 错误消息判断改动和其他未提交文件。

`CHANGELOG-yoer.md` 单独记录 Yoer 定制历史；上游 `CHANGELOG.md` 记录上游版本历史。

### 合入上游 2.2.2 至 2.2.7

Dart API 在 `FlutterLogsYoer` 上增加上游对应的配置类型、初始化参数、日志过滤方法和序列化逻辑，并保留 `useCachesDirectory`。导出方法采用 nullable 返回类型，适配原生侧可能返回空值的行为。

Android 侧升级 RxLogs 至 `v1.0.27`，合入限流、脱敏、日志格式、自动导出和过滤相关配置。插件通道状态改为实例字段；引擎分离时注销并清空通道；向 Dart 派发回调时切到主线程并记录送达结果。异步导出处理器及时完成 MethodChannel 请求，避免 Dart 调用等待不返回。

`setMetaInfo(labels:)` 接受旧的字符串调用和上游新增的 `Map<String, String>` 调用。字符串输入保持源码兼容，并维持其在当前 fork 中未写入 MetaInfo 的既有行为；Map 输入在边界处校验键和值为字符串，再传给新版本原生库。

### 平台范围

限流、脱敏、日志格式扩展和过滤查询依赖 Android 的 RxLogs 实现。iOS 保留现有 2.2.1 能力，不宣称支持这些新增功能。README 和示例明确标出平台差异。

## 文件范围

- `lib/flutter_logs_yoer.dart`：合并 Dart API 与兼容处理。
- `android/build.gradle`：升级 RxLogs 依赖。
- `android/src/main/kotlin/com/flutter/logs/plogs/flutter_logs_yoer/FlutterLogsYoerPlugin.kt`：通道生命周期、回调线程、方法处理和返回值。
- `android/src/main/kotlin/com/flutter/logs/plogs/flutter_logs_yoer/LogsHelper.kt`、`Utils.kt`：新配置解析与映射。
- `pubspec.yaml`、`example/pubspec.lock`：版本与依赖锁定信息。
- `CHANGELOG.md`、`CHANGELOG-yoer.md`、`README.md`、`example/README.md`：分开记录上游和 fork 定制，更新用法及平台说明。
- `example/lib/main.dart`：保留现有示例能力，更新元信息调用并展示新增 Android API。

iOS 原生实现、fork 插件标识、工作区里的 Zego 调用方和预存的未提交文件不在改写范围内。

## 验收条件

- 包名、插件类名、原生包名和通道名仍与 `flutter_logs_yoer` 一致。
- iOS 的缓存目录选择和自定义日志目录行为与 2.2.1 一致。
- Android 暴露上游新增 API，并包含 2.2.3 至 2.2.7 的原生回调和生命周期修复。
- 旧字符串 `labels` 调用仍可编译；Map 标签按字符串键值传递。
- 上游和 Yoer 定制变更分别记录在对应 changelog，README 说明 Android 与 iOS 能力差别。
- 现有未提交的 `lib/flutter_logs_yoer.dart` 修改和 `example/android/.settings/` 保持原样。
