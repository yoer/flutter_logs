package com.flutter.logs.plogs.flutter_logs_yoer

import io.flutter.plugin.common.MethodChannel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PluginCallbacksTest {

    @Test
    fun postsSuccessAndErrorResultsToInjectedMainThreadQueue() {
        val pending = mutableListOf<Runnable>()
        val callbacks = PluginCallbacks { pending.add(it) }
        val result = RecordingResult()
        val callbackEvents = mutableListOf<String>()

        callbacks.dispatch { callbackEvents.add("native callback") }
        callbacks.success(result, "finished")
        assertTrue(callbackEvents.isEmpty())
        assertTrue(result.events.isEmpty())
        pending.removeAt(0).run()
        assertEquals(listOf("native callback"), callbackEvents)
        assertTrue(result.events.isEmpty())
        pending.removeAt(0).run()
        assertEquals(listOf("success:finished"), result.events)

        callbacks.error(result, "export_error", "failed", "details")
        assertEquals(listOf("success:finished"), result.events)
        pending.removeAt(0).run()
        assertEquals(listOf("success:finished", "error:export_error:failed:details"), result.events)
    }

    @Test
    fun completesBothFilteredMethodResultsAfterAsyncWorkHasBeenScheduled() {
        listOf("exportFilteredLogs", "printFilteredLogs").forEach { method ->
            val result = RecordingResult()
            val events = mutableListOf<String>()
            val pending = mutableListOf<Runnable>()
            val callbacks = PluginCallbacks { pending.add(it) }

            callbacks.completeAfterScheduling(result) { events.add("$method scheduled") }

            assertEquals(listOf("$method scheduled"), events)
            assertTrue(result.events.isEmpty())
            pending.single().run()
            assertEquals(listOf("success:null"), result.events)
        }
    }

    @Test
    fun clearsMethodAndEventHandlersBeforeDroppingChannelReferences() {
        val events = mutableListOf<String>()

        clearPluginChannelHandlers(
            clearMethodCallHandler = { events.add("method") },
            clearEventStreamHandler = { events.add("event") },
            clearReferences = { events.add("references") },
        )

        assertEquals(listOf("method", "event", "references"), events)
    }

    @Test
    fun reportsNativeCallbackDeliverySuccessErrorAndMissingHandler() {
        val events = mutableListOf<String>()
        val delivery = MethodCallDelivery(
            onSuccess = { events.add("success:$it") },
            onError = { code, message, details -> events.add("error:$code:$message:$details") },
            onNotImplemented = { events.add("notImplemented") },
        )

        delivery.success("delivered")
        delivery.error("dart_error", "failed", "details")
        delivery.notImplemented()

        assertEquals(
            listOf("success:delivered", "error:dart_error:failed:details", "notImplemented"),
            events,
        )
    }

    @Test
    fun reportsSchedulingErrorsInsteadOfLeavingMethodCallPending() {
        val result = RecordingResult()
        val pending = mutableListOf<Runnable>()
        val callbacks = PluginCallbacks { pending.add(it) }

        callbacks.completeAfterScheduling(result) { throw IllegalStateException("queue failed") }
        pending.single().run()

        assertEquals(
            listOf("error:flutter_logs_schedule_failed:queue failed:null"),
            result.events,
        )
    }

    private class RecordingResult : MethodChannel.Result {
        val events = mutableListOf<String>()

        override fun success(result: Any?) {
            events.add("success:$result")
        }

        override fun error(errorCode: String, errorMessage: String?, errorDetails: Any?) {
            events.add("error:$errorCode:$errorMessage:$errorDetails")
        }

        override fun notImplemented() {
            events.add("notImplemented")
        }
    }
}
