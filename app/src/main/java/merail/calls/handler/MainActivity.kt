package merail.calls.handler

import android.Manifest
import android.app.role.RoleManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
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
import merail.tools.permissions.role.RoleRequester
import merail.tools.permissions.role.RoleState
import merail.tools.permissions.special.SpecialPermissionState

class MainActivity : ComponentActivity() {

    private lateinit var specialPermissionRequester: SpecialPermissionRequester
    private lateinit var runtimePermissionRequester: RuntimePermissionRequester
    private lateinit var roleRequester: RoleRequester

    private val specialPermission = Manifest.permission.SYSTEM_ALERT_WINDOW

    private val runtimePermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        arrayOf(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_CALL_LOG,
        )
    } else {
        arrayOf(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG,
        )
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private val rolePermission = RoleManager.ROLE_CALL_SCREENING

    private lateinit var isSpecialPermissionButtonVisible: MutableState<Boolean>
    private lateinit var isRuntimePermissionsButtonVisible: MutableState<Boolean>
    private lateinit var rolePermissionButtonVisible: MutableState<Boolean>

    private val onSpecialPermissionClick = {
        specialPermissionRequester.requestPermission {
            isSpecialPermissionButtonVisible.value = it.second == SpecialPermissionState.DENIED
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
    @RequiresApi(Build.VERSION_CODES.Q)
    private val rolePermissionClick = {
        roleRequester.requestRole {
            rolePermissionButtonVisible.value = it.second == RoleState.DENIED
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Content()
        }

        specialPermissionRequester = SpecialPermissionRequester(
            activity = this,
            requestedPermission = specialPermission,
        )
        runtimePermissionRequester = RuntimePermissionRequester(
            activity = this,
            requestedPermissions = runtimePermissions,
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            roleRequester = RoleRequester(
                activity = this,
                requestedRole = rolePermission,
            )
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

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    rolePermissionButtonVisible = remember {
                        mutableStateOf(
                            roleRequester.isRoleGranted().not()
                        )
                    }
                    Button(
                        onClick = {
                            rolePermissionClick.invoke()
                        },
                        text = "Get role permissions",
                        isVisible = rolePermissionButtonVisible.value,
                    )
                }
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

            Button(
                onClick = { },
                text = "Get call role permissions",
                isVisible = true,
            )
        }
    }
}