package com.example.tempsdeflors

import android.content.Context
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tempsdeflors.ui.theme.TempsDeFlorsTheme
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.io.FileInputStream


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().load(applicationContext, PreferenceManager.getDefaultSharedPreferences(applicationContext))

        setContent {
            TempsDeFlorsTheme {
                PantallaMapa()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaMapa() {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()

    //Menu de l'esquerra
    ModalNavigationDrawer (
        drawerState = drawerState,
        drawerContent = {
            //Opcions del menú
            ModalDrawerSheet {
                Text("Menú", modifier = Modifier.padding(16.dp))
                NavigationDrawerItem(
                    label = { Text("Mapa") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("map")
                    }
                )
                NavigationDrawerItem(
                    label = { Text("Sobre") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("about")
                    }
                )
            }
        }
    ) {
        Scaffold(
            //barra lila de sobre del mapa, que diu temps de flors i l'icona del menu
            topBar = {
                TopAppBar(
                    title = { Text("Temps de Flors") },
                    //icona del menu
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Obrir el menú")
                        }
                    }
                )
            }
        ) { innerPadding ->
            /*Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                //Contingut de la pantalla, crida al mapa
                OsmMapView()
            }*/
            NavHost(
                navController = navController,
                startDestination = "map",
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("map") { OsmMapView() }
                composable("altre") { Altre() }
            }
        }
    }
}

@Composable
fun Altre() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("App de Temps de Flors!")
    }
}

fun carregarPuntsDesDeJSON(context: Context): List<Punts> {
   // val json = context.assets.open("punts.json").bufferedReader().use { it.readText() }
    val json = context.assets.open("punts.json").bufferedReader().use { it.readText() }
    val gson = Gson()
    val tipus = object : TypeToken<List<Punts>>() {}.type
    return gson.fromJson(json, tipus)
}


@Composable
fun OsmMapView() {
    val context = LocalContext.current

    val punts = remember { carregarPuntsDesDeJSON(context) }

    val mapView = MapView(context)
    val mapController = mapView.controller


    //Una vista per a veure el mapa
    AndroidView(
        factory = { context ->
            mapView.setTileSource(TileSourceFactory.MAPNIK)//tipus de mapa
            mapView.setMultiTouchControls(true)//que es pugui tocar
            mapController.setZoom(15.0)//zoom aplicat a l'inici
            mapController.setCenter(GeoPoint(41.983, 2.824)) //coordenades de l'inici

            punts.forEach { punt ->
                val marker = Marker(mapView)
                marker.position = GeoPoint(punt.lat, punt.lon)
                marker.title = punt.titol
                marker.subDescription = punt.descripcio
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                marker.icon = when (punt.ruta) {
                    "ruta1" -> context.getDrawable(R.drawable.one_circle_svgrepo_com)
                    "ruta2" -> context.getDrawable(R.drawable.two_circle_svgrepo_com)
                    "ruta3" -> context.getDrawable(R.drawable.three_circle_svgrepo_com)
                    "accessible" -> context.getDrawable(R.drawable.accessibility_svgrepo_com)
                    else -> null
                }
                mapView.overlays.add(marker)
            }

            // Mostrar localització de l'usuari
            val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), mapView).apply {
                enableMyLocation()
                enableFollowLocation()
            }
            mapView.overlays.add(locationOverlay)


            mapView//aixo sempre al final
        },
        modifier = Modifier.fillMaxSize()
    )
}