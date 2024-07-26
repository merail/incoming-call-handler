package merail.calls.handler

import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import merail.calls.handler.ui.theme.IncomingCallHandlerTheme
import merail.calls.handler.ui.theme.Typography
import merail.tools.permissions.SettingsSnackbar
import merail.tools.permissions.role.RoleRequester
import merail.tools.permissions.role.RoleState
import merail.tools.permissions.runtime.RuntimePermissionRequester
import merail.tools.permissions.runtime.RuntimePermissionState
import merail.tools.permissions.special.SpecialPermissionRequester
import merail.tools.permissions.special.SpecialPermissionState
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : ComponentActivity() {

    private lateinit var specialPermissionRequester: SpecialPermissionRequester
    private lateinit var runtimePermissionRequester: RuntimePermissionRequester
    private lateinit var roleRequester: RoleRequester
    private var logger = OperationLogger();

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
    private lateinit var addedNumbersCount: MutableState<Int>;
    private lateinit var logContents: MutableState<String>;
    private lateinit var dialogOpen: MutableState<Boolean>;

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
//                val shared = getSharedPreferences("blocked-numbers", MODE_PRIVATE);
//                System.out.println("here" + shared.toString());

                addedNumbersCount = remember { mutableStateOf(0) }
                logContents = remember {
                    mutableStateOf("")
                }
                dialogOpen = remember { mutableStateOf(false) }
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
                    Button(
                        onClick = {
                            showFileChooser()
                        },
                        text = "Import numbers from a file",
                        isVisible = true
                    )
                    Button(
                        onClick = {
                            importFromUrl()
                        },
                        text = "Import numbers from URL",
                        isVisible = true
                    )
                    Button(
                        onClick = { toggleLog() },
                        text = "Toggle log",
                        isVisible = true
                    )
                    Text("Currently, there are " + addedNumbersCount.value + " imported numbers",
                        Modifier
                            .fillMaxWidth()
                            .defaultMinSize(
                                minWidth = 72.dp,
                            )
                            .padding(8.dp))
                    Text(
                        text = logContents.value,
                        style = Typography.titleSmall,
                        modifier = Modifier
                            .padding(top = 2.dp, start = 4.dp)
                            .verticalScroll(
                                rememberScrollState()
                            )
                    )
                    FileUrlDialog()
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
                    .padding(4.dp),
                contentPadding = PaddingValues(
                    vertical = 8.dp,
                ),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.DarkGray,
                ),
            ) {
                Text(
                    text = text,
                    style = Typography.titleLarge,
                    fontSize = 20.sp
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
                text = "Get role permissions",
                isVisible = true,
            )
            Button(
                onClick = { },
                text = "Toggle log",
                isVisible = true
            )
            Text(
                text = "Log appears here",
                style = Typography.titleSmall,
                modifier = Modifier.padding(top = 2.dp, start = 4.dp)
            )
            FileUrlDialog()
        }
    }

    private fun showFileChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.setType("application/json")
        intent.addCategory(Intent.CATEGORY_OPENABLE)

        try {
            startActivityForResult(Intent.createChooser(intent, "Select a file"), 100)
        } catch (exception: Exception) {
            Toast.makeText(this, "please install a file manager", Toast.LENGTH_SHORT).show()
        }
    }

    private fun importFromUrl() {
    }

    private fun toggleLog() {
        if (logContents.value.length > 0) {
            logContents.value = "";
        } else {
            logContents.value = logger.getLog(applicationContext)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 100 && resultCode === RESULT_OK && data != null) {
            val performanceMarkStart = System.currentTimeMillis()
            val uri: Uri = data.getData()!!;
            val path = uri?.getPath();
//            val file = File(path);
//            val inStream = contentResolver.openInputStream(uri)

            var text = ""

            val stringBuilder = StringBuilder()
            contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    var line: String? = reader.readLine()
                    while (line != null) {
                        stringBuilder.append(line)
                        line = reader.readLine()
                    }
                }
            }

//            txtResult.text = "Path:" + path + "\nFile name:" + file.getName() + " " + file.exists() + stringBuilder.toString();
            val jsonObj = JSONObject(stringBuilder.toString());
            val numbersArray = jsonObj.getJSONArray("numbers")
            val numbersArrayLen = numbersArray.length()
            System.out.println("len is $numbersArrayLen")

            addedNumbersCount.value = numbersArrayLen;

            val filename = "numbers_list"
            applicationContext.openFileOutput(filename, Context.MODE_PRIVATE).use {
                it.write(stringBuilder.toString().toByteArray());
                text = text + " finished in " + (System.currentTimeMillis() - performanceMarkStart) + "ms, got " + numbersArrayLen + " numbers"
                System.out.println( " finished in " + (System.currentTimeMillis() - performanceMarkStart) + "ms, got " + numbersArrayLen + " numbers");
            }

            System.out.println("file saved");

            logger.saveToLog(applicationContext, "Loaded " + numbersArrayLen + " numbers from " + uri);

            // Read the numbers list
//            applicationContext.openFileInput(filename).use {
//                val myString: String = IOUtils.toString(it, "UTF-8");
//
//                val jsonObj = JSONObject(stringBuilder.toString());
//                val numbersArray = jsonObj.getJSONArray("numbers")
//                System.out.println("dupax " + numbersArray)
//            }

//            val stringBuilder2 = StringBuilder()
//            contentResolver.openInputStream(uri)?.use { inputStream ->
//                BufferedReader(InputStreamReader(inputStream)).use { reader ->
//                    var line: String? = reader.readLine()
//                    while (line != null) {
//                        stringBuilder.append(line)
//                        line = reader.readLine()
//                    }
//                }
//            }


            val dbVersionFilename = "db_version"
            // Read current db version
//            try {
//                applicationContext.openFileInput(dbVersionFilename).use {
//                    val dbVersion: String = IOUtils.toString(it, "UTF-8");
//                    System.out.println("dupax " + dbVersion)
//                }
//            } catch (e: Exception) {
//
//            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    @Composable
    fun FileUrlDialog() {
        when {
            dialogOpen.value -> {
                Dialog(onDismissRequest = { dialogOpen.value = false }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(350.dp)
                            .padding(8.dp),
                        shape = RoundedCornerShape(4.dp),
                    ) {
                        Column (
                            Modifier.fillMaxHeight(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                Text(
                                    text = "Enter blocked numbers database URL:",
                                    modifier = Modifier
                                        .wrapContentSize(Alignment.TopCenter)
                                        .padding(top = 16.dp),
                                    fontSize = 20.sp,
                                    textAlign = TextAlign.Center,
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                SimpleFilledTextFieldSample()
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                TextButton(
                                    onClick = {  },
                                    modifier = Modifier.padding(8.dp),
                                ) {
                                    Text("Dismiss")
                                }
                                TextButton(
                                    onClick = {  },
                                    modifier = Modifier.padding(8.dp),
                                ) {
                                    Text("Confirm")
                                }
                            }
                        }
                    }
                }
            }
        }


    }

    @Composable
    fun SimpleFilledTextFieldSample() {
        var text by remember { mutableStateOf("") }

        TextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("") }
        )
    }


    /**
     * Shows [message] in a Toast.
     */
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}