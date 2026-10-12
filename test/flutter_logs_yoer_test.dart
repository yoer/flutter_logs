import 'package:flutter/services.dart';
import 'package:flutter_logs_yoer/flutter_logs_yoer.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  const MethodChannel channel = MethodChannel('flutter_logs_yoer');
  final List<MethodCall> calls = <MethodCall>[];
  Object? response;

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    calls.clear();
    response = 'ok';
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(channel, (MethodCall methodCall) async {
      calls.add(methodCall);
      return response;
    });
  });

  tearDown(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(channel, null);
  });

  group('initLogs', () {
    test('encodes new config and preserves cache option', () async {
      const BackpressureConfig backpressure = BackpressureConfig(
        queueCapacity: 12,
        warnQueueCapacity: 20,
        perLevelQuotas: <LogLevel, int>{LogLevel.ERROR: 2},
        quotaWindowMillis: 321,
      );
      const RedactionConfig redaction = RedactionConfig(
        enableBuiltInPatterns: <BuiltInPattern>{BuiltInPattern.EMAIL},
        customRules: <RedactionRule>[
          RedactionRule(
            name: 'account',
            patternString: r'\d{4}',
            maskType: MaskType.HASH,
            replacement: '[hidden]',
          ),
        ],
        defaultMaskType: MaskType.PARTIAL,
      );

      await FlutterLogsYoer.initLogs(
        directoryStructure: DirectoryStructure.SINGLE_FILE_FOR_DAY,
        timeStampFormat: TimeStampFormat.TIME_FORMAT_24_FULL,
        logFileExtension: LogFileExtension.LOG,
        useCachesDirectory: true,
        forceWriteLogs: false,
        enableLogsWriteToFile: false,
        formatType: FormatType.FORMAT_CSV,
        customFormatOpen: '<',
        customFormatClose: '>',
        logFilesLimit: 8,
        nameForEventDirectory: 'events',
        autoExportLogTypes: <String>['Errors', 'Network'],
        autoExportLogTypesPeriod: 15,
        csvDelimiter: '|',
        exportFormatted: false,
        exportFileNamePostFix: '_post',
        exportFileNamePreFix: 'pre_',
        backpressureConfig: backpressure,
        redactionConfig: redaction,
      );

      expect(calls, hasLength(1));
      expect(calls.single.method, 'initLogs');
      final Map<dynamic, dynamic> arguments =
          calls.single.arguments as Map<dynamic, dynamic>;
      expect(arguments['useCachesDirectory'], isTrue);
      expect(arguments['forceWriteLogs'], isFalse);
      expect(arguments['enableLogsWriteToFile'], isFalse);
      expect(arguments['formatType'], 'FORMAT_CSV');
      expect(arguments['customFormatOpen'], '<');
      expect(arguments['customFormatClose'], '>');
      expect(arguments['logFilesLimit'], 8);
      expect(arguments['nameForEventDirectory'], 'events');
      expect(arguments['autoExportLogTypes'], 'Errors,Network');
      expect(arguments['autoExportLogTypesPeriod'], 15);
      expect(arguments['csvDelimiter'], '|');
      expect(arguments['exportFormatted'], isFalse);
      expect(arguments['exportFileNamePostFix'], '_post');
      expect(arguments['exportFileNamePreFix'], 'pre_');
      expect(arguments['backpressureConfig'], <String, dynamic>{
        'queueCapacity': 12,
        'warnQueueCapacity': 20,
        'perLevelQuotas': <String, int>{'ERROR': 2},
        'quotaWindowMillis': 321,
      });
      expect(arguments['redactionConfig'], <String, dynamic>{
        'enableBuiltInPatterns': <String>['EMAIL'],
        'customRules': <Map<String, dynamic>>[
          <String, dynamic>{
            'name': 'account',
            'patternString': r'\d{4}',
            'maskType': 'HASH',
            'replacement': '[hidden]',
          },
        ],
        'defaultMaskType': 'PARTIAL',
      });
    });
  });

  group('setMetaInfo', () {
    test('accepts legacy and structured labels', () async {
      await FlutterLogsYoer.setMetaInfo(
        organizationName: 'Zego',
        labels: 'legacy-labels',
      );
      expect(
        (calls.single.arguments as Map<dynamic, dynamic>)['labels'],
        <String, String>{},
      );
      expect(
        (calls.single.arguments as Map<dynamic, dynamic>)['organizationName'],
        'Zego',
      );

      await FlutterLogsYoer.setMetaInfo(
        labels: <String, String>{'team': 'mobile', 'region': 'apac'},
      );
      expect(
        (calls.last.arguments as Map<dynamic, dynamic>)['labels'],
        <String, String>{'team': 'mobile', 'region': 'apac'},
      );
    });

    test('rejects invalid label maps', () async {
      await expectLater(
        FlutterLogsYoer.setMetaInfo(labels: <dynamic, dynamic>{'team': 42}),
        throwsArgumentError,
      );
      expect(calls, isEmpty);
    });
  });

  test('logThis forwards non-empty errorMessage content', () async {
    await FlutterLogsYoer.logThis(
      tag: 'example',
      subTag: 'failure',
      logMessage: 'request failed',
      level: LogLevel.ERROR,
      errorMessage: 'timeout',
    );

    expect(calls.single.method, 'logThis');
    expect(calls.single.arguments, <String, dynamic>{
      'tag': 'example',
      'subTag': 'failure',
      'logMessage': 'request failed , Error: timeout',
      'level': 'ERROR',
    });
  });

  group('filtered log methods', () {
    test('encode options and reject empty keywords', () async {
      await FlutterLogsYoer.exportFilteredLogs(
        keywords: <String>['alpha', 'Beta'],
        filterType: FilterType.AND,
        exportType: ExportType.TODAY,
        decryptBeforeExporting: true,
        ignoreCase: false,
      );
      expect(calls.single.method, 'exportFilteredLogs');
      expect(calls.single.arguments, <String, dynamic>{
        'keywords': 'alpha,Beta',
        'filterType': 'AND',
        'exportType': 'TODAY',
        'decryptBeforeExporting': true,
        'ignoreCase': false,
      });

      await FlutterLogsYoer.printFilteredLogs(
        keywords: <String>['needle'],
      );
      expect(calls.last.method, 'printFilteredLogs');
      expect(calls.last.arguments, <String, dynamic>{
        'keywords': 'needle',
        'filterType': 'OR',
        'exportType': 'ALL',
        'decryptBeforeExporting': false,
        'ignoreCase': true,
      });

      final int callCountBeforeEmpty = calls.length;
      await FlutterLogsYoer.exportFilteredLogs(keywords: <String>[]);
      await FlutterLogsYoer.printFilteredLogs(keywords: <String>[]);
      expect(calls, hasLength(callCountBeforeEmpty));
    });
  });

  test('exportLogs accepts null response', () async {
    response = null;
    await expectLater(FlutterLogsYoer.exportLogs(), completes);
  });
}
