package com.example.tempsdeflors

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
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
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import android.graphics.Path
import android.preference.PreferenceActivity
import androidx.benchmark.perfetto.ExperimentalPerfettoTraceProcessorApi
import androidx.benchmark.perfetto.Row
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.compose.ui.graphics.*
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import java.io.File


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


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalPerfettoTraceProcessorApi::class
)
@Composable
fun PantallaMapa() {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()

    val context = LocalContext.current
    val puntsRepo = PuntsRepository(context)
    puntsRepo.inicialitzarSiCal()

    val punts = remember { puntsRepo.llegirPunts() }
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
                    modifier = Modifier.padding(16.dp).
                    align(Alignment.CenterHorizontally)
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
                                            "1" -> {androidx.compose.ui.graphics.Color( 0xFF00a80d)}
                                            "2" -> {androidx.compose.ui.graphics.Color( 0xFF7d007d)}
                                            "3" -> {androidx.compose.ui.graphics.Color(0xFF004988)}
                                            "ACCESSIBLE" -> {androidx.compose.ui.graphics.Color.Gray}
                                            else -> {androidx.compose.ui.graphics.Color.Gray }
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
                                isLast = index == punts.lastIndex
                            )
                        }

                    }
                }

                /*LazyColumn (state = listState, modifier = Modifier.fillMaxSize().padding(start = 24.dp)){
                    grouped.forEach { (ruta, punts) ->
                        stickyHeader {
                            Text(
                                "Ruta $ruta",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(10.dp)
                                    .fillMaxWidth()
                                    .padding(6.dp)
                                    .align(Alignment.CenterHorizontally)
                                    .background(
                                        when (ruta) {
                                            "1" -> {androidx.compose.ui.graphics.Color( 0xFF00a80d)}
                                            "2" -> {androidx.compose.ui.graphics.Color( 0xFF7d007d)}
                                            "3" -> {androidx.compose.ui.graphics.Color(0xFF004988)}
                                            "ACCESSIBLE" -> {androidx.compose.ui.graphics.Color.Gray}
                                            else -> {androidx.compose.ui.graphics.Color.Gray }
                                        }
                                    )
                            )
                        }

                        itemsIndexed(punts) { index, punt ->
                            Box {
                                // Línia vertical contínua
                                Box(
                                    modifier = Modifier
                                        .width(2.dp)
                                        .fillMaxHeight()
                                        .background(androidx.compose.ui.graphics.Color.Black)
                                        .align(Alignment.TopStart)
                                        .offset(x = 11.dp) // per centrar-la darrere dels cercles
                                )

                                // Contingut del punt
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp)
                                ) {
                                    // Cercle indicador
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .align(Alignment.Top)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .align(Alignment.Center)
                                                .clip(CircleShape)
                                                .background(
                                                    if (punt.visitat.equals("si")) androidx.compose.ui.graphics.Color.Green
                                                    else androidx.compose.ui.graphics.Color.Red
                                                )
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(end = 16.dp),
                                        elevation = CardDefaults.cardElevation(4.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(
                                                text = punt.titol,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = if (punt.visitat.equals("si")) "Visitat" else "No visitat",
                                                fontSize = 14.sp,
                                                color = if (punt.visitat.equals("si")) androidx.compose.ui.graphics.Color.Green else androidx.compose.ui.graphics.Color.Red
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }*/


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
                    title = { Text("Temps de Flors 2025", fontSize = 24.sp) },
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
fun TimelineItem(punt: Punts, nextPunt: Punts?, isFirst: Boolean, isLast: Boolean) {
    val circleColor = if (punt.visitat == "si") Color(0xFF4CAF50) else Color(0xFFF44336) // Verd o vermell

    // Degradat entre el color del punt actual i el següent
    val lineGradient = Brush.verticalGradient(
        colors = listOf(
            circleColor,
            if (nextPunt?.visitat == "si") Color(0xFF4CAF50) else Color(0xFFF44336)
        )
    )

    Row(modifier = Modifier.fillMaxWidth().padding(top = if (isFirst)  16.dp else 0.dp)) {
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
                .padding(bottom = 16.dp),
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
    val puntsRepo = PuntsRepository(context)

    val punts = remember { puntsRepo.llegirPunts() }

    val mapView = MapView(context)
    val mapController = mapView.controller

    //Una vista per a veure el mapa
    AndroidView(
        factory = { context ->
            mapView.setTileSource(TileSourceFactory.MAPNIK)//tipus de mapa
            mapView.setMultiTouchControls(true)//que es pugui tocar
            mapController.setZoom(15.0)//zoom aplicat a l'inici
            mapController.setCenter(GeoPoint(41.983, 2.824)) //coordenades de l'inici

            var color = ContextCompat.getColor(context, R.color.ruta1)

            //rutes

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
            polyline2.setPoints(ruta2Coords)
            polyline2.setColor(Color.rgb(125, 0, 125)) // Color lila
            polyline2.getPaint().setStrokeCap(Paint.Cap.ROUND);
            polyline2.width = 8.0f // gruix de la línia
            mapView.overlays.add(polyline2)

            val polyline3 = Polyline()
            polyline3.setPoints(ruta3Coords)
            polyline3.setColor(Color.rgb(0, 73, 136)) // Color blau
            polyline3.getPaint().setStrokeCap(Paint.Cap.ROUND);
            polyline3.width = 8.0f // gruix de la línia
            mapView.overlays.add(polyline3)

            /*val polyline1 = Polyline()
            polyline1.setPoints(ruta1Coords)
            polyline1.setColor(Color.rgb(0, 168, 132)) // Color verd
            polyline1.getPaint().setStrokeCap(Paint.Cap.ROUND);
            polyline1.width = 8.0f // gruix de la línia
            mapView.overlays.add(polyline1)*/

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