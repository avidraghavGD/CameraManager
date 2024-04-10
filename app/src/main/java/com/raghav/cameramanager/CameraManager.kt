package com.raghav.cameramanager

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat

class CameraManager(private val activity: ComponentActivity, listener: PermissionState) {

    companion object {
        private const val CAMERA_PERMISSION = Manifest.permission.CAMERA
    }

    private var _listener: PermissionState
    private var cameraPermissionLauncher: ActivityResultLauncher<String>

    init {
        _listener = listener
        cameraPermissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { isGranted ->
            if (isGranted) {
                _listener.onPermissionGranted()
            } else {
                isCameraPermissionDeclined = true

//                if (activity.shouldShowRequestPermissionRationale(CAMERA_PERMISSION)) {
//                    _listener.explainRequestReason()
//                } else {
//                    _listener.permanentlyDenied()
//                }
            }
        }
        requestCameraPermission()
    }

    private var isCameraPermissionAvailable = false
    private var isCameraPermissionDeclined = false
    private var showCameraPermissionRationale = false

    private fun isCameraPermissionAvailable(): Boolean {
        isCameraPermissionAvailable = ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.CAMERA,
        ) == PackageManager.PERMISSION_GRANTED
        return isCameraPermissionAvailable
    }

    private fun requestCameraPermission() {
        if (isCameraPermissionAvailable()) {
            _listener.onPermissionGranted()
        } else {
            cameraPermissionLauncher.launch(CAMERA_PERMISSION)
//            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, CAMERA_PERMISSION)) {
//                showCameraPermissionRationale = true
//                _listener.explainRequestReason(isCameraPermissionDeclined, cameraPermissionLauncher)
//            } else {
//                cameraPermissionLauncher.launch(CAMERA_PERMISSION)
//            }
        }
    }
}

@Composable
fun ShowPermissionRationaleDialog(
    @StringRes
    title: Int,
    @StringRes
    content: Int,
    modifier: Modifier = Modifier,
    onDismissClick: () -> Unit,
    onConfirmClick: () -> Unit,
) {
    AlertDialog(
        onDismissClick = onDismissClick,
        onConfirmClick = onConfirmClick,
        title = title,
        content = content,
        modifier = modifier,
    )
}

@Composable
fun AlertDialog(
    @StringRes
    title: Int,
    @StringRes
    content: Int,
    modifier: Modifier = Modifier,
    onDismissClick: () -> Unit,
    onConfirmClick: () -> Unit,
) {
    var openDialog: Boolean by remember {
        mutableStateOf(true)
    }

    if (openDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = {
                // Callback when the user clicks anywhere outside the dialog
                // or presses the back button
                // note: this is not called when the Dismiss button is clicked
                onDismissClick()
                openDialog = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onConfirmClick()
                    },
                ) {
                    Text("Allow")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onDismissClick()
                        openDialog = false
                    },
                ) {
                    Text("Dismiss")
                }
            },
            title = { Text(text = stringResource(title)) },
            text = {
                Text(
                    text = stringResource(content),
                )
            },
            modifier = modifier,
        )
    }
}

interface PermissionState {
    fun onPermissionDeclined()
    fun onPermissionGranted()
    fun explainRequestReason(
        isPermissionDenied: Boolean,
        cameraPermissionLauncher: ActivityResultLauncher<String>,
    )

    fun permanentlyDenied()
}
