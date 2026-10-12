package com.flutter.logs.plogs.flutter_logs_yoer

import com.blackbox.plog.pLogs.exporter.ExportType
import com.blackbox.plog.pLogs.backpressure.BackpressureConfig
import com.blackbox.plog.pLogs.formatter.FormatType
import com.blackbox.plog.pLogs.formatter.TimeStampFormat
import com.blackbox.plog.pLogs.models.LogExtension
import com.blackbox.plog.pLogs.models.LogLevel
import com.blackbox.plog.pLogs.redaction.BuiltInPattern
import com.blackbox.plog.pLogs.redaction.MaskType
import com.blackbox.plog.pLogs.redaction.RedactionConfig
import com.blackbox.plog.pLogs.redaction.RedactionRule
import com.blackbox.plog.pLogs.structure.DirectoryStructure
import io.flutter.plugin.common.MethodCall
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream

fun getLogLevelsById(key: String, call: MethodCall): ArrayList<LogLevel> {
    val listOfLogLevels = arrayListOf<LogLevel>()
    call.argument<String>(key)?.let {
        it.split(",").forEach {
            listOfLogLevels.add(getLogLevel(it))
        }
        return listOfLogLevels
    }
    return arrayListOf()
}

fun getListOfStringById(key: String, call: MethodCall): ArrayList<String> {
    val logTypesList = arrayListOf<String>()
    call.argument<String>(key)?.let {
        it.split(",").forEach {
            logTypesList.add(it)
        }
        return logTypesList
    }
    return arrayListOf()
}

fun getStringValueById(key: String, call: MethodCall): String {
    call.argument<String>(key)?.let {
        return it
    }
    return ""
}

fun getIntValueById(key: String, call: MethodCall): Int? {
    call.argument<Int>(key)?.let {
        return it
    }
    return null
}

fun getBoolValueById(key: String, call: MethodCall): Boolean {
    call.argument<Boolean>(key)?.let {
        return it
    }
    return false
}

fun getStringMapValueById(key: String, call: MethodCall): Map<String, String> {
    return when (val value = call.argument<Any>(key)) {
        null, is String -> emptyMap()
        is Map<*, *> -> value.entries.associate { (entryKey, entryValue) ->
            require(entryKey is String) { "$key keys must be strings" }
            require(entryValue is String) { "$key values must be strings" }
            entryKey to entryValue
        }
        else -> throw IllegalArgumentException("$key must be a string map")
    }
}

fun getInputStreamValueById(key: String, call: MethodCall): InputStream? {
    call.argument<ByteArray>(key)?.let {
        return ByteArrayInputStream(it)
    }
    return null
}


fun getDirectoryStructure(type: String?): DirectoryStructure {
    when (type) {
        "FOR_DATE" -> {
            return DirectoryStructure.FOR_DATE
        }
        "FOR_EVENT" -> {
            return DirectoryStructure.FOR_EVENT
        }
        "SINGLE_FILE_FOR_DAY" -> {
            return DirectoryStructure.SINGLE_FILE_FOR_DAY
        }
    }
    return DirectoryStructure.SINGLE_FILE_FOR_DAY
}

fun getLogFileExtension(type: String?): String {
    when (type) {
        "TXT" -> {
            return LogExtension.TXT
        }
        "CSV" -> {
            return LogExtension.CSV
        }
        "LOG" -> {
            return LogExtension.LOG
        }
        "NONE" -> {
            return LogExtension.NONE
        }
    }
    return LogExtension.LOG
}

fun getLogLevel(type: String?): LogLevel {
    when (type) {
        "INFO" -> {
            return LogLevel.INFO
        }
        "WARNING" -> {
            return LogLevel.WARNING
        }
        "ERROR" -> {
            return LogLevel.ERROR
        }
        "SEVERE" -> {
            return LogLevel.SEVERE
        }
    }
    return LogLevel.INFO
}

fun getExportType(type: String?): ExportType {
    when (type) {
        "TODAY" -> {
            return ExportType.TODAY
        }
        "LAST_HOUR" -> {
            return ExportType.LAST_HOUR
        }
        "WEEKS" -> {
            return ExportType.WEEKS
        }
        "LAST_24_HOURS" -> {
            return ExportType.LAST_24_HOURS
        }
        "ALL" -> {
            return ExportType.ALL
        }
    }
    return ExportType.ALL
}

fun getTimeStampFormat(type: String?): String {
    when (type) {
        "DATE_FORMAT_1" -> {
            return TimeStampFormat.DATE_FORMAT_1
        }
        "DATE_FORMAT_2" -> {
            return TimeStampFormat.DATE_FORMAT_2
        }
        "TIME_FORMAT_FULL_JOINED" -> {
            return TimeStampFormat.TIME_FORMAT_FULL_JOINED
        }
        "TIME_FORMAT_FULL_1" -> {
            return TimeStampFormat.TIME_FORMAT_FULL_1
        }
        "TIME_FORMAT_FULL_2" -> {
            return TimeStampFormat.TIME_FORMAT_FULL_2
        }
        "TIME_FORMAT_24_FULL" -> {
            return TimeStampFormat.TIME_FORMAT_24_FULL
        }
        "TIME_FORMAT_READABLE" -> {
            return TimeStampFormat.TIME_FORMAT_READABLE
        }
        "TIME_FORMAT_READABLE_2" -> {
            return TimeStampFormat.TIME_FORMAT_READABLE_2
        }
        "TIME_FORMAT_SIMPLE" -> {
            return TimeStampFormat.TIME_FORMAT_SIMPLE
        }
    }
    return TimeStampFormat.TIME_FORMAT_24_FULL
}

fun getFormatType(type: String?): FormatType {
    return when (type) {
        "FORMAT_CURLY" -> FormatType.FORMAT_CURLY
        "FORMAT_SQUARE" -> FormatType.FORMAT_SQUARE
        "FORMAT_CSV" -> FormatType.FORMAT_CSV
        "FORMAT_CUSTOM" -> FormatType.FORMAT_CUSTOM
        else -> FormatType.FORMAT_CURLY
    }
}

fun getMaskType(type: String?): MaskType {
    return when (type) {
        "FULL_MASK" -> MaskType.FULL_MASK
        "HASH" -> MaskType.HASH
        "PARTIAL" -> MaskType.PARTIAL
        else -> MaskType.FULL_MASK
    }
}

fun getBuiltInPattern(type: String?): BuiltInPattern? {
    return when (type) {
        "PHONE_NUMBER" -> BuiltInPattern.PHONE_NUMBER
        "EMAIL" -> BuiltInPattern.EMAIL
        "CREDIT_CARD" -> BuiltInPattern.CREDIT_CARD
        "JWT_TOKEN" -> BuiltInPattern.JWT_TOKEN
        "IP_ADDRESS" -> BuiltInPattern.IP_ADDRESS
        else -> null
    }
}

fun getBackpressureConfig(map: Map<String, Any>?): BackpressureConfig? {
    return map?.let { configMap ->
        BackpressureConfig(
            queueCapacity = configMap["queueCapacity"] as? Int ?: 500,
            warnQueueCapacity = configMap["warnQueueCapacity"] as? Int ?: 750,
            perLevelQuotas = (configMap["perLevelQuotas"] as? Map<*, *>)
                ?.entries
                ?.associate { (level, quota) ->
                    getLogLevel(level as? String) to (quota as? Int ?: 0)
                }
                ?: emptyMap(),
            quotaWindowMillis = (configMap["quotaWindowMillis"] as? Number)?.toLong() ?: 60_000L,
        )
    }
}

fun getRedactionConfig(map: Map<String, Any>?): RedactionConfig? {
    return map?.let { configMap ->
        RedactionConfig(
            enableBuiltInPatterns = (configMap["enableBuiltInPatterns"] as? List<*>)
                ?.mapNotNull { pattern -> getBuiltInPattern(pattern as? String) }
                ?.toSet()
                ?: emptySet(),
            customRules = (configMap["customRules"] as? List<*>)
                ?.mapNotNull { rule ->
                    (rule as? Map<*, *>)?.let { ruleMap ->
                        RedactionRule(
                            name = ruleMap["name"] as? String ?: "",
                            patternString = ruleMap["patternString"] as? String ?: "",
                            maskType = getMaskType(ruleMap["maskType"] as? String),
                            replacement = ruleMap["replacement"] as? String,
                        )
                    }
                }
                ?: emptyList(),
            defaultMaskType = getMaskType(configMap["defaultMaskType"] as? String),
        )
    }
}

fun getParentPath(path: String): String {
    val parentFile = File(path)
    return "${LogsHelper.savePathProvided}${File.separator}${LogsHelper.exportPathProvided}${File.separator}${parentFile.name}"
}
