package pt.isec.a21250089.amov_tp2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class WaitingRoomActivity : AppCompatActivity() {

    lateinit var nomeEquipa :String
    lateinit var idJogador :String
    var nPlayers :Int = 0
    var db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_waiting_room)

        nomeEquipa = intent.getStringExtra(Constants.INTENT_NOME_EQUIPA).toString()
        nPlayers = intent.getIntExtra(Constants.INTENT_NJOGADORES,0)
        idJogador = intent.getStringExtra(Constants.INTENT_IDJOGADOR).toString()

        var fl = false
        var info = null

        do{
            val docRef = db.collection(Constants.COLLECTION).document("${nomeEquipa}")
            docRef.get()
                    .addOnSuccessListener { document ->
                        if (document != null && document.exists()) {
                            println( "DocumentSnapshot data: ${document.data}")
                            fl = document.getBoolean(Constants.FLAG)!!
                            println(fl)
                        } else {
                            println("No such document")
                        }
                    }
                    .addOnFailureListener { exception ->

                    }

        }while (!fl)

        val intent = Intent(this,WaitingRoomActivity::class.java)
        intent.putExtra(Constants.INTENT_NOME_EQUIPA,nomeEquipa)
        intent.putExtra(Constants.INTENT_NJOGADORES, nPlayers)
        intent.putExtra(Constants.INTENT_IDJOGADOR, idJogador)
        startActivity(intent)
    }
}