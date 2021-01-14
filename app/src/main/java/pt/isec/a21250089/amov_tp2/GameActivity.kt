package pt.isec.a21250089.amov_tp2

import android.content.DialogInterface
import android.net.wifi.WifiManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.dialog_server_mode.*
import kotlinx.android.synthetic.main.dialog_server_mode.view.*

const val SERVER_MODE = 0
const val CLIENT_MODE = 1

class GameActivity : AppCompatActivity() {
    private var dlg: AlertDialog? = null
    lateinit var nomeEquipa :String
    var nPlayers :Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        nomeEquipa = intent.getStringExtra("NOME").toString()
        nPlayers = intent.getIntExtra("NPLAYERS",0)

        println(nomeEquipa)
        println(nPlayers)
    }

}



