package org.saudigitus.emis

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.AndroidEntryPoint
import org.saudigitus.emis.ui.teis.TeiScreen
import org.saudigitus.emis.ui.teis.TeiViewModel
import org.saudigitus.emis.ui.theme.EMISAndroidTheme

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    private val viewModel: TeiViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EMISAndroidTheme(
                darkTheme = false,
                dynamicColor = false
            ) {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TeiScreen(viewModel) {}
                }
            }
        }
    }
}
