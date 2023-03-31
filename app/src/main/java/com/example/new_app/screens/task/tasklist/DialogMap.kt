package com.example.new_app.screens.task.tasklist

import android.os.Bundle
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.new_app.model.Task
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.*


@Composable
fun DialogMap(
    task: Task,
    modifier: Modifier = Modifier
) {
    val marker = remember { mutableStateOf<MarkerOptions?>(null) }
    val cameraPosition = remember {
        CameraPosition.Builder()
            .target(LatLng(task.location?.latitude ?: 0.0, task.location?.longitude ?: 0.0))
            .zoom(15f)
            .build()
    }

    val icon = remember {
        BitmapDescriptorFactory.defaultMarker()
    }

    LaunchedEffect(task.location) {
        task.location?.let {
            marker.value = MarkerOptions()
                .position(LatLng(it.latitude, it.longitude))
                .icon(icon)
        }
    }

    GoogleMapViewSmall(
        modifier = modifier
            .padding(4.dp)
            .clip(RoundedCornerShape(4.dp)),
        cameraPosition = cameraPosition,
        marker = marker.value
    )
}

@Composable
fun GoogleMapViewSmall(
    modifier: Modifier = Modifier,
    cameraPosition: CameraPosition,
    marker: MarkerOptions? = null
) {
    val googleMap = remember { mutableStateOf<GoogleMap?>(null) }
    val markerState = remember { mutableStateOf<Marker?>(null) }

    LaunchedEffect(googleMap.value, marker) {
        if (googleMap.value != null && marker != null) {
            markerState.value?.remove()
            markerState.value = googleMap.value?.addMarker(marker)
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            MapView(context).apply {
                onCreate(Bundle())
                onResume()
                getMapAsync { map ->
                    googleMap.value = map

                    marker?.let { markerOptions ->
                        markerState.value?.remove()
                        markerState.value = map.addMarker(markerOptions)
                    }
                    map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                }
            }
        },
        update = { mapView ->
            mapView.getMapAsync { map ->
                // No need to update the marker on every recomposition
            }
        }
    )
}

