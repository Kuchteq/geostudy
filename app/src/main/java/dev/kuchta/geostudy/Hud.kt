package dev.kuchta.geostudy

import androidx.compose.foundation.background
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng

@Composable
fun Hud(viewModel: GameViewModel = viewModel(), scaffoldPadding : PaddingValues) {
    val currentQuery = viewModel.currentQuery.collectAsState()
    val incorrect = viewModel.incorrect.collectAsState()
    val remaining = viewModel.remainingCountriesCount.collectAsState()
    val selectedCountry = viewModel.selectedCountry.collectAsState()
    val mistake = viewModel.mistakeAcknowledge.collectAsState()

    if (currentQuery.value.name == "") {

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center) {
            MainMenu()
        }
    } else {

        Box(
            modifier = Modifier.fillMaxSize().padding(scaffoldPadding),
            contentAlignment = Alignment.TopCenter) {
            Surface(
                shape = MaterialTheme.shapes.medium,
            ) {
                Text(currentQuery.value.name,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
            }
            Row (
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 10.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                Row {
                    Text("${incorrect.value}",
                        modifier = Modifier
                            .background(color = MaterialTheme.colorScheme.errorContainer, shape = MaterialTheme.shapes.medium)
                            .padding(12.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("${remaining.value}",
                        modifier = Modifier
                            .background(color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium)
                            .padding(12.dp)
                    )
                }
                if(!mistake.value) {
                    LargeFloatingActionButton(onClick = { viewModel.acknowledgeMistake() }) {
                        Icon(Icons.Default.PlayArrow, "Mistake made")  }
                }
                if(selectedCountry.value.id != -1) {
                    LargeFloatingActionButton(onClick = { viewModel.confirmCountry() }) {
                        Icon(Icons.Default.Check, "Confirm pick")  }
                }

            }
            }
    }
}

