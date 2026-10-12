package com.flutter.logs.plogs.flutter_logs_yoer

import io.flutter.plugin.common.MethodChannel

internal class PluginCallbacks(private val postToMainThread: (Runnable) -> Unit) {

    fun dispatch(callback: () -> Unit) {
        postToMainThread(Runnable(callback))
    }

    fun success(result: MethodChannel.Result, value: Any?) {
        dispatch { result.success(value) }
    }

    fun error(result: MethodChannel.Result, code: String, message: String?, details: Any?) {
        dispatch { result.error(code, message, details) }
    }

    fun completeAfterScheduling(result: MethodChannel.Result, scheduleWork: () -> Unit) {
        try {
            scheduleWork()
            success(result, null)
        } catch (error: Exception) {
            error(result, "flutter_logs_schedule_failed", error.message, null)
        }
    }
}

internal class MethodCallDelivery(
    private val onSuccess: (Any?) -> Unit,
    private val onError: (String, String?, Any?) -> Unit,
    private val onNotImplemented: () -> Unit,
) : MethodChannel.Result {
    override fun success(result: Any?) = onSuccess(result)

    override fun error(errorCode: String, errorMessage: String?, errorDetails: Any?) {
        onError(errorCode, errorMessage, errorDetails)
    }

    override fun notImplemented() = onNotImplemented()
}

internal fun clearPluginChannelHandlers(
    clearMethodCallHandler: () -> Unit,
    clearEventStreamHandler: () -> Unit,
    clearReferences: () -> Unit,
) {
    clearMethodCallHandler()
    clearEventStreamHandler()
    clearReferences()
}
