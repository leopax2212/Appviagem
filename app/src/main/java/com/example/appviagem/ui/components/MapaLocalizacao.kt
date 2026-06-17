package com.example.appviagem.ui.components

import android.preference.PreferenceManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

/**
 * Exibe um mapa (OpenStreetMap via osmdroid) centrado na localizacao informada,
 * com um marcador indicando a posicao atual da viagem.
 */
@Composable
fun MapaLocalizacao(
    latitude: Double,
    longitude: Double,
    titulo: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val mapView = remember {
        Configuration.getInstance().load(
            context,
            PreferenceManager.getDefaultSharedPreferences(context)
        )
        Configuration.getInstance().userAgentValue = context.packageName

        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(15.0)
        }
    }

    DisposableEffect(Unit) {
        mapView.onResume()
        onDispose { mapView.onPause() }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier,
        update = { map ->
            val ponto = GeoPoint(latitude, longitude)
            map.controller.setCenter(ponto)

            map.overlays.clear()
            val marcador = Marker(map).apply {
                position = ponto
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title = titulo
            }
            map.overlays.add(marcador)
            map.invalidate()
        }
    )
}
