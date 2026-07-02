package com.example.ringtimer.reciever

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import com.example.ringtimer.data.CallLogRepository
import kotlin.math.round

class CallStateReceiver : BroadcastReceiver() {

    companion object {
        // Persisted across broadcasts within a call — use a simple object,
        // DataStore, or pass through a repository singleton.
        var ringStartTime: Long? = null
        var lastState: String = TelephonyManager.EXTRA_STATE_IDLE
        var incomingNumber: String? = null

        fun estimateRingCount(durationMs: Long, avgRingCycleMs: Double = 3000.0): Int =
            round(durationMs / avgRingCycleMs).toInt()
    }

    override fun onReceive(context: Context, intent: Intent) {
        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE) ?: return
        val number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

        when (state) {
            TelephonyManager.EXTRA_STATE_RINGING -> {
                ringStartTime = System.currentTimeMillis()
                incomingNumber = number
            }

            TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                if (lastState == TelephonyManager.EXTRA_STATE_RINGING) {
                    // Call was answered
                    val duration = ringStartTime?.let { System.currentTimeMillis() - it }
                    if (duration != null) {
                        saveCallEvent(context, incomingNumber, duration, answered = true)
                    }
                }
                // if lastState was IDLE, this is an outgoing call — ignore for ring-time purposes
            }

            TelephonyManager.EXTRA_STATE_IDLE -> {
                if (lastState == TelephonyManager.EXTRA_STATE_RINGING) {
                    // Call ended while still ringing = missed or rejected
                    val duration = ringStartTime?.let { System.currentTimeMillis() - it }
                    if (duration != null) {
                        saveCallEvent(context, incomingNumber, duration, answered = false)
                    }
                }
                ringStartTime = null
                incomingNumber = null
            }
        }

        lastState = state
    }

    private fun saveCallEvent(context: Context, number: String?, durationMs: Long, answered: Boolean) {
        // Use goAsync() or WorkManager if this write needs to survive receiver teardown.
        CallLogRepository.getInstance(context).logCall(number, durationMs, answered)
    }
}