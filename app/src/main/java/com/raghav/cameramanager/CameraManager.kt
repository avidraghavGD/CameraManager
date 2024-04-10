package com.raghav.cameramanager

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.annotation.StringRes
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

@OptIn(ExperimentalPermissionsApi::class)
class CameraManager {

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state

    @Stable
    data class State(
        val permissionState: PermissionState? = null,
        val uiAction: UiActions = UiActions.IDLE,
    )

    enum class UiActions {
        REQUEST, SHOW_RATIONALE, IDLE, PERMANENTLY_DENIED
    }

    sealed class PermissionManagerState {
        data object PermissionDenied : PermissionManagerState()
        data object PermissionGranted : PermissionManagerState()
        data object PermissionRequested : PermissionManagerState()
        data object PermissionPermanentlyDenied : PermissionManagerState()
        data object RationaleAgreed : PermissionManagerState()
        data object NavigateToSettings : PermissionManagerState()
        data class PermissionStateUpdated(val permissionsState: PermissionState) :
            PermissionManagerState()
    }

    private fun onPermissionDenied() {
        _state.update { it.copy(uiAction = UiActions.IDLE) }
    }

    private fun onPermissionGranted() {
        _state.update { it.copy(uiAction = UiActions.IDLE) }
    }

    private fun onPermissionRequested() {
        _state.value.permissionState?.let { pmState ->
            val action =
                if (!pmState.hasPermission && !pmState.shouldShowRationale && !pmState.permissionRequested) {
                    UiActions.REQUEST
                } else if (!pmState.hasPermission && pmState.shouldShowRationale) {
                    UiActions.SHOW_RATIONALE
                } else {
                    UiActions.PERMANENTLY_DENIED
                }
            _state.update { it.copy(uiAction = action) }
        }
    }

    private fun onPermissionPermanentlyDenied() {
        _state.update { it.copy(uiAction = UiActions.PERMANENTLY_DENIED) }
    }

    private fun onRationaleAgreed() {
        _state.update { it.copy(uiAction = UiActions.REQUEST) }
    }

    private fun onNavigateToSettings() {
        _state.update { it.copy(uiAction = UiActions.IDLE) }
    }

    private fun onPermissionsStateUpdated(permissionState: PermissionState) {
        _state.update { it.copy(permissionState = permissionState) }
    }

    fun updateState(state: PermissionManagerState) {
        when (state) {
            PermissionManagerState.PermissionDenied -> onPermissionDenied()
            PermissionManagerState.PermissionPermanentlyDenied -> onPermissionPermanentlyDenied()
            PermissionManagerState.RationaleAgreed -> onRationaleAgreed()
            PermissionManagerState.PermissionRequested -> onPermissionRequested()
            PermissionManagerState.NavigateToSettings -> onNavigateToSettings()
            PermissionManagerState.PermissionGranted -> onPermissionGranted()
            is PermissionManagerState.PermissionStateUpdated -> onPermissionsStateUpdated(state.permissionsState)
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HandleCameraPermission(
    permissionManager: CameraManager,
    modifier: Modifier = Modifier,
) {
    val state by permissionManager.state.collectAsStateWithLifecycle()
    val permissionsState = rememberPermissionState(Manifest.permission.CAMERA)

    LaunchedEffect(permissionsState) {
        permissionManager.updateState(
            CameraManager.PermissionManagerState.PermissionStateUpdated(
                permissionsState,
            ),
        )
        when {
            permissionsState.hasPermission -> {
                permissionManager.updateState(CameraManager.PermissionManagerState.PermissionGranted)
            }

            permissionsState.permissionRequested && !permissionsState.shouldShowRationale -> {
                permissionManager.updateState(CameraManager.PermissionManagerState.PermissionPermanentlyDenied)
            }

            else -> {
                permissionManager.updateState(CameraManager.PermissionManagerState.PermissionDenied)
            }
        }
    }

    PermissionsHandlerDialog(
        uiAction = state.uiAction,
        permissionState = state.permissionState,
        onRationaleAgreed = { permissionManager.updateState(CameraManager.PermissionManagerState.RationaleAgreed) },
        navigateToSettings = { permissionManager.updateState(CameraManager.PermissionManagerState.NavigateToSettings) },
        modifier = modifier,
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionsHandlerDialog(
    uiAction: CameraManager.UiActions,
    permissionState: PermissionState?,
    onRationaleAgreed: () -> Unit,
    navigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    when (uiAction) {
        CameraManager.UiActions.REQUEST -> {
            LaunchedEffect(true) {
                permissionState?.launchPermissionRequest()
            }
        }

        CameraManager.UiActions.SHOW_RATIONALE -> {
            AlertDialogPopup(
                title = R.string.permission_rationale_title,
                content = R.string.permission_rationale,
                onConfirmClick = onRationaleAgreed,
                onDismissClick = {},
                modifier = modifier,
            )
        }

        CameraManager.UiActions.PERMANENTLY_DENIED -> {
            AlertDialogPopup(
                title = R.string.allow_permission,
                content = R.string.settings_screen_info,
                onConfirmClick = {
                    navigateToSettings()
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:" + context.packageName)
                        context.startActivity(this)
                    }
                },
                onDismissClick = {},
                modifier = modifier,
            )
        }

        CameraManager.UiActions.IDLE -> {
        }
    }
}

@Composable
fun AlertDialogPopup(
    @StringRes
    title: Int,
    @StringRes
    content: Int,
    modifier: Modifier = Modifier,
    onConfirmClick: () -> Unit,
    onDismissClick: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = {
            onDismissClick()
        },
        title = {
            Text(
                text = stringResource(title),
            )
        },
        text = {
            Text(
                text = stringResource(content),
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirmClick() }) {
                Text(
                    text = stringResource(R.string.ok),
                )
            }
        },
        modifier = modifier,
    )
}
