package merail.calls.handler

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import merail.calls.handler.ui.theme.IncomingCallHandlerTheme
import merail.tools.permissions.SettingsSnackbar
import merail.tools.permissions.runtime.RuntimePermissionRequester
import merail.tools.permissions.runtime.RuntimePermissionState
import merail.tools.permissions.special.SpecialPermissionRequester
import merail.calls.handler.ui.theme.Typography

class MainActivity : ComponentActivity() {

    private lateinit var specialPermissionRequester: SpecialPermissionRequester
    private lateinit var runtimePermissionRequester: RuntimePermissionRequester

    private val runtimePermissions = arrayOf(
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_CALL_LOG
    )


    private lateinit var isSpecialPermissionButtonVisible: MutableState<Boolean>
    private lateinit var isRuntimePermissionsButtonVisible: MutableState<Boolean>

    private val onSpecialPermissionClick = {
        specialPermissionRequester.requestPermission {
            isSpecialPermissionButtonVisible.value = it.second.not()
        }
    }
    private val onRuntimePermissionsClick = {
        runtimePermissionRequester.requestPermissions {
            isRuntimePermissionsButtonVisible.value = runtimePermissionRequester.areAllPermissionsGranted().not()
            if (it.containsValue(RuntimePermissionState.PERMANENTLY_DENIED)) {
                val settingsOpeningSnackbar = SettingsSnackbar(
                    activity = this,
                    view = window.decorView,
                )
                settingsOpeningSnackbar.showSnackbar(
                    text = "You must grant permissions in Settings!",
                    actionName = "Settings",
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Content()
        }

        specialPermissionRequester = SpecialPermissionRequester(
            activity = this,
            requestedPermission = Manifest.permission.SYSTEM_ALERT_WINDOW,
        )
        runtimePermissionRequester = RuntimePermissionRequester(
            activity = this,
            requestedPermissions = runtimePermissions,
        )

        if (!specialPermissionRequester.isPermissionGranted()) {
            onSpecialPermissionClick.invoke()
        }
        if (!runtimePermissionRequester.areAllPermissionsGranted()) {
            onRuntimePermissionsClick.invoke()
        }
    }

    @Composable
    private fun Content() {
        IncomingCallHandlerTheme {
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize(),
            ) {
                isSpecialPermissionButtonVisible = remember {
                    mutableStateOf(
                        specialPermissionRequester.isPermissionGranted().not()
                    )
                }
                Button(
                    onClick = {
                        onSpecialPermissionClick.invoke()
                    },
                    text = "Get special permission",
                    isVisible = isSpecialPermissionButtonVisible.value,
                )

                isRuntimePermissionsButtonVisible = remember {
                    mutableStateOf(
                        runtimePermissionRequester.areAllPermissionsGranted().not()
                    )
                }
                Button(
                    onClick = {
                        onRuntimePermissionsClick.invoke()
                    },
                    text = "Get runtime permissions",
                    isVisible = isRuntimePermissionsButtonVisible.value,
                )
            }
        }
    }

    @Composable
    private fun Button(
        onClick: () -> Unit,
        text: String,
        isVisible: Boolean,
    ) {
        if (isVisible) {
            Button(
                onClick = {
                    onClick()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(
                        minWidth = 72.dp,
                    )
                    .padding(12.dp),
                contentPadding = PaddingValues(
                    vertical = 20.dp,
                ),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                ),
            ) {
                Text(
                    text = text,
                    style = Typography.titleLarge,
                )
            }
        }
    }

    @Preview(
        showBackground = true,
    )
    @Composable
    private fun ContentPreview() {
        Column {
            Button(
                onClick = { },
                text = "Get special permissions",
                isVisible = true,
            )

            Button(
                onClick = { },
                text = "Get runtime permissions",
                isVisible = true,
            )
        }
    }
}