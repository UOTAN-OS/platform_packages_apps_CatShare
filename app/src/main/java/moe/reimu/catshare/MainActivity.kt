package moe.reimu.catshare

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.format.Formatter
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import moe.reimu.catshare.services.GattServerService
import moe.reimu.catshare.services.P2pReceiverService
import moe.reimu.catshare.ui.theme.CatShareTheme
import moe.reimu.catshare.utils.INTERNAL_BROADCAST_PERMISSION
import moe.reimu.catshare.utils.ServiceState
import moe.reimu.catshare.utils.registerInternalBroadcastReceiver
import org.uwuaosp.compose.settingslib.PreferenceGroupSpacer
import org.uwuaosp.compose.settingslib.PreferencePosition
import org.uwuaosp.compose.settingslib.PreferenceRow
import org.uwuaosp.compose.settingslib.SettingsCategory
import org.uwuaosp.compose.settingslib.SettingsHomepageIcon
import org.uwuaosp.compose.settingslib.SettingsScaffold
import org.uwuaosp.compose.settingslib.SettingsToolbarActionButton
import org.uwuaosp.compose.settingslib.SwitchPreferenceRow
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
    var checked by remember { mutableStateOf(false) }
    var receiveCardStates by remember { mutableStateOf<List<ReceiveCardState>>(emptyList()) }

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
    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action != P2pReceiverService.ACTION_RECEIVE_CARD_UPDATE) return
                receiveCardStates = ReceiveCardState.updateList(intent, receiveCardStates)
            }
        }

        context.registerInternalBroadcastReceiver(
            receiver,
            IntentFilter(P2pReceiverService.ACTION_RECEIVE_CARD_UPDATE),
        )

        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    val pickFilesLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { pickedUris ->
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
        AnimatedVisibility(
            visible = receiveCardStates.isNotEmpty(),
            enter = fadeIn(tween(220)) + expandVertically(
                animationSpec = tween(320, easing = FastOutSlowInEasing),
            ),
            exit = fadeOut(tween(160)) + shrinkVertically(
                animationSpec = tween(260, easing = FastOutSlowInEasing),
            ),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.animateContentSize(tween(280, easing = FastOutSlowInEasing)),
            ) {
                receiveCardStates.forEach { state ->
                    ReceiveTransferCard(
                        state = state,
                        onAccept = {
                            context.sendBroadcast(
                                P2pReceiverService.getAcceptIntent(state.taskId),
                                INTERNAL_BROADCAST_PERMISSION,
                            )
                        },
                        onReject = {
                            context.sendBroadcast(
                                P2pReceiverService.getDismissIntent(state.taskId),
                                INTERNAL_BROADCAST_PERMISSION,
                            )
                            receiveCardStates = receiveCardStates.withoutTask(state.taskId)
                        },
                        onCancel = {
                            context.sendBroadcast(
                                P2pReceiverService.getCancelIntent(state.taskId),
                                INTERNAL_BROADCAST_PERMISSION,
                            )
                        },
                        onClose = {
                            receiveCardStates = receiveCardStates.withoutTask(state.taskId)
                        },
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
            }
        }
        SettingsCategory(title = stringResource(R.string.catshare_category_transfer))
        SwitchPreferenceRow(
            title = stringResource(R.string.discoverable),
            summary = stringResource(R.string.discoverable_desc),
            checked = checked,
            iconContent = {
                SettingsHomepageIcon(iconRes = R.drawable.ic_feature_search)
            },
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
            iconContent = {
                SettingsHomepageIcon(imageVector = Icons.Filled.Share)
            },
            onClick = { pickFilesLauncher.launch(arrayOf("*/*")) },
            position = PreferencePosition.Bottom,
        )
    }
}

private enum class ReceiveCardPhase {
    Asking,
    Receiving,
    Completed,
    Failed,
}

private data class ReceiveCardState(
    val phase: ReceiveCardPhase,
    val taskId: Int,
    val senderName: String,
    val fileName: String,
    val fileCount: Int,
    val totalSize: Long,
    val processedSize: Long,
    val isText: Boolean,
) {
    val progress: Float?
        get() = if (totalSize > 0L) {
            (processedSize.toFloat() / totalSize.toFloat()).coerceIn(0f, 1f)
        } else {
            null
        }

    companion object {
        fun updateList(
            intent: Intent,
            current: List<ReceiveCardState>,
        ): List<ReceiveCardState> {
            val taskId = intent.getIntExtra(P2pReceiverService.EXTRA_RECEIVE_CARD_TASK_ID, -1)
            val previous = current.firstOrNull { it.taskId == taskId } ?: current.firstOrNull()
            val state = fromIntent(intent, previous)
                ?: return current.filterNot { it.phase.isTransient }
            val next = current.filterNot { it.taskId == state.taskId }
            return listOf(state) + next
        }

        private fun fromIntent(intent: Intent, previous: ReceiveCardState?): ReceiveCardState? {
            return when (
                intent.getStringExtra(P2pReceiverService.EXTRA_RECEIVE_CARD_STATE)
            ) {
                P2pReceiverService.RECEIVE_CARD_STATE_ASKING -> ReceiveCardPhase.Asking
                P2pReceiverService.RECEIVE_CARD_STATE_RECEIVING -> ReceiveCardPhase.Receiving
                P2pReceiverService.RECEIVE_CARD_STATE_COMPLETED -> ReceiveCardPhase.Completed
                P2pReceiverService.RECEIVE_CARD_STATE_FAILED -> ReceiveCardPhase.Failed
                else -> return null
            }.let { phase ->
                ReceiveCardState(
                    phase = phase,
                    taskId = intent.getIntExtra(P2pReceiverService.EXTRA_RECEIVE_CARD_TASK_ID, -1),
                    senderName = intent.getStringExtra(
                        P2pReceiverService.EXTRA_RECEIVE_CARD_SENDER_NAME
                    ) ?: previous?.senderName.orEmpty(),
                    fileName = intent.getStringExtra(
                        P2pReceiverService.EXTRA_RECEIVE_CARD_FILE_NAME
                    ) ?: previous?.fileName.orEmpty(),
                    fileCount = intent.getIntExtra(
                        P2pReceiverService.EXTRA_RECEIVE_CARD_FILE_COUNT,
                        previous?.fileCount ?: 0,
                    ),
                    totalSize = intent.getLongExtra(
                        P2pReceiverService.EXTRA_RECEIVE_CARD_TOTAL_SIZE,
                        previous?.totalSize ?: 0L,
                    ),
                    processedSize = intent.getLongExtra(
                        P2pReceiverService.EXTRA_RECEIVE_CARD_PROCESSED_SIZE,
                        previous?.processedSize ?: 0L,
                    ),
                    isText = intent.getBooleanExtra(
                        P2pReceiverService.EXTRA_RECEIVE_CARD_IS_TEXT,
                        previous?.isText ?: false,
                    ),
                )
            }
        }
    }
}

private val ReceiveCardPhase.isTransient: Boolean
    get() = this == ReceiveCardPhase.Asking || this == ReceiveCardPhase.Receiving

private fun List<ReceiveCardState>.withoutTask(taskId: Int) = filterNot { it.taskId == taskId }

@Composable
private fun ReceiveTransferCard(
    state: ReceiveCardState,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onCancel: () -> Unit,
    onClose: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(tween(280, easing = FastOutSlowInEasing)),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceBright,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 28.dp, vertical = 26.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ReceiveCardIcon(state.phase)
                Spacer(modifier = Modifier.width(22.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = when (state.phase) {
                            ReceiveCardPhase.Asking -> stringResource(R.string.receive_card_new_file)
                            ReceiveCardPhase.Receiving -> stringResource(R.string.receive_card_receiving)
                            ReceiveCardPhase.Completed -> stringResource(R.string.receive_card_completed)
                            ReceiveCardPhase.Failed -> stringResource(R.string.receive_card_failed)
                        },
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = receiveCardSubtitle(state),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            when (state.phase) {
                ReceiveCardPhase.Asking -> ReceiveCardDecisionButtons(onReject, onAccept)
                ReceiveCardPhase.Receiving -> ReceiveCardProgress(state, onCancel)
                ReceiveCardPhase.Completed,
                ReceiveCardPhase.Failed -> ReceiveCardCloseButton(onClose)
            }
        }
    }
}

@Composable
private fun ReceiveCardIcon(phase: ReceiveCardPhase) {
    val icon = when (phase) {
        ReceiveCardPhase.Completed -> R.drawable.ic_done
        ReceiveCardPhase.Failed -> R.drawable.ic_warning
        else -> R.drawable.ic_download
    }
    Box(
        modifier = Modifier
            .size(72.dp)
            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(icon),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(34.dp),
        )
    }
}

@Composable
private fun ReceiveCardDecisionButtons(
    onReject: () -> Unit,
    onAccept: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(18.dp, Alignment.End),
    ) {
        OutlinedButton(
            onClick = onReject,
            modifier = Modifier
                .heightIn(min = 52.dp)
                .width(132.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary,
            ),
        ) {
            Text(
                text = stringResource(R.string.reject),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Button(
            onClick = onAccept,
            modifier = Modifier
                .heightIn(min = 52.dp)
                .width(132.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Text(
                text = stringResource(R.string.accept),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun ReceiveCardCloseButton(onClose: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
    ) {
        OutlinedButton(
            onClick = onClose,
            modifier = Modifier
                .heightIn(min = 52.dp)
                .width(132.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary,
            ),
        ) {
            Text(
                text = stringResource(R.string.close),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun ReceiveCardProgress(
    state: ReceiveCardState,
    onCancel: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        val progress = state.progress
        if (progress == null) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        } else {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = receiveCardProgressText(state),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            OutlinedButton(
                onClick = onCancel,
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary,
                ),
            ) {
                Text(
                    text = stringResource(android.R.string.cancel),
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun receiveCardSubtitle(state: ReceiveCardState): String {
    if (state.isText) return stringResource(R.string.noti_request_desc_text)
    val fileName = state.fileName.ifBlank {
        if (state.fileCount > 1) {
            stringResource(R.string.receive_card_multiple_files, state.fileCount)
        } else {
            stringResource(R.string.unknown)
        }
    }
    return if (state.senderName.isBlank()) {
        fileName
    } else {
        "$fileName - ${state.senderName}"
    }
}

@Composable
private fun receiveCardProgressText(state: ReceiveCardState): String {
    val context = LocalContext.current
    return if (state.totalSize > 0L) {
        val processed = Formatter.formatShortFileSize(context, state.processedSize)
        val total = Formatter.formatShortFileSize(context, state.totalSize)
        val percent = ((state.progress ?: 0f) * 100).toInt()
        "$processed / $total | $percent%"
    } else {
        stringResource(R.string.preparing)
    }
}
