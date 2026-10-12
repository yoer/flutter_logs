package com.flutter.logs.plogs.flutter_logs_yoer

import com.blackbox.plog.pLogs.formatter.FormatType
import com.blackbox.plog.pLogs.models.LogLevel
import com.blackbox.plog.pLogs.redaction.BuiltInPattern
import com.blackbox.plog.pLogs.redaction.MaskType
import io.flutter.plugin.common.MethodCall
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UtilsTest {

    @Test
    fun mapsEveryFormatTypeAndUsesCurlyForUnknownValues() {
        assertEquals(FormatType.FORMAT_CURLY, getFormatType("FORMAT_CURLY"))
        assertEquals(FormatType.FORMAT_SQUARE, getFormatType("FORMAT_SQUARE"))
        assertEquals(FormatType.FORMAT_CSV, getFormatType("FORMAT_CSV"))
        assertEquals(FormatType.FORMAT_CUSTOM, getFormatType("FORMAT_CUSTOM"))
        assertEquals(FormatType.FORMAT_CURLY, getFormatType("UNKNOWN"))
        assertEquals(FormatType.FORMAT_CURLY, getFormatType(null))
    }

    @Test
    fun mapsEveryMaskTypeAndUsesFullMaskForUnknownValues() {
        assertEquals(MaskType.FULL_MASK, getMaskType("FULL_MASK"))
        assertEquals(MaskType.HASH, getMaskType("HASH"))
        assertEquals(MaskType.PARTIAL, getMaskType("PARTIAL"))
        assertEquals(MaskType.FULL_MASK, getMaskType("UNKNOWN"))
        assertEquals(MaskType.FULL_MASK, getMaskType(null))
    }

    @Test
    fun mapsBuiltInPatternsAndIgnoresUnknownValues() {
        assertEquals(BuiltInPattern.PHONE_NUMBER, getBuiltInPattern("PHONE_NUMBER"))
        assertEquals(BuiltInPattern.EMAIL, getBuiltInPattern("EMAIL"))
        assertEquals(BuiltInPattern.CREDIT_CARD, getBuiltInPattern("CREDIT_CARD"))
        assertEquals(BuiltInPattern.JWT_TOKEN, getBuiltInPattern("JWT_TOKEN"))
        assertEquals(BuiltInPattern.IP_ADDRESS, getBuiltInPattern("IP_ADDRESS"))
        assertNull(getBuiltInPattern("UNKNOWN"))
        assertNull(getBuiltInPattern(null))
    }

    @Test
    fun parsesBackpressureSettingsAndUsesDefaultsForMissingFields() {
        val parsed = getBackpressureConfig(
            mapOf(
                "queueCapacity" to 12,
                "warnQueueCapacity" to 20,
                "perLevelQuotas" to mapOf("ERROR" to 3, "UNKNOWN" to 1),
                "quotaWindowMillis" to 321,
            ),
        )

        assertEquals(12, parsed?.queueCapacity)
        assertEquals(20, parsed?.warnQueueCapacity)
        assertEquals(mapOf(LogLevel.ERROR to 3, LogLevel.INFO to 1), parsed?.perLevelQuotas)
        assertEquals(321L, parsed?.quotaWindowMillis)

        val defaults = getBackpressureConfig(emptyMap())
        assertEquals(500, defaults?.queueCapacity)
        assertEquals(750, defaults?.warnQueueCapacity)
        assertEquals(60_000L, defaults?.quotaWindowMillis)
        assertNull(getBackpressureConfig(null))
    }

    @Test
    fun parsesLongQuotaWindowsAndValidatesMetaInfoLabels() {
        assertEquals(
            5_000_000_000L,
            getBackpressureConfig(mapOf("quotaWindowMillis" to 5_000_000_000L))?.quotaWindowMillis,
        )

        assertEquals(
            mapOf("team" to "platform"),
            getStringMapValueById("labels", MethodCall("test", mapOf("labels" to mapOf("team" to "platform")))),
        )
        assertEquals(
            emptyMap<String, String>(),
            getStringMapValueById("labels", MethodCall("test", mapOf("labels" to "legacy-label"))),
        )
        org.junit.Assert.assertThrows(IllegalArgumentException::class.java) {
            getStringMapValueById("labels", MethodCall("test", mapOf("labels" to mapOf(1 to "bad"))))
        }
        org.junit.Assert.assertThrows(IllegalArgumentException::class.java) {
            getStringMapValueById("labels", MethodCall("test", mapOf("labels" to mapOf("team" to 1))))
        }
        org.junit.Assert.assertThrows(IllegalArgumentException::class.java) {
            getStringMapValueById("labels", MethodCall("test", mapOf("labels" to listOf("invalid"))))
        }
    }

    @Test
    fun parsesRedactionSettingsAndIgnoresUnknownPatternNames() {
        val parsed = getRedactionConfig(
            mapOf(
                "enableBuiltInPatterns" to listOf("EMAIL", "UNKNOWN"),
                "customRules" to listOf(
                    mapOf(
                        "name" to "account",
                        "patternString" to "\\d{4}",
                        "maskType" to "HASH",
                        "replacement" to "[hidden]",
                    ),
                    mapOf("name" to "defaulted", "patternString" to "x"),
                ),
                "defaultMaskType" to "PARTIAL",
            ),
        )

        assertEquals(setOf(BuiltInPattern.EMAIL), parsed?.enableBuiltInPatterns)
        assertEquals(MaskType.PARTIAL, parsed?.defaultMaskType)
        assertEquals(2, parsed?.customRules?.size)
        assertEquals("account", parsed?.customRules?.get(0)?.name)
        assertEquals("\\d{4}", parsed?.customRules?.get(0)?.patternString)
        assertEquals(MaskType.HASH, parsed?.customRules?.get(0)?.maskType)
        assertEquals("[hidden]", parsed?.customRules?.get(0)?.replacement)
        assertEquals(MaskType.FULL_MASK, parsed?.customRules?.get(1)?.maskType)
        assertEquals(MaskType.FULL_MASK, getRedactionConfig(emptyMap())?.defaultMaskType)
        assertNull(getRedactionConfig(null))
    }
}
