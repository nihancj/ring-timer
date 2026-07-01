package com.example.ringtimer

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import com.example.ringtimer.data.CallLogRepository
import com.example.ringtimer.ui.CallHistoryScreen

class MainActivity : ComponentActivity() {

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results.values.all { it }) {
            // proceed — receiver is manifest-registered so it'll start receiving once granted
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 2. Launch the permission request when the app starts
        permissionLauncher.launch(
            arrayOf(Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CALL_LOG)
        )

        val myCallLogRepository = CallLogRepository.getInstance(this)
        setContent {
            CallHistoryScreen(repository = myCallLogRepository)
        }
    }
}