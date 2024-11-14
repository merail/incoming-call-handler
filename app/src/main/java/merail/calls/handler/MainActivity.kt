package merail.calls.handler

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {

    val runtimePermissionsLauncher = registerForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        callback = {},
    )

    override fun onCreate(savedInstanceState: Bundle?) {

        enableEdgeToEdge()

        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Content()
                }
            }
        }
    }
}

@Composable
private fun MainActivity.Content() {
    Column(
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize(),
    ) {
        if (isSpecialPermissionButtonVisible.not() &&
            isRuntimePermissionsButtonVisible.not()) {
            Text(
                text = "All permissions are granted.\nJust use it!",
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(24.dp),
            )
        } else {
            if (isSpecialPermissionButtonVisible) {
                Button(
                    onClick = {
                        requestSpecialPermission()
                    },
                    text = "Get special permission",
                )
            }

            if (isRuntimePermissionsButtonVisible) {
                Button(
                    onClick = {
                        requestRuntimePermissions()
                    },
                    text = "Get runtime permissions",
                )
            }
        }
    }
}

@Composable
private fun Button(
    onClick: () -> Unit,
    text: String,
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        contentPadding = PaddingValues(
            vertical = 20.dp,
        ),
    ) {
        Text(
            text = text,
            fontSize = 24.sp,
        )
    }
}

val MainActivity.isSpecialPermissionButtonVisible
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        Settings.canDrawOverlays(this).not()
    } else {
        false
    }

val MainActivity.isRuntimePermissionsButtonVisible
    get() = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.READ_PHONE_STATE,
    ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CALL_LOG,
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS,
            ) != PackageManager.PERMISSION_GRANTED

fun MainActivity.requestSpecialPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName"),
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        startActivity(intent)
    }
}

fun MainActivity.requestRuntimePermissions() {
    runtimePermissionsLauncher.launch(
        input = arrayOf(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_CALL_LOG,
        ),
    )
}