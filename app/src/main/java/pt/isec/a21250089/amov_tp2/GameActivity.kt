package pt.isec.a21250089.amov_tp2

import android.content.DialogInterface
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
import androidx.lifecycle.ViewModelProvider
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

class GameActivity : AppCompatActivity() {
    private var dlg: AlertDialog? = null
    lateinit var nomeEquipa :String
    var nPlayers :Int = 0
    lateinit var idJogador : String
    var db = FirebaseFirestore.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        nomeEquipa = intent.getStringExtra(Constants.INTENT_NOME_EQUIPA).toString()
        nPlayers = intent.getIntExtra(Constants.INTENT_NJOGADORES, 0)
        idJogador = intent.getStringExtra(Constants.INTENT_IDJOGADOR).toString()

        val docRef = db.collection(Constants.COLLECTION).document("${nomeEquipa}")


        //onObservarDados()

        Log.d("doc", "Cheguei: ${idJogador}")

        var i = 11
        var fl = false

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

    fun onObservarDados() {
        db.collection(Constants.COLLECTION).document("${nomeEquipa}")
            .addSnapshotListener { docSS, e ->
                if (e!=null) {
                    Log.i("doc", " erro")

                }
                if (docSS!=null && docSS.exists()) {
                    val coor = docSS.getField<ArrayList<Float>>("${idJogador}")
                    Log.i("doc", " Li $coor")
                }
            }
    }

}



