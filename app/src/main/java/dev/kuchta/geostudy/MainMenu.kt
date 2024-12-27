package dev.kuchta.geostudy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun MainMenu(viewModel: GameViewModel = viewModel()) {

    val currentQuery = viewModel.currentQuery.collectAsState()
    Surface(
        color = MaterialTheme.colorScheme.background, // Background color from the Material theme
        shape = MaterialTheme.shapes.medium,
    ) {

        Column(verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Chose continent!",
            )
            Button(onClick = {
                viewModel.selectContinent("Europe")
            }) {
                Text("Europe")
            }

            Button(onClick = {
                viewModel.selectContinent("Asia")
            }) {
                Text("Asia")
            }

            Button(onClick = {}) {
                Text("North America")
            }

            Button(onClick = {}) {
                Text("South Africa")
            }

            Button(onClick = {}) {
                Text("Africa")
            }

            Button(onClick = {}) {
                Text("Oceania")
            }
        }
    }
}