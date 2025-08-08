package org.saudigitus.emis.ui.form.fields

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import org.dhis2.form.R
import timber.log.Timber

@Composable
fun IntentAction(
    action: String,
    value: String,
) {
    val context = LocalContext.current

    val intent = Intent(action).apply {
        when (action) {
            Intent.ACTION_DIAL -> {
                data = Uri.parse("tel:$value")
            }
            Intent.ACTION_SENDTO -> {
                data = Uri.parse("mailto:$value")
            }
        }
    }

    val title = stringResource(R.string.open_with)
    val chooser = Intent.createChooser(intent, title)

    try {
        context.startActivity(chooser)
    } catch (e: ActivityNotFoundException) {
        Timber.e("No activity found that can handle this action")
    }
}
