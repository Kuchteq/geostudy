package dev.kuchta.geostudy
import androidx.lifecycle.lifecycleScope
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.CoroutineScope
import org.maplibre.android.MapLibre
import org.maplibre.android.maps.MapView
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.PropertyFactory.fillColor
import org.maplibre.android.style.layers.SymbolLayer
import kotlin.math.cos

class HighlightAnimationController(
    val style: Style,
    val layerId: String,
    val selectedCountry: StateFlow<Country>,
    lifecycleScope: CoroutineScope
) {
    val handler = Handler(Looper.getMainLooper())
    init {
        lifecycleScope.launch {
            selectedCountry.collect { newState ->
                val layer = style.getLayerAs<FillLayer>(layerId)
                layer?.setFilter(
                    Expression.eq(Expression.get("ADM0_A3"),Expression.literal(selectedCountry.value.isoCode))
                )
                handler.post(animationLoop)
            }
        }
    }
    val animationLoop = object : Runnable {
        override fun run() {

            try {
                if (style.getLayer(layerId) == null) {
                    handler.removeCallbacks(this) // Stop updates if layer doesn't exist
                    return
                }

                val time = System.currentTimeMillis() / 1000.0 // Current time in seconds
                val cosValue = cos(time * 2) // Periodic cosine value

                val dynamicColor = when {
                    cosValue < -0.5 -> "#0000FF"
                    cosValue < 0.5 -> "#FFFF00"
                    else -> "#FF0000"
                }

                style.getLayerAs<FillLayer>(layerId)?.withProperties(
                    fillColor(dynamicColor)
                )?.setFilter(
                    Expression.eq(Expression.get("ADM0_A3"),Expression.literal(selectedCountry.value.isoCode))
                )

                handler.postDelayed(this, 120)
            } catch (e: Exception) {
                e.printStackTrace()
                handler.removeCallbacks(this)
            }
        }
    }
}
@Composable
fun MapView2(gameViewModel: GameViewModel = viewModel()) {
    val currentQuery = gameViewModel.currentQuery.collectAsState()
    val mistake = gameViewModel.mistakeAcknowledge.collectAsState()
    val lifecycle = LocalLifecycleOwner.current.lifecycleScope
    val mapState = remember { mutableStateOf<MapLibreMap?>(null) }

    LaunchedEffect(mistake.value) {
        var revealedName = "NonExistant"
        if (mistake.value == false)  {
            mapState.value?.cameraPosition = CameraPosition.Builder()
                .target(LatLng(currentQuery.value.lat, currentQuery.value.lon))
                .zoom(5.0)
                .build()
            revealedName = currentQuery.value.name
        }
        (mapState.value?.style?.getLayer("countries-label") as? SymbolLayer)?.setFilter(
            Expression.eq(Expression.get("NAME"),Expression.literal(revealedName))
        )
    }

    AndroidView(
        factory = { context ->
            MapLibre.getInstance(context)

            val mapView = MapView(context).apply {
                onCreate(null)
            }

            mapView.getMapAsync { map ->
                map.setStyle("https://demotiles.maplibre.org/style.json") { style ->
                    val clickedCountryLayer = FillLayer("clicked-country-layer", "maplibre").apply {
                        withProperties(
                            fillColor("rgba(0, 255, 0, 1)")
                        )
                        withFilter(
                            Expression.eq(Expression.get("ADM0_A3"), Expression.literal("NonExistent"))
                        )
                        withSourceLayer("countries")
                    }

                    style.addLayer(clickedCountryLayer)

                    HighlightAnimationController(
                        style,
                        "clicked-country-layer",
                        gameViewModel.selectedCountry,
                        lifecycle
                    )
                }

                map.cameraPosition = CameraPosition.Builder()
                    .target(LatLng(0.0, 0.0))
                    .zoom(1.0)
                    .build()

                mapState.value = map

                map.addOnMapClickListener { latLng ->
                    val screenPoint = map.projection.toScreenLocation(latLng)
                    val features = map.queryRenderedFeatures(screenPoint, "countries-fill")

                    if (features.isNotEmpty()) {
                        val code = features[0].getProperty("ADM0_A3").asString
                        gameViewModel.selectCountryByCode(code)
                    } else {
                        println("No features found at the clicked location.")
                    }
                    true
                }
            }

            mapView
        },
        update = { mapView ->
        }
    )
}