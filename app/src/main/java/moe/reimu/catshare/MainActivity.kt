package moe.reimu.catshare

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.launch
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import moe.reimu.catshare.services.GattServerService
import moe.reimu.catshare.ui.theme.CatShareTheme
import moe.reimu.catshare.utils.ServiceState
import moe.reimu.catshare.utils.TAG
import moe.reimu.catshare.utils.registerInternalBroadcastReceiver
import org.uwuaosp.compose.settingslib.MainSwitchPreference
import org.uwuaosp.compose.settingslib.PreferenceGroupSpacer
import org.uwuaosp.compose.settingslib.PreferencePosition
import org.uwuaosp.compose.settingslib.PreferenceRow
import org.uwuaosp.compose.settingslib.SettingsCategory
import org.uwuaosp.compose.settingslib.SettingsScaffold
import org.uwuaosp.compose.settingslib.SettingsToolbarActionButton
import org.uwuaosp.compose.settingslib.SwitchPreferenceRow
import rikka.shizuku.Shizuku
import java.util.ArrayList

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkAndRequestPermissions()

        enableEdgeToEdge()
        setContent {
            CatShareTheme {
                MainActivityContent()
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(
                this, Manifest.permission.NEARBY_WIFI_DEVICES
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.NEARBY_WIFI_DEVICES)
        }

        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        if (Build.VERSION.SDK_INT <= 32 && ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (Build.VERSION.SDK_INT >= 31) {
            for (perm in listOf(
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )) {
                if (ContextCompat.checkSelfPermission(
                        this, perm
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    permissionsToRequest.add(perm)
                }
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissions(permissionsToRequest.toTypedArray(), 0)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray, deviceId: Int
    ) {
        for ((name, status) in permissions.zip(grantResults.toList())) {
            if (status == PackageManager.PERMISSION_GRANTED) {
                continue
            }

            Toast.makeText(this, "$name not granted", Toast.LENGTH_LONG).show()
            finish()

            return
        }
    }
}

@Composable
fun MainActivityContent() {
    var preferencesEnabled by remember { mutableStateOf(true) }
    var checked by remember { mutableStateOf(false) }

    val context = LocalContext.current
    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == ServiceState.ACTION_UPDATE_RECEIVER_STATE) {
                    checked = intent.getBooleanExtra("isRunning", false)
                }
            }
        }

        context.registerInternalBroadcastReceiver(
            receiver,
            IntentFilter(ServiceState.ACTION_UPDATE_RECEIVER_STATE),
        )
        context.sendBroadcast(ServiceState.getQueryIntent())

        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    val localMacAddressGranted = remember {
        context.checkSelfPermission("android.permission.LOCAL_MAC_ADDRESS") == PackageManager.PERMISSION_GRANTED
    }

    var shizukuGranted by remember {
        mutableStateOf(false)
    }

    var shizukuAvailable by remember {
        mutableStateOf(false)
    }

    DisposableEffect(Unit) {
        val permissionListener = Shizuku.OnRequestPermissionResultListener { _, grantResult ->
            Log.d(TAG, "Shizuku grant result: $grantResult")
            shizukuGranted = grantResult == PackageManager.PERMISSION_GRANTED
        }

        val binderRecvListener = Shizuku.OnBinderReceivedListener {
            shizukuAvailable = true
            shizukuGranted = Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        }

        val binderDeadReceiver = Shizuku.OnBinderDeadListener {
            shizukuAvailable = false
        }

        Shizuku.addRequestPermissionResultListener(permissionListener)
        Shizuku.addBinderReceivedListenerSticky(binderRecvListener)
        Shizuku.addBinderDeadListener(binderDeadReceiver)

        onDispose {
            Shizuku.removeRequestPermissionResultListener(permissionListener)
            Shizuku.removeBinderReceivedListener(binderRecvListener)
            Shizuku.removeBinderDeadListener(binderDeadReceiver)
        }
    }

    val pickFilesLauncher = rememberLauncherForActivityResult(ChooseFilesContract()) { pickedUris ->
        if (pickedUris.isNotEmpty()) {
            val intent = Intent(context, ShareActivity::class.java)
                .putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(pickedUris))
            context.startActivity(intent)
        }
    }

    SettingsScaffold(
        title = stringResource(R.string.app_name),
        showBackButton = false,
        actions = {
            SettingsToolbarActionButton(
                imageVector = Icons.Filled.Settings,
                contentDescription = stringResource(R.string.title_activity_settings),
                onClick = { context.startActivity(Intent(context, SettingsActivity::class.java)) },
            )
        },
    ) {
        MainSwitchPreference(
            checked = preferencesEnabled,
            onCheckedChange = {
                preferencesEnabled = it
                if (!it) {
                    GattServerService.stop(context)
                }
            },
        )
        Spacer(modifier = Modifier.height(8.dp))
        SettingsCategory(title = stringResource(R.string.catshare_category_transfer))
        SwitchPreferenceRow(
            title = stringResource(R.string.discoverable),
            summary = stringResource(R.string.discoverable_desc),
            checked = checked,
            enabled = preferencesEnabled,
            icon = ImageVector.vectorResource(R.drawable.ic_feature_search),
            onCheckedChange = {
                if (it) {
                    GattServerService.start(context)
                } else {
                    GattServerService.stop(context)
                }
            },
            position = PreferencePosition.Top,
        )
        PreferenceGroupSpacer()
        PreferenceRow(
            title = stringResource(R.string.send),
            summary = stringResource(R.string.send_desc),
            icon = Icons.Filled.Share,
            onClick = { pickFilesLauncher.launch() },
            enabled = preferencesEnabled,
            position = if (localMacAddressGranted) {
                PreferencePosition.Bottom
            } else {
                PreferencePosition.Middle
            },
        )

        if (!localMacAddressGranted) {
            PreferenceGroupSpacer()
            PreferenceRow(
                title = stringResource(
                    if (shizukuAvailable) {
                        if (shizukuGranted) {
                            R.string.shizuku_available
                        } else {
                            R.string.shizuku_not_granted
                        }
                    } else {
                        R.string.shizuku_unavailable
                    }
                ),
                summary = stringResource(R.string.shizuku_desc),
                icon = if (shizukuAvailable && shizukuGranted) {
                    ImageVector.vectorResource(R.drawable.ic_done)
                } else {
                    ImageVector.vectorResource(R.drawable.ic_close)
                },
                enabled = preferencesEnabled,
                onClick = {
                    if (!shizukuGranted) {
                        try {
                            Shizuku.requestPermission(0)
                        } catch (e: Throwable) {
                            e.printStackTrace()
                        }
                    }
                },
                position = PreferencePosition.Bottom,
    )
}

class ChooseFilesContract : ActivityResultContract<Void?, List<Uri>>() {
    override fun createIntent(context: Context, input: Void?): Intent {
        val cf = Intent(Intent.ACTION_GET_CONTENT)
            .setType("*/*")
            .addCategory(Intent.CATEGORY_OPENABLE)
            .putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        return Intent.createChooser(cf, context.getString(R.string.choose_files))
    }

    override fun getSynchronousResult(
        context: Context,
        input: Void?
    ): SynchronousResult<List<Uri>>? =
        null

    override fun parseResult(resultCode: Int, intent: Intent?): List<Uri> {
        if (intent == null) {
            return emptyList()
        }

        val ret = mutableListOf<Uri>()

        val clipData = intent.clipData
        if (clipData != null) {
            for (i in 0..<clipData.itemCount) {
                clipData.getItemAt(i).uri?.let {
                    ret.add(it)
                }
            }
        } else {
            intent.data?.let {
                ret.add(it)
            }
        }

        return ret
    }
}
