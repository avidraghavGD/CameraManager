package com.raghav.cameramanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.raghav.cameramanager.ui.theme.CameraManagerTheme

class MainActivity : ComponentActivity() {
    // private lateinit var cameraManager: CameraManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CameraManagerTheme {
                CameraApp()
            }
        }
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun CameraApp(modifier: Modifier = Modifier) {
        val permissionManager = remember { CameraManager() }
        val permissionState by permissionManager.state.collectAsStateWithLifecycle()
        HandleCameraPermission(permissionManager = permissionManager, modifier = modifier)

        Box {
            if (permissionState.permissionState?.hasPermission == true) {
                CameraScreen(modifier)
            } else {
                Button(onClick = { permissionManager.updateState(CameraManager.PermissionManagerState.PermissionRequested) }) {
                    Text(text = "Request Camera Permission", modifier)
                }
            }
        }
    }
}

@Composable
fun CameraScreen(modifier: Modifier = Modifier) {
    Text(text = "This is Camera Screen", modifier = modifier.fillMaxSize())
}
