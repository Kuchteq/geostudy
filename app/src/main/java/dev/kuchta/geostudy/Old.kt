package dev.kuchta.geostudy
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import org.maplibre.android.MapLibre
import org.maplibre.android.maps.MapView
import dev.kuchta.geostudy.R
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.Projection
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.PropertyFactory.fillColor
import org.maplibre.android.style.layers.PropertyFactory.fillOpacity
import kotlin.math.cos

fun startFillColorAnimation(style: Style, layerId: String) {
    val handler = Handler(Looper.getMainLooper())

    val updateTask = object : Runnable {
        override fun run() {
            try {
                // Step 4: Check if the layer exists to prevent crashes
                if (style.getLayer(layerId) == null) {
                    handler.removeCallbacks(this) // Stop updates if layer doesn't exist
                    return
                }

                // Step 5: Compute a dynamic color based on time
                val time = System.currentTimeMillis() / 1000.0 // Current time in seconds
                val cosValue = cos(time * 2) // Periodic cosine value

                // Map `cosValue` (-1 to 1) to specific colors
                val dynamicColor = when {
                    cosValue < -0.5 -> "#0000FF" // Blue
                    cosValue < 0.5 -> "#FFFF00" // Yellow
                    else -> "#FF0000" // Red
                }

                // Step 6: Safely update the layer's properties
                style.getLayerAs<FillLayer>(layerId)?.setProperties(
                    fillColor(dynamicColor)
                )

                // Step 7: Schedule the next update
                handler.postDelayed(this, 30) // Update every 30ms (~33 FPS)
            } catch (e: Exception) {
                // Log or handle exceptions to prevent crashes
                e.printStackTrace()
                handler.removeCallbacks(this) // Stop updates if an error occurs
            }
        }
    }

    // Start the animation loop
    handler.post(updateTask)
}

@Composable
fun OldXmlViewComposable() {
    AndroidView(
        factory = { context ->
            // Inflate the XML layout
            MapLibre.getInstance(context)

            LayoutInflater.from(context).inflate(R.layout.old_view_layout, null)

        },
        update = { rootView ->

            var mapView : MapView = rootView.findViewById(R.id.mapView)
            mapView.getMapAsync { map ->
                map.setStyle("https://demotiles.maplibre.org/style.json")
                map.cameraPosition = CameraPosition.Builder().target(LatLng(0.0,0.0)).zoom(1.0).build()
                map.addOnMapClickListener { latlen ->
                    val screenPoint = map.projection.toScreenLocation(latlen)
                    val features = map.queryRenderedFeatures(screenPoint, "countries-fill") // Replace "layer-id" with your layer name
                    //println((map.style?.getLayer("countries-fill") as FillLayer).sourceId)
                    if (features.isNotEmpty()) {
                        // Display properties of the first feature
                        val properties = features[0].properties()?.toString() // Get feature properties as a JSON string
                        println("Feature Properties: $properties")
                    } else {
                        println("No features found at the clicked location.")
                    }
                    val kurwa = FillLayer("countrieskurwa", "maplibre").withProperties(
                        fillColor("rgba(0, 255, 0, 1)") // Initial fill color
                    ).withFilter(
                            Expression.eq(Expression.get("NAME"),Expression.literal("Germany"))
                    )
                    kurwa.sourceLayer = "countries"
                    map.style?.addLayer(kurwa)
                    startFillColorAnimation(map.style!!,"countrieskurwa")

                    map.style?.getLayer("countries-fill")?.setProperties(
                        PropertyFactory.fillColor("rgba(0, 255, 0, 0.5)"),
                        PropertyFactory.visibility(Property.NONE)
                    )
//                     (map.style?.getLayer("countries-label") as? SymbolLayer)?.setFilter(
//                            Expression.neq(Expression.get("NAME"),Expression.literal("Germany"))
//                         )

//                    map.style?.getLayer("countries-label")?.
                    println(latlen.longitude)
                    println(latlen.latitude)
                   true
                }
            }

            // Optionally update the view's properties or set listeners here
//            val textView = view.findViewById<TextView>(R.id.old_text_view)
//            val button = view.findViewById<Button>(R.id.old_button)
//
//            button.setOnClickListener {
//                textView.text = "Button Clicked!"
//            }
        }
    )
}