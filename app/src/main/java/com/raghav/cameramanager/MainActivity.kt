package com.raghav.cameramanager

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.raghav.cameramanager.ui.theme.CameraManagerTheme

class MainActivity : ComponentActivity() {
    private lateinit var cameraManager: CameraManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        cameraManager = CameraManager(
            activity = this,
            listener = object : PermissionState {
                override fun onPermissionDeclined() {
                    setContent {
                        CameraManagerTheme {
                            ExitScreen()
                        }
                    }
                }

                override fun onPermissionGranted() {
                    setContent {
                        CameraManagerTheme {
                            CameraApp()
                        }
                    }
                }

                override fun explainRequestReason(
                    isPermissionDenied: Boolean,
                    cameraPermissionLauncher: ActivityResultLauncher<String>,
                ) {
                    setContent {
                        CameraManagerTheme {
                            ShowPermissionRationaleDialog(
                                title = R.string.permission_required_to_use_the_sdk,
                                content = R.string.permission_required_to_use_the_sdk,
                                onDismissClick = {
                                    finish()
                                },
                                onConfirmClick = {
                                    if (isPermissionDenied) {
                                        this@MainActivity.openAppSettings()
                                    } else {
                                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                    }
                                },
                            )
                        }
                    }
                }

                override fun permanentlyDenied() {
                    finish()
                }
            },
        )
    }
}

@Composable
fun CameraApp(modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier.fillMaxWidth()) {
        item {
            Button(onClick = { /*TODO*/ }) {
                Text(text = "Open Camera")
            }
        }
    }
}

@Composable
fun ExitScreen(modifier: Modifier = Modifier) {
    Text(text = "Can't open SDK", modifier = modifier.fillMaxSize())
}

fun Activity.openAppSettings() {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", this.packageName, null),
    )
    this.startActivity(intent)
}
