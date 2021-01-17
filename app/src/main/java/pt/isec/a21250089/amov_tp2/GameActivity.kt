package pt.isec.a21250089.amov_tp2

import android.Manifest
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.net.wifi.WifiManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.getField
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_game.*
import kotlinx.android.synthetic.main.dialog_server_mode.*
import kotlinx.android.synthetic.main.dialog_server_mode.view.*
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlin.concurrent.thread
import kotlin.math.roundToInt

const val SERVER_MODE = 0
const val CLIENT_MODE = 1

const val TAG = "Localizacao"
class GameActivity : AppCompatActivity(), LocationListener, OnMapReadyCallback {
    private var dlg: AlertDialog? = null
    lateinit var nomeEquipa :String
    var nPlayers :Int = 0
    lateinit var idJogador : String
    var db = FirebaseFirestore.getInstance()
    var jogadores = mutableListOf<Jogador>()

    var marcadores = mutableListOf<Marker>()
    var marcOptions = mutableListOf<MarkerOptions>()
    val ISEC = LatLng(40.1925, -8.4115)

    private lateinit var markerOptions: MarkerOptions
    private lateinit var marker: Marker
    private lateinit var cameraPosition: CameraPosition
    lateinit var googleMap:GoogleMap

    var PosAtual = LatLng(40.1925, -8.4128)
    lateinit var fLoc : FusedLocationProviderClient
    var locActive = false

    var maisProx = 0.00
    var maisProx2= 0.00
    var idJog1 = 0
    var idJog2 = 0
    var angulo = 0
    var a1 = Location("a1")
    var a2 = Location("a2")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        nomeEquipa = intent.getStringExtra(Constants.INTENT_NOME_EQUIPA).toString()
        nPlayers = intent.getIntExtra(Constants.INTENT_NJOGADORES, 0)
        idJogador = intent.getStringExtra(Constants.INTENT_IDJOGADOR).toString()

        val docRef = db.collection("${nomeEquipa}")


        Log.d("doc", "Cheguei: ${idJogador}")

        fLoc = FusedLocationProviderClient(this)
        tvAnguloNecessario.text = ((nPlayers - 2)*180 / (nPlayers)).toString()

        // Prepara Mapa
        var i = 1
        while(i <= nPlayers) {
            jogadores.add(Jogador(40.1925, -8.4128, i))
            marcOptions.add(MarkerOptions().position(ISEC).title("${i}"))
            //marcadores[i] = googleMap.addMarker(markerOptions[i])

            i++
        }
        //markerOptions = MarkerOptions()
        //markerOptions.position(PosAtual).title(idJogador)
        cameraPosition = CameraPosition.Builder()
            .target(PosAtual)
            .zoom(17f).build()

        //esta linha tem de ser depois do setContentView
        (supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment)?.getMapAsync(this)

        var fl = false
        i = 1
        thread {
            do {
                //Prepara data e escreve
                var data = hashMapOf(
                    "latitude" to PosAtual.latitude,
                    "longitude" to PosAtual.longitude
                )
                db.collection("${nomeEquipa}").document("${idJogador}").set(data, SetOptions.merge())


                //Lê dados da firestore
                Thread.sleep(5000)
                for(p in jogadores) {
                    docRef.document("${p.id}").get()
                        .addOnSuccessListener { document ->
                            if (document != null  && document.exists()) {
                                var la = document.getDouble("latitude")!!
                                var lo = document.getDouble("longitude")!!
                                p.lat = la
                                p.long = lo
                                //jogadores.add(Jogador(la,lo,p))
                                //marcadores[p-1].position(LatLng(la,lo)).title("${p}")
                                Log.d("doc", "DocumentSnapshot data: ${document.data}")
                            } else {
                                Log.d("doc", "No such document ${p.id}")
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.d("doc", "get failed with ", exception)
                        }
                }
                var i=0
                for(m in marcadores) {
                    if(jogadores.size > 0)
                        updateMapa(jogadores[i].lat, jogadores[i].long, i, jogadores[i].id.toString())
                    i++
                }

                setDistancias()

            } while(!fl)
        }
    }

    private fun updateMapa(lat: Double, lon : Double, i: Int, id: String) {
        runOnUiThread {
            val newLatLng = LatLng(lat, lon)
            marcadores[i].position = newLatLng
            marcadores[i].title = id
            if(id.compareTo(idJogador) == 0) {
                cameraPosition = CameraPosition.Builder()
                        .target(newLatLng)
                        .zoom(17f).build()
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
            }
            tvDist1.text = "${maisProx.roundToInt()}m"
            tvJog1.text = "${idJog1}= "
            tvDist2.text = "${maisProx2.roundToInt()}m"
            tvJog2.text = "${idJog2}= "
            tvAngulo.text = angulo.toString()
        }
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates(true)
    }

    override fun onPause() {
        super.onPause()
        if(locActive) {
            fLoc.removeLocationUpdates(localtionCallback)
            locActive = false
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        startLocationUpdates(false)
    }

    fun startLocationUpdates(askPerm : Boolean) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED)
        {
            if(askPerm)
                ActivityCompat.requestPermissions(this, arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION), 10)
            else
            //se não dá permissões, termina a aplicação
                finish()
            return
        }

        val locReq = LocationRequest().apply {
            interval = 10000    //apesar de estar 10s em 10s pode receber atualizacoes de outras apps em menos tempo
            fastestInterval = 10000     //este parametro limita a frequencia dos updates para 10s, sem isto pode receber updates de outras fontes mais frequente/
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        fLoc.requestLocationUpdates(locReq, localtionCallback, null)
        locActive = true
    }

    override fun onLocationChanged(location: Location) {
        val latitude = location.latitude
        val longitude = location.longitude
        PosAtual = LatLng(location.latitude, location.longitude)
        Log.i(TAG, "onLocationChanged: $latitude $longitude")
    }

    var localtionCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult?) {
            Log.i(TAG, "onLocationResult: ")
            p0?.locations?.forEach {
                Log.i(TAG, "localtionCallback: ${it.latitude} ${it.longitude}")
                PosAtual = LatLng(it.latitude, it.longitude)
                Log.i(TAG, "onLocationResult: PosAtual(${PosAtual.latitude}, ${PosAtual.longitude}) ")
            }
        }
    }

    //a linha a abaixo diz ao AS que nós temos a certeza que neste momento já temos a permissão necessária
    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap : GoogleMap?) {
        this.googleMap = googleMap!!
        this.googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID
        this.googleMap.uiSettings.isCompassEnabled = true
        this.googleMap.uiSettings.isZoomControlsEnabled = true
        this.googleMap.uiSettings.isZoomGesturesEnabled = true
        var i=0;
        while (i < nPlayers) {
            var mk =  googleMap.addMarker(MarkerOptions().position(ISEC).title("${i+1}"))
            marcadores.add(mk)
            i++
        }
        //marker = googleMap.addMarker(markerOptions)
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

    }

    fun setDistancias() {
        if (jogadores.size == nPlayers) {
            var pos = Location("posAtual")
            pos.latitude = PosAtual.latitude
            pos.longitude = PosAtual.longitude



            for (j in jogadores) {
                var aux = Location("pos")
                aux.latitude = j.lat
                aux.longitude = j.long

                var d = pos.distanceTo(aux).toDouble()
                if(pos.distanceTo(aux).toInt() != 0) {
                    if (maisProx == 0.0) {
                        maisProx = d
                        idJog1 = j.id
                        a1 = aux
                    } else if (maisProx2 == 0.0) {
                        maisProx2 = d
                        idJog2 = j.id
                        a2 = aux
                    } else if (maisProx > d) {
                        maisProx2 = maisProx
                        maisProx = d
                        a2 = a1
                        a1 = aux
                        idJog2 = idJog1
                        idJog1 = j.id
                    } else if (maisProx2 > d && d != maisProx) {
                        maisProx2 = d
                        idJog2 = j.id
                        a2 = aux
                    }
                    var d3 = a1.distanceTo(a2)
                    var ratio = (maisProx*maisProx + maisProx2*maisProx2 - d3*d3) / (2*maisProx*maisProx2)
                    angulo = (Math.acos(ratio)*(180/Math.PI)).toInt()
                }
            }
        }
    }
}

class Jogador(la: Double, lo: Double, i: Int){
    var lat :Double = la
    var long :Double = lo
    var id : Int = i
}

