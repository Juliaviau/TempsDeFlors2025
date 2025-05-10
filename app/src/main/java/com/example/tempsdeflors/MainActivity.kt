package com.example.tempsdeflors

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.benchmark.perfetto.ExperimentalPerfettoTraceProcessorApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tempsdeflors.ui.theme.TempsDeFlorsTheme
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.io.Console
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

//fonts
@OptIn(ExperimentalTextApi::class)
/*val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val poppinsFont = FontFamily(
    Font(
        googleFont = GoogleFont("Poppins"),
        fontProvider = provider
    )
)

val caveat = FontFamily(
    Font(
        googleFont = GoogleFont("Caveat"),
        fontProvider = provider
    )
)

val kalnia = FontFamily(
    Font(
        googleFont = GoogleFont("Kalnia Glaze"),
        fontProvider = provider
    ))
*/
val llistaDeMarkers = mutableListOf<Marker>()
var mapa = mutableListOf<MapView>()
private fun Date.formatToCalendarDay(): String = SimpleDateFormat("d", Locale.getDefault()).format(this)

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


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,ExperimentalPerfettoTraceProcessorApi::class)
@Composable
fun PantallaMapa() {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()

    val context = LocalContext.current
    //val puntsRepo = PuntsRepository(context)
    //puntsRepo.inicialitzarSiCal()

    val punts = remember { carregarPuntsDesDeJSON(context)}
    val grouped = punts.groupBy { it.ruta }
    val listState = rememberLazyListState()

    //Menu de l'esquerra
    ModalNavigationDrawer (
        drawerState = drawerState,
        drawerContent = {
            //Opcions del menú
            ModalDrawerSheet {
                Text("Espais",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.CenterHorizontally)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    grouped.forEach { (ruta, punts) ->
                        stickyHeader {
                            Text(
                                text = "Ruta $ruta",
                                fontSize = 20.sp,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                color = androidx.compose.ui.graphics.Color.White,
                                modifier = Modifier
                                    .background(
                                        when (ruta) {
                                            "1" -> {
                                                androidx.compose.ui.graphics.Color(0xFF00a80d)
                                            }

                                            "2" -> {
                                                androidx.compose.ui.graphics.Color(0xFF7d007d)
                                            }

                                            "3" -> {
                                                androidx.compose.ui.graphics.Color(0xFF004988)
                                            }

                                            "ACCESSIBLE" -> {
                                                androidx.compose.ui.graphics.Color.Gray
                                            }

                                            else -> {
                                                androidx.compose.ui.graphics.Color.Gray
                                            }
                                        },
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .padding(horizontal = 6.dp)
                                    .wrapContentSize(Alignment.Center)

                            )
                        }

                        itemsIndexed(punts) { index, punt ->
                            val nextPunt = punts.getOrNull(index + 1)
                            TimelineItem(
                                punt = punt,
                                nextPunt = nextPunt,
                                isFirst = index == 0,
                                isLast = index == punts.lastIndex,
                                scope = scope,
                                drawerState = drawerState
                            )
                        }

                    }
                }

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
                    title = { Text("Temps de Flors 2025", fontSize = 24.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif, color = Color(0xFF93117e), fontWeight = FontWeight.Bold)},
                    //icona del menu
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Obrir el menú")
                        }
                    },

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

fun onPuntClick(punt: Punts,mapView: MapView,drawerState: DrawerState,markers: List<Marker>,scope: CoroutineScope) {
    // Tanca el Drawer
    scope.launch {
        drawerState.close()
    }

    // Cerca el Marker associat
    val marker = markers.find { it.relatedObject == punt } ?: return

    // Centra el mapa en aquest punt
    val controller = mapView.controller
    controller.animateTo(marker.position)

    // Obre el seu InfoWindow
    marker.showInfoWindow()
}


@Composable
fun TimelineItem(punt: Punts, nextPunt: Punts?, isFirst: Boolean, isLast: Boolean, scope: CoroutineScope,drawerState: DrawerState) {
    val circleColor = if (punt.visitat == "si") Color(0xFF4CAF50) else Color(0xFFF44336) // Verd o vermell

    // Degradat entre el color del punt actual i el següent
    val lineGradient = Brush.verticalGradient(
        colors = listOf(
            circleColor,
            if (nextPunt?.visitat == "si") Color(0xFF4CAF50) else Color(0xFFF44336)
        )
    )

    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(top = if (isFirst) 16.dp else 0.dp)) {
        // Timeline
        Column(
            modifier = Modifier
                .width(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!isFirst) {
                Box(
                    modifier = Modifier
                        .width(6.dp)
                        .height(16.dp)
                        .background(circleColor)
                )
            }

            // Punt indicador (cercle)
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(circleColor)
            )

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(6.dp)
                        .height(64.dp)
                        .background(brush = lineGradient)
                )
            }
        }

        // Targeta de contingut
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .clickable {
                    onPuntClick(
                        punt = punt,
                        mapView = mapa.get(0),
                        drawerState = drawerState,
                        markers = llistaDeMarkers,
                        scope = scope
                    )
                },
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = punt.titol,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (punt.visitat == "si") "Visitat" else "No visitat",
                    fontSize = 16.sp,
                    color = circleColor
                )
            }
        }
    }
}

fun carregarPuntsDesDeJSON(context: Context): MutableList<Punts> {
    val json = context.assets.open("punts.json").bufferedReader().use { it.readText() }
    val gson = Gson()
    val tipus = object : TypeToken<List<Punts>>() {}.type
    return gson.fromJson(json, tipus)
}


@Composable
fun Altre() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("App de Temps de Flors!")
    }
}
/*
fun copyJsonIfNeeded(context: Context) {
    val file = File(context.filesDir, "punts.json")
    if (!file.exists()) {
        val asset = context.assets.open("punts.json")
        file.outputStream().use { asset.copyTo(it) }
    }
}

fun marcarPuntComVisitat(context: Context, numero: String) {
    val punts = carregarPuntsDesDeJSON(context)

    val punt = punts.find { it.numero == numero }
    punt?.let {
        it.visitat = "si"
        guardarPunts(context, punts)
    }
}

fun guardarPunts(context: Context, punts: List<Punts>) {
    val file = File(context.filesDir, "punts.json")
    val json = Gson().toJson(punts)
    file.writeText(json)
}


fun carregarPuntsDesDeJSON(context: Context): MutableList<Punts> {
   // val json = context.assets.open("punts.json").bufferedReader().use { it.readText() }
    val file = File(context.filesDir, "punts.json")
    val json = file.readText()
    val gson = Gson()
    val tipus = object : TypeToken<MutableList<Punts>>() {}.type
    return gson.fromJson(json, tipus)
}*/

@Composable
fun OsmMapView() {
    val context = LocalContext.current
    //val puntsRepo = PuntsRepository(context)

    val punts = remember { carregarPuntsDesDeJSON(context) }

    val mapView = MapView(context)
    val mapController = mapView.controller
    mapa = mutableListOf(mapView)
    val activity = context as? Activity
    val locationPermissionState = remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val locationProvider = GpsMyLocationProvider(context)
    val locationOverlay = MyLocationNewOverlay(locationProvider, mapView)

    //var mostrarRuta2 = remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        if (!locationPermissionState.value && activity != null) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                1001
            )
        } else {
            startLocationOverlay(locationOverlay, locationProvider,mapView)
        }
    }
    if (!locationPermissionState.value) {
        Text("Cal activar el permís de localització.")
    }
    //Una vista per a veure el mapa
    AndroidView(
        factory = { context ->

            mapView.setTileSource(TileSourceFactory.MAPNIK)//tipus de mapa opentopo, mapink
            mapView.setMultiTouchControls(true)//que es pugui tocar
            mapController.setZoom(15.0)//zoom aplicat a l'inici
            mapController.setCenter(GeoPoint(41.983, 2.824)) //coordenades de l'inici

            var color = ContextCompat.getColor(context, R.color.ruta1)

            //rutes
            val ruta1Coords = listOf(
                GeoPoint(41.98109852452547, 2.825254862889998),
                GeoPoint(41.98064392244017, 2.825045650597422),
                GeoPoint(41.98064392243832, 2.824798887349051), //cruilla pedreres mossen jacint verdaguer
                GeoPoint(41.980643871230576, 2.824144434281989),
                GeoPoint(41.98104456195319, 2.823906433056236),
                GeoPoint(41.981681679147144, 2.823617734171104),
                GeoPoint(41.98219807471998, 2.8236222450720505), //cruilla pl cat
                GeoPoint(41.98218801510607, 2.8230222927568813), //cruilla punt 95
                GeoPoint(41.98201700142651, 2.822918541604634),
                GeoPoint(41.98118914449985, 2.822979580946365),
                GeoPoint(41.981148632348365, 2.822743403446672),
                GeoPoint(41.9813417558765, 2.8212018053115755),
                GeoPoint(41.981148632348365, 2.822743403446672),
                GeoPoint(41.98118914449985, 2.822979580946365),
                GeoPoint(41.98201700142651, 2.822918541604634),
                GeoPoint(41.98218801510607, 2.8230222927568813), //cruilla punt 95 tornada ramal hospital
                GeoPoint(41.98226458016483, 2.822702608566745),
                GeoPoint(41.98270559785695, 2.822798299623236), //cruilla punt 94 amb pont de pedra est
                GeoPoint(41.98269497783582, 2.821962484099978), //punt 94
                GeoPoint(41.98270559785695, 2.822798299623236), //retorn cruilla punt 94 amb pont pedra
                GeoPoint(41.98348529817638, 2.8230684905494123),
                GeoPoint(41.98387414519732, 2.8231769404205362), //punt 93 cruilla
                GeoPoint(41.983717935804535, 2.822210355477685), //cantonada punt 92 carrer sequia amb pl sta susanna
                GeoPoint(41.98436752926397, 2.822170454860172), //cruilla c/hortes
                GeoPoint(41.98482867422382, 2.821989547193168), //punt 90
                GeoPoint(41.98496669185403, 2.821581943947246),
                GeoPoint(41.985195077026894, 2.8226250784150637), //anselm clave amb c/nord
                GeoPoint(41.98575770646682, 2.822364381392028), // c/nord amb gv jaume i
                GeoPoint(41.985994800124324, 2.8223516225844962),
                GeoPoint(41.98618447441462, 2.82234524318073), //cruilla del punt 87
                GeoPoint(41.98650691941124, 2.8216243705551625),
                GeoPoint(41.98627745078095, 2.8212165696738216),
                GeoPoint(41.98644890871311, 2.8207284076268664),
                GeoPoint(41.98666622100278, 2.8206345303160956),
                GeoPoint(41.98652877223936, 2.8199507066646703), //entrada devesa
                GeoPoint(41.987947782421315, 2.819542384943666),
                GeoPoint(41.98816769858773, 2.8211279493044197),
                GeoPoint(41.989836781169295, 2.820642417614568),
                GeoPoint(41.9898593360392, 2.8192996190889534), //punts 83/84
                GeoPoint(41.99221360752885, 2.8182340768442997), //rodona ocine
                GeoPoint(41.99330614959599, 2.8186384008441703),
                GeoPoint(41.993377914813124, 2.8179624842066175),
                GeoPoint(41.99364105325037, 2.817286567569064),
                GeoPoint(41.9954032553902, 2.8170398043174343),
                GeoPoint(41.99670294787224, 2.8165891931915317),
                GeoPoint(41.997619892854885, 2.816739396890937), //residencia domusvi
                GeoPoint(41.99670294787224, 2.8165891931915317),
                GeoPoint(41.9954032553902, 2.8170398043174343),
                GeoPoint(41.99364105325037, 2.817286567569064),
                GeoPoint(41.993377914813124, 2.8179624842066175),
                GeoPoint(41.99330614959599, 2.8186384008441703),
                GeoPoint(41.99221360752885, 2.8182340768442997), //rodona ocine
                GeoPoint(41.99228153270258, 2.8106090057910853),
                GeoPoint(41.99274618781898, 2.810583488176021),
                GeoPoint(41.99376083107807, 2.808975878426968), //cc ter
                GeoPoint(41.99274618781898, 2.810583488176021),
                GeoPoint(41.99228153270258, 2.8106090057910853),
                GeoPoint(41.99221360752885, 2.8182340768442997), //rodona ocine
                GeoPoint(41.9898593360392, 2.8192996190889534), //punts 83/84
                GeoPoint(41.989836781169295, 2.820642417614568),
                GeoPoint(41.98816769858773, 2.8211279493044197),
                GeoPoint(41.987947782421315, 2.819542384943666),
                GeoPoint(41.98652877223936, 2.8199507066646703),
                GeoPoint(41.98666622100278, 2.8206345303160956),
                GeoPoint(41.98644890871311, 2.8207284076268664),
                GeoPoint(41.98627745078095, 2.8212165696738216),
                GeoPoint(41.98650691941124, 2.8216243705551625),
                GeoPoint(41.98618447441462, 2.82234524318073), // cruilla punt 87
                GeoPoint(41.9864713693318, 2.822320207949836),
                GeoPoint(41.9860825979205, 2.8230363577441926),
                GeoPoint(41.98611250349878, 2.8232589811088498), //bif pl independencia
                GeoPoint(41.98621088859183, 2.8236690499822257),
                GeoPoint(41.986583064133356, 2.8236842228941352),
                GeoPoint(41.98678888754458, 2.8244770050745127), //cruilla pont gomez amb ballesteries
                GeoPoint(41.98690853822619, 2.8244348726578745), //bif ballesteries
                GeoPoint(41.98746681459182, 2.8241671508929542),
                GeoPoint(41.98804384898717, 2.824203200294473), //cul de la lleona
                GeoPoint(41.98833119246961, 2.824566371568843),
                GeoPoint(41.98825144701566, 2.8246843887656348),
                GeoPoint(41.98837305879824, 2.8249472452357938), //cruilla pujada rei marti
                GeoPoint(41.98864015940304, 2.8253832123000238),
                GeoPoint(41.98896969776134, 2.8255642899770295), //cruilla riu galligants
                GeoPoint(41.98915154864943, 2.825294971628672),
                GeoPoint(41.98991992254499, 2.825912760724912),
                GeoPoint(41.989806124714725, 2.8263880263278005),
                GeoPoint(41.98992110793805, 2.826389621177371), // punt 77
                GeoPoint(41.989806124714725, 2.8263880263278005),
                GeoPoint(41.98991992254499, 2.825912760724912),
                GeoPoint(41.98915154864943, 2.825294971628672),
                GeoPoint(41.98896969776134, 2.8255642899770295), //cruilla riu galligants
                GeoPoint(41.98896964495334, 2.825724240861404),
                GeoPoint(41.98893779289267, 2.8259249767932517),
                GeoPoint(41.98897132137799, 2.8260986472002743), //cruila st pere galligants
                GeoPoint(41.98906184820016, 2.826610636582017),
                GeoPoint(41.98887387116339, 2.8267723713614057),
                GeoPoint(41.98861868752337, 2.8271639738577656),
                GeoPoint(41.98837292488172, 2.828299394328307),
                GeoPoint(41.9882455153459, 2.8285835822670715),
                GeoPoint(41.98826227977307, 2.8291970673412297),
                GeoPoint(41.98812145843997, 2.8302120243623907),
                GeoPoint(41.98865456611108, 2.831051055449138),
                GeoPoint(41.98850894307043, 2.8321497695691873),
                GeoPoint(41.98881405369143, 2.832334717275367),
                GeoPoint(41.98865646926758, 2.8330339098231208),
                GeoPoint(41.98870005648765, 2.833575220182672), //monestir de sant daniel
                GeoPoint(41.98865646926758, 2.8330339098231208),
                GeoPoint(41.98881405369143, 2.832334717275367),
                GeoPoint(41.98850894307043, 2.8321497695691873),
                GeoPoint(41.98865456611108, 2.831051055449138),
                GeoPoint(41.98812145843997, 2.8302120243623907),
                GeoPoint(41.98826227977307, 2.8291970673412297),
                GeoPoint(41.9882455153459, 2.8285835822670715),
                GeoPoint(41.98837292488172, 2.828299394328307),
                GeoPoint(41.98861868752337, 2.8271639738577656),
                GeoPoint(41.98887387116339, 2.8267723713614057),
                GeoPoint(41.98906184820016, 2.826610636582017),
                GeoPoint(41.98897132137799, 2.8260986472002743), //cruilla st pere galligants
                GeoPoint(41.98888557268882, 2.8262890363785824),
                GeoPoint(41.98846691156626, 2.8261602903519747),
                GeoPoint(41.988278616312485, 2.826119688260733), //cruilla riu galligants
                GeoPoint(41.988167971035566, 2.8258760985895774),
                GeoPoint(41.98791650379186, 2.825747537379185), //bif ferran el catolic
                GeoPoint(41.987872851688564, 2.8259830139077318),
                GeoPoint(41.987975524557115, 2.8260567746519087),
                GeoPoint(41.987924675005324, 2.826591782438308),
                GeoPoint(41.98738829660218, 2.827999920544393),
                GeoPoint(41.98724076746351, 2.828011197843264),
                GeoPoint(41.987408162830214, 2.8275253728745984),
                GeoPoint(41.98722071261326, 2.827332297687185), //cruilla alemanys/bisbe josep cartanyà
                GeoPoint(41.986941643719106, 2.8274073556852723), //cruilla alemanys/pere rocaberti
                GeoPoint(41.98690302519758, 2.8277089718337147),
                GeoPoint(41.986752870394476, 2.8282565452960147),
                GeoPoint(41.98679562067739, 2.8284493871116045),
                GeoPoint(41.98690207713949, 2.8285328391253692), // punt 53
                GeoPoint(41.98679562067739, 2.8284493871116045),
                GeoPoint(41.986752870394476, 2.8282565452960147),
                GeoPoint(41.98690302519758, 2.8277089718337147),
                GeoPoint(41.986941643719106, 2.8274073556852723), //cruilla alemanys/pere rocaberti
                GeoPoint(41.98686708811359, 2.826796287553301),
                GeoPoint(41.98654441148025, 2.8266415878138207),
                GeoPoint(41.98653947734461, 2.8262790097258583),
                GeoPoint(41.98644982862962, 2.8262197423548847),
                GeoPoint(41.98637941600118, 2.8260889256846604), //cruilla claveria
                GeoPoint(41.986266826366816, 2.825782830691015),
                GeoPoint(41.98626118733142, 2.8255571343154195),
                GeoPoint(41.986330265475054, 2.8252081584274062), //cruilla força/antic hernandez
                GeoPoint(41.98643317767464, 2.825204365213343), //cruilla força/pujada catedral
                GeoPoint(41.98644696076824, 2.8253375092537545),
                GeoPoint(41.98687362946112, 2.8258062776531445), //cruilla catedral/claveria
                GeoPoint(41.98637941600118, 2.8260889256846604), //cruilla claveria (ramal entre cruilles)
                GeoPoint(41.98687362946112, 2.8258062776531445), //cruilla catedral/claveria
                GeoPoint(41.98688757007755, 2.8262917694377503),
                GeoPoint(41.98706942691748, 2.8262936660554296),
                GeoPoint(41.98715797127175, 2.8268348923992157),
                GeoPoint(41.98734138882849, 2.8269502273827896),
                GeoPoint(41.98722071261326, 2.827332297687185), //cruilla alemanys/josep cartanya (fi ramal absis catedral)
                GeoPoint(41.98734138882849, 2.8269502273827896),
                GeoPoint(41.98715797127175, 2.8268348923992157),
                GeoPoint(41.98706942691748, 2.8262936660554296),
                GeoPoint(41.98688757007755, 2.8262917694377503),
                GeoPoint(41.98687362946112, 2.8258062776531445), //cruilla catedral/claveria (fi tornada ramal absis catedral)
                GeoPoint(41.987200412763634, 2.825757619932531),
                GeoPoint(41.987240286166276, 2.8252024026752687),
                GeoPoint(41.98743965278917, 2.8252131315107847), //cruilla petit ramal lateral nord catedral
                GeoPoint(41.987501456318654, 2.825816628519456), //ramal a punt 58 lateral nord de la catedral
                GeoPoint(41.98743965278917, 2.8252131315107847), //cruilla petit ramal lateral nord catedral (fi tornada ramal)
                GeoPoint(41.9877522760039, 2.825214765871935), //trifurcació força/ferran catolic/pujada del rei
                GeoPoint(41.987808839311306, 2.8255130438554086),
                GeoPoint(41.98791650379186, 2.825747537379185), //bif ferran el catolic fi ramal
                GeoPoint(41.987808839311306, 2.8255130438554086),
                GeoPoint(41.9877522760039, 2.825214765871935), //trifurcació força/ferran catolic/pujada del rei (fi ramal cap a ferran el catolic)
                GeoPoint(41.98837305879824, 2.8249472452357938), //cruilla pujada rei marti dir riu galligants (fi ramal)
                GeoPoint(41.9877522760039, 2.825214765871935),//trifurcació força/ferran catolic/pujada del rei (fi tots els ramals)
                GeoPoint(41.987690506357396, 2.824997322008843),
                GeoPoint(41.98764265861338, 2.824619130556879),
                GeoPoint(41.98690853822619, 2.8244348726578745), // bif ballesteries (connexio amb pujada st feliu)
                GeoPoint(41.98678888754458, 2.8244770050745127), //cruilla pont gomez amb ballesteries
                GeoPoint(41.98568309490265, 2.824971746633843),
                GeoPoint(41.985344161920764, 2.824971746633843), //trif ballesteries, argenteria i bonaventura carreras
                GeoPoint(41.9854201445474, 2.825325458089721), //ramal cap a cruilla força/antic hernandez per bv carreras
                GeoPoint(41.986330265475054, 2.8252081584274062), //cruilla força/antic hernandez
                GeoPoint(41.9854201445474, 2.825325458089721),
                GeoPoint(41.985344161920764, 2.824971746633843), //trif ballesteries, argenteria i bonaventura carreras  (fi ramal a c/força)
                GeoPoint(41.98532655070404, 2.8247617045129623), //cruilla argenteria amb pont st agusti
                GeoPoint(41.985561594527724, 2.8240041464236527),
                GeoPoint(41.98546100363131, 2.8234763687897666), //cruilla independencia sta clara
                GeoPoint(41.98611250349878, 2.8232589811088498), //bif pl independencia (fi ramal)
                GeoPoint(41.98546100363131, 2.8234763687897666), //cruilla independencia sta clara
                GeoPoint(41.98469387059895, 2.823445659327425), //trifurcacio sta clara, hortes i pont peixateries velles
                GeoPoint(41.98436752926397, 2.822170454860172), //cruilla c/hortes
                GeoPoint(41.98469387059895, 2.823445659327425), //trifurcacio sta clara, hortes i pont peixateries velles (fi ramal connexio amb cruilla hortes)
                GeoPoint(41.98387414519732, 2.8231769404205362), //punt 93 cruilla (fi ramal)
                GeoPoint(41.98469387059895, 2.823445659327425), //trifurcacio sta clara, hortes i pont peixateries velles (fi ramals)
                GeoPoint(41.984448891019426, 2.8243792556831626),//cruilla pont peixateries velles amb pg llibertat i carrer minali
                GeoPoint(41.98532655070404, 2.8247617045129623), //cruilla argenteria amb pont st agusti
                GeoPoint(41.985344161920764, 2.824971746633843), //trif ballesteries, argenteria i bonaventura carreras
                GeoPoint(41.984825088704554, 2.825022530726335),
                GeoPoint(41.984778145758334, 2.8257262342300424), //cruilla plaça de l'oli
                GeoPoint(41.98499727728638, 2.826522262705578), //cruilla pujada st domenec
                GeoPoint(41.985281726997705, 2.826512371689138),
                GeoPoint(41.985717835057734, 2.827392736614295),
                GeoPoint(41.985516304628405, 2.8276925685913006), //punt 37 (fi ramal)
                GeoPoint(41.985717835057734, 2.827392736614295),
                GeoPoint(41.985281726997705, 2.826512371689138),
                GeoPoint(41.98499727728638, 2.826522262705578), //cruilla pujada st domenec (fi ramal punt 37)
                GeoPoint(41.98461755335412, 2.826552660752563),
                GeoPoint(41.984239727121064, 2.826408518537949),
                GeoPoint(41.98438070732099, 2.8255815974120058), //cruilla st marti amb ciutadans
                GeoPoint(41.984778145758334, 2.8257262342300424), //cruilla plaça de l'oli
                GeoPoint(41.98438070732099, 2.8255815974120058), //cruilla st marti amb ciutadans (fi ramal a pl oli)
                GeoPoint(41.984151524203334, 2.825393788930234),
                GeoPoint(41.984448891019426, 2.8243792556831626),//cruilla pont peixateries velles amb pg llibertat i carrer minali
                GeoPoint(41.983565331884606, 2.823929596476662),
                GeoPoint(41.98274716231684, 2.8238032907160577), //cruilla pont de pedra
                GeoPoint(41.98219807471998, 2.8236222450720505), //cruilla pl cat (fi ramal)
                GeoPoint(41.98274716231684, 2.8238032907160577), //cruilla pont de pedra
                GeoPoint(41.982742487349256, 2.824365188716106), //cruilla pl vi 1
                GeoPoint(41.98217297718624, 2.824060598684278), //fi ramal punt 12
                GeoPoint(41.982742487349256, 2.824365188716106), //cruilla pl vi 1
                GeoPoint(41.98281661051744, 2.8244171925587853), //cruilla pl vi 2
                GeoPoint(41.98329191251765, 2.824700932026055),
                GeoPoint(41.983260896370055, 2.8247957624304063),
                GeoPoint(41.98388381104625, 2.825249539988253),
                GeoPoint(41.98438070732099, 2.8255815974120058), //cruilla st marti amb ciutadans (fi ramal des de pl vi)
                GeoPoint(41.98388381104625, 2.825249539988253),
                GeoPoint(41.983260896370055, 2.8247957624304063),
                GeoPoint(41.98329191251765, 2.824700932026055),
                GeoPoint(41.98281661051744, 2.8244171925587853), //cruilla pl vi 2
                GeoPoint(41.98246651609437, 2.8252411754122604), //cruilla nou del teatre amb auriga
                GeoPoint(41.982555569508506, 2.8257470539616616),
                GeoPoint(41.982928679673606, 2.8257536944820143),
                GeoPoint(41.98296648192391, 2.826191512315619),
                GeoPoint(41.98285975848732, 2.82623767151012),
                GeoPoint(41.98284790324168, 2.8263237934609626),
                GeoPoint(41.98295196486495, 2.826374469639671),
                GeoPoint(41.98284790324168, 2.8263237934609626),
                GeoPoint(41.98258563727943, 2.826258045514806),
                GeoPoint(41.98227934424397, 2.82604821772495),
                GeoPoint(41.98207459616589, 2.8256588358616646),
                GeoPoint(41.98198154589723, 2.8252264092022767),
                GeoPoint(41.98182364210022, 2.8247788096689748), //cruilla portal nou amb pujada merce
                GeoPoint(41.98195334881743, 2.824786396101323),
                GeoPoint(41.98246651609437, 2.8252411754122604), //cruilla nou del teatre amb auriga (fi ramal)
                GeoPoint(41.98195334881743, 2.824786396101323),
                GeoPoint(41.98182364210022, 2.8247788096689748), //cruilla portal nou amb pujada merce (fi ramal a teatre nou)
                GeoPoint(41.981404597486446, 2.8248366801179907),
                GeoPoint(41.98064392243832, 2.824798887349051), //cruilla pedreres mossen jacint verdaguer
            )
            val ruta2Coords = listOf(
                GeoPoint(41.977659707636306, 2.8074963985445667),
                GeoPoint(41.97723785431085, 2.8064302642503107),
                GeoPoint(41.9766050690748, 2.8057316883035592),
                GeoPoint(41.97563238438624, 2.8024697467594626),
                GeoPoint(41.97515827568987, 2.8025857433483683),
                GeoPoint(41.97521356754567, 2.802912677025183),
                GeoPoint(41.97378600968252, 2.8032706870865285),
                GeoPoint(41.97311926356623, 2.8030137164215296),
                GeoPoint(41.97262218039327, 2.804369501610906),
                GeoPoint(41.97016232298209, 2.8024056828646553),//sta eugeni insti
                GeoPoint(41.96990001130191, 2.803172298083817), //100
                GeoPoint(41.97016232298209, 2.8024056828646553),
                GeoPoint(41.97151038406361, 2.8034388682506437),
                GeoPoint(41.9706081706856, 2.8060075378714404),
                GeoPoint(41.970602194871844, 2.806513903232856),
                GeoPoint(41.97106233127936, 2.808314313493786),
                GeoPoint(41.9712774588491, 2.8084107640434786),
                GeoPoint(41.97143880404958, 2.808579552505441),
                GeoPoint(41.97348247458373, 2.808499177045052),
                GeoPoint(41.973651788209004, 2.809008593811322),
                GeoPoint(41.974399066918416, 2.81136409601482),
                GeoPoint(41.97489146018709, 2.8110366585491815),
                GeoPoint(41.975311927810694, 2.8119892038787535),
                GeoPoint(41.977336772522726, 2.810433875958142),
                GeoPoint(41.977270385193236, 2.8099799285745184),
                GeoPoint(41.97718186865547, 2.808446926015112),
                GeoPoint(41.97709244210335, 2.807943487103821),
                GeoPoint(41.977659707636306, 2.8074963985445667),
            )

            val ruta3Coords = listOf(
                GeoPoint(41.98077872253018, 2.8157099108425307),//103
                GeoPoint(41.98105937326412, 2.8159000512992542),
                GeoPoint(41.98135256600147, 2.8164730667847535),//104
                GeoPoint(41.98105937326412, 2.8159000512992542),
                GeoPoint(41.98077872253018, 2.8157099108425307),//103
                GeoPoint(41.980669369511816, 2.8163688821510267),
                GeoPoint(41.98093767022193, 2.817514913122024),
                GeoPoint(41.980721923341875, 2.8174814253052154),
                GeoPoint(41.98047574973409, 2.818530713402038),
                GeoPoint(41.97956572890889, 2.8181437419098776),//105
                GeoPoint(41.98071915734866, 2.818620014551476),
                GeoPoint(41.98018808498383, 2.819602326856738),//106
                GeoPoint(41.97942742673062, 2.821172538235733),
                GeoPoint(41.98010510451137, 2.8218162504369753),//RODONA CLARET MARAGALL MIG
                GeoPoint(41.9797224913841, 2.8224921457823235),//INICI LLEO
                GeoPoint(41.97996574730077, 2.822712086906631),
                GeoPoint(41.98002955197753, 2.8232056133319072), //107
                GeoPoint(41.97996574730077, 2.822712086906631),
                GeoPoint(41.9797224913841, 2.8224921457823235),//108
                GeoPoint(41.979464032120724, 2.822270485440047),
                GeoPoint(41.97888122074769, 2.8225979600537063),
                GeoPoint(41.9783031340074, 2.8216722261421547),//109
                GeoPoint(41.97888122074769, 2.8225979600537063),
                GeoPoint(41.97895297800465, 2.8237925685783236),
                GeoPoint(41.97840663915357, 2.824388018939254),//ravessat pont
                GeoPoint(41.977684826601674, 2.8243236459931),
                GeoPoint(41.97765292309659, 2.825536004385626),//111
                GeoPoint(41.97810355863317, 2.8256111062907863),
                GeoPoint(41.97809957072212, 2.825884691591754),
                GeoPoint(41.97829099017017, 2.826667896570996),
                GeoPoint(41.978614009184064, 2.8265981591413376),//112
                GeoPoint(41.97829099017017, 2.826667896570996),
                GeoPoint(41.97809957072212, 2.825884691591754),
                GeoPoint(41.97810355863317, 2.8256111062907863),
                GeoPoint(41.97765292309659, 2.825536004385626),//111
                GeoPoint(41.977684826601674, 2.8243236459931),
                GeoPoint(41.976695810499805, 2.8240661539884133),//110
                GeoPoint(41.97575463574814, 2.8241358915581154),
                GeoPoint(41.975132495634206, 2.8250746646773175),
                GeoPoint(41.97435481197907, 2.8246562400993667),
                GeoPoint(41.97430167028126, 2.8238874074532383),
                GeoPoint(41.974172654029175, 2.823763451566593),
                GeoPoint(41.974075891668626, 2.8242096927585156),
                GeoPoint(41.973860529172676, 2.824226530482672),
                GeoPoint(41.970901127972205, 2.824062296943864),
                GeoPoint(41.970838402163594, 2.824290412187343),
                GeoPoint(41.97032031047448, 2.824398809366888),//114
                GeoPoint(41.970838402163594, 2.824290412187343),
                GeoPoint(41.970901127972205, 2.824062296943864),
                GeoPoint(41.973860529172676, 2.824226530482672),
                GeoPoint(41.974075891668626, 2.8242096927585156),
                GeoPoint(41.974172654029175, 2.823763451566593),
                GeoPoint(41.97430167028126, 2.8238874074532383),
                GeoPoint(41.97435481197907, 2.8246562400993667),
                GeoPoint(41.975132495634206, 2.8250746646773175),
                GeoPoint(41.97507168475664, 2.8255793057303817),
                GeoPoint(41.97420861338008, 2.8283401985241494),
                GeoPoint(41.97405370187794, 2.8302452892404135),
                GeoPoint(41.97376600812035, 2.831592247719313),
                GeoPoint(41.973113159750845, 2.8330061821744574),
                GeoPoint(41.97289708693628, 2.8351339935416355),
                GeoPoint(41.97184890427998, 2.8364137139580876),
                GeoPoint(41.970732169772184, 2.8381839717878816),
                GeoPoint(41.97074812459967, 2.839804050389056),//113
            )

            val polyline2 = Polyline()

            //if (mostrarRuta2.value) {
                polyline2.setPoints(ruta2Coords)
                polyline2.setColor(Color.rgb(125, 0, 125)) // Color lila
                polyline2.getPaint().setStrokeCap(Paint.Cap.ROUND);
                polyline2.width = 8.0f // gruix de la línia
                mapView.overlays.add(polyline2)
            /*} else {
                mapView.overlays.remove(polyline2)
            }*/


            val polyline3 = Polyline()
            polyline3.setPoints(ruta3Coords)
            polyline3.setColor(Color.rgb(0, 73, 136)) // Color blau
            polyline3.getPaint().setStrokeCap(Paint.Cap.ROUND);
            polyline3.width = 8.0f // gruix de la línia
            mapView.overlays.add(polyline3)

            val polyline1 = Polyline()
            polyline1.setPoints(ruta1Coords)
            polyline1.setColor(Color.rgb(0, 168, 132)) // Color verd
            polyline1.getPaint().setStrokeCap(Paint.Cap.ROUND);
            polyline1.width = 8.0f // gruix de la línia
            mapView.overlays.add(polyline1)

            punts.forEach { punt ->
                val marker = Marker(mapView)
                marker.position = GeoPoint(punt.lat, punt.lon)
                marker.title = punt.titol
                marker.subDescription = punt.descripcio
                marker.snippet = punt.snippet
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                marker.relatedObject = punt
                val infoWindow = InfoPuntMarker(mapView)
                marker.infoWindow = infoWindow

                when (punt.ruta) {
                    "1" -> {
                        color = if (punt.visitat.equals("no"))
                        ContextCompat.getColor(mapView.context, R.color.ruta1) else ContextCompat.getColor(mapView.context, R.color.ruta1clar)
                    }
                    "2" -> {
                        color = if (punt.visitat.equals("no"))
                            ContextCompat.getColor(mapView.context, R.color.ruta2) else ContextCompat.getColor(mapView.context, R.color.ruta2clar)
                    }
                    "3" -> {
                        color = if (punt.visitat.equals("no"))
                            ContextCompat.getColor(mapView.context, R.color.ruta3) else ContextCompat.getColor(mapView.context, R.color.ruta3clar)
                    }
                    "ACCESSIBLE" -> {
                        color = if (punt.visitat.equals("no"))
                            ContextCompat.getColor(mapView.context, R.color.accessible) else ContextCompat.getColor(mapView.context, R.color.accessibleclar)
                    }
                }
                marker.icon = createNumberedMarkerDrawable(context, punt.numero.toInt(), color)
                mapView.overlays.add(marker)
                llistaDeMarkers.add(marker)
            }

            //Rotar mapa
            val rotationGestureOverlay = RotationGestureOverlay(mapView)
            rotationGestureOverlay.isEnabled
            mapView.setMultiTouchControls(true)
            mapView.overlays.add(rotationGestureOverlay)

            //Bruixola
            val compassOverlay = CompassOverlay(context, InternalCompassOrientationProvider(context), mapView).apply {
                enableCompass()
            }
            mapView.overlays.add(compassOverlay)

            mapView//aixo sempre al final
        },
        modifier = Modifier.fillMaxSize()

    )

    /*Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Row {
            FloatingActionButton(onClick = {

            },
                shape = CircleShape,
                containerColor = Color(ContextCompat.getColor(context, R.color.ruta1)),
                contentColor = androidx.compose.ui.graphics.Color.White,
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Ruta 1"
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            FloatingActionButton(onClick = {
                println("Hola"+mostrarRuta2.value)
                if (mostrarRuta2.value) {
                    mostrarRuta2.value = false
                    return@FloatingActionButton
                } else {
                    mostrarRuta2.value = true
                }
            },
                shape = CircleShape,
                containerColor = if (mostrarRuta2.value) Color(ContextCompat.getColor(context, R.color.ruta2) ) else Color(ContextCompat.getColor(context, R.color.ruta2clar)),
                contentColor = androidx.compose.ui.graphics.Color.White,
            ) {
                Icon(
                    imageVector = if (mostrarRuta2.value) Icons.Default.CheckCircle else Icons.Default.Clear,
                    contentDescription = "Ruta 1"
                )
            }
            FloatingActionButton(onClick = {

            },
                shape = CircleShape,
                containerColor = Color(ContextCompat.getColor(context, R.color.ruta3)),
                contentColor = androidx.compose.ui.graphics.Color.White,
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Ruta 1"
                )
            }
        }

    }*/

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        FloatingActionButton(onClick = {
            val loc = locationOverlay.myLocation
            if (loc != null) {
                Handler(Looper.getMainLooper()).post {
                    mapController.animateTo(loc)
                }
            }
        },

            shape = CircleShape,
            containerColor = Color(0xFF93117e),
            contentColor = androidx.compose.ui.graphics.Color.White,
            ) {
            Icon(
                imageVector = Icons.Default.Place,
                contentDescription = "Centrar ubicació"
            )
        }
    }
}

fun startLocationOverlay(locationOverlay: MyLocationNewOverlay,locationProvider: GpsMyLocationProvider,mapView: MapView) {

    locationOverlay.enableMyLocation()
    locationOverlay.enableFollowLocation()

    locationOverlay.runOnFirstFix {
        val location = locationOverlay.myLocation
        if (location != null) {
            Handler(Looper.getMainLooper()).post {
                mapView.controller.animateTo(location)
            }
        }
    }

    mapView.overlays.add(locationOverlay)
}


fun createNumberedMarkerDrawable(context: Context, number: Int, colorp: Int): Drawable {
    val width = 100
    val height = 120
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val markerPaint = Paint().apply {
        color = colorp
        isAntiAlias = true
    }

    val markerPath = Path().apply {
        moveTo(width / 2f, height.toFloat()) // Punta inferior
        cubicTo(width / 2f, height * 0.75f, width * 0.8f, height * 0.6f, width * 0.8f, height * 0.4f)
        arcTo(width * 0.2f, 0f, width * 0.8f, height * 0.8f, 0f, 180f, false)
        cubicTo(width * 0.2f, height * 0.6f, width / 2f, height * 0.75f, width / 2f, height.toFloat())
        close()
    }
    canvas.drawPath(markerPath, markerPaint)

    val numberBackgroundPaint = Paint().apply {
        color = colorp
        isAntiAlias = true
    }

    val numberBackgroundRadius = 30f
    val numberBackgroundX = width / 2f

    val numberBackgroundY = height * 0.4f

    canvas.drawCircle(numberBackgroundX, numberBackgroundY, numberBackgroundRadius, numberBackgroundPaint)

    val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 35f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
        isAntiAlias = true
    }

    val textX = width / 2f
    val textY = numberBackgroundY - ((textPaint.descent() + textPaint.ascent()) / 2)

    canvas.drawText(number.toString(), textX, textY, textPaint)

    return BitmapDrawable(context.resources, bitmap)
}