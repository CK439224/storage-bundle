package com.storagebundle.permission

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.storagebundle.R
import com.storagebundle.core.ui.theme.Dimens

/**
 * Gates [content] behind a runtime permission.
 *
 * Two rules from PLAN.md §6 are structural here rather than advisory:
 *
 *  * **Requested at first use, never at launch.** The gate is placed around the feature that
 *    needs the permission, so the request always arrives with visible context.
 *  * **Fail securely.** When permission is absent the feature is unavailable and says so.
 *    There is no silent partial mode that would let the user believe they had seen everything
 *    their library contains.
 *
 * The rationale text states that analysis happens on-device and that the app has no network
 * access — the claim the whole product rests on (PLAN.md §1.3), made at the moment the user
 * is deciding whether to trust it.
 *
 * @param permission the Android permission to require.
 * @param rationaleTitle heading shown when the permission has not been granted.
 * @param rationaleBody explanation of why the permission is needed.
 * @param modifier applied to the rationale layout.
 * @param content shown once the permission is granted.
 */
@Composable
fun PermissionGate(
    permission: String,
    rationaleTitle: String,
    rationaleBody: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }

    var isGranted by rememberSaveable(permission) {
        mutableStateOf(PermissionStatus.isGranted(context, permission))
    }
    var hasBeenRequested by rememberSaveable(permission) { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        isGranted = granted
        hasBeenRequested = true
    }

    if (isGranted) {
        content()
        return
    }

    // Once the system stops offering the dialog, the only remaining route is Settings.
    // Detecting that requires an Activity; without one, keep offering the in-app request
    // rather than sending the user somewhere they may not need to go.
    val deniedPermanently = hasBeenRequested &&
        activity != null &&
        !ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)

    PermissionRationale(
        title = rationaleTitle,
        body = rationaleBody,
        deniedPermanently = deniedPermanently,
        onRequest = { launcher.launch(permission) },
        onOpenSettings = { context.openAppSettings() },
        modifier = modifier,
    )
}

@Composable
private fun PermissionRationale(
    title: String,
    body: String,
    deniedPermanently: Boolean,
    onRequest: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Dimens.SpaceMd),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMd, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )

        if (deniedPermanently) {
            Text(
                text = stringResource(R.string.permission_denied_permanently),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
            )
            Button(onClick = onOpenSettings) {
                Text(stringResource(R.string.permission_open_settings))
            }
        } else {
            Button(onClick = onRequest) {
                Text(stringResource(R.string.permission_grant))
            }
        }
    }
}

/** Opens this app's entry in system settings, where a denied permission can be re-enabled. */
private fun Context.openAppSettings() {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        "package:$packageName".toUri(),
    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)
}

/** Unwraps the [Activity] behind a Compose [Context], or null if there is not one. */
private fun Context.findActivity(): Activity? {
    var current: Context? = this
    while (current is ContextWrapper) {
        if (current is Activity) {
            return current
        }
        current = current.baseContext
    }
    return null
}

/** Reads the current grant state of a permission. */
private object PermissionStatus {
    fun isGranted(context: Context, permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}
