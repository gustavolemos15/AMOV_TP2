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
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.getField
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.dialog_server_mode.*
import kotlinx.android.synthetic.main.dialog_server_mode.view.*
import kotlin.concurrent.thread

const val SERVER_MODE = 0
const val CLIENT_MODE = 1

const val TAG = "Localizacao"
class GameActivity : AppCompatActivity(), LocationListener, OnMapReadyCallback {
    private var dlg: AlertDialog? = null
    lateinit var nomeEquipa :String
    var nPlayers :Int = 0
    lateinit var idJogador : String
    var db = FirebaseFirestore.getInstance()

    var PosAtual = LatLng(40.1925, -8.4128)
    lateinit var fLoc : FusedLocationProviderClient
    var locActive = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        nomeEquipa = intent.getStringExtra(Constants.INTENT_NOME_EQUIPA).toString()
        nPlayers = intent.getIntExtra(Constants.INTENT_NJOGADORES, 0)
        idJogador = intent.getStringExtra(Constants.INTENT_IDJOGADOR).toString()

        val docRef = db.collection(Constants.COLLECTION).document("${nomeEquipa}")


        Log.d("doc", "Cheguei: ${idJogador}")

        var i = 11
        var fl = false

        fLoc = FusedLocationProviderClient(this)
        //esta linha tem de ser depois do setContentView
        (supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment)?.getMapAsync(this)

        thread {
            do {
                var data = hashMapOf(
                    "${idJogador}" to arrayListOf(i, i)
                )
                db.collection("Equipas").document("${nomeEquipa}").set(data, SetOptions.merge())
                i++

                Thread.sleep(5000)
                docRef.get()
                    .addOnSuccessListener { document ->
                        if (document != null) {
                            Log.d("doc", "DocumentSnapshot data: ${document.data}")
                        } else {
                            Log.d("doc", "No such document")
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.d("doc", "get failed with ", exception)
                    }
                if(i == 20)
                    fl = true
            }while(!fl)
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

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
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
            }
        }
    }

    val ISEC = LatLng(40.1925, -8.4115)
    val DEIS = LatLng(40.1925, -8.4128)

    //a linha a abaixo diz ao AS que nós temos a certeza que neste momento já temos a permissão necessária
    @SuppressLint("MissingPermission")
    override fun onMapReady(map : GoogleMap?) {
        map ?: return
        map.isMyLocationEnabled = true
        map.mapType = GoogleMap.MAP_TYPE_HYBRID
        map.uiSettings.isCompassEnabled = true
        map.uiSettings.isZoomControlsEnabled = true
        map.uiSettings.isZoomGesturesEnabled = true
        val cp = CameraPosition.Builder().target(ISEC).zoom(17f)
                .bearing(0f).tilt(0f).build()
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cp))
        /*map.add
        //map.addPolygon()
        val mo = MarkerOptions().position(ISEC).title("ISEC-IPC")
            .snippet("Instituto Superior de Engenharia de Coimbra")
        val isec = map.addMarker(mo)
        isec.showInfoWindow()
        map.addMarker(MarkerOptions().position(DEIS).title("DEIS-ISEC"))*/
    }
}



