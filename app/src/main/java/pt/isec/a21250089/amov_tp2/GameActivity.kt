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
    private lateinit var model: GameViewModel
    private var dlg: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        model = ViewModelProvider(this).get(GameViewModel::class.java)

        model.connectionState.observe(this) {
            if (it != GameViewModel.ConnectionState.SETTING_PARAMETERS &&
                it != GameViewModel.ConnectionState.SERVER_CONNECTING && dlg?.isShowing == true) {
                dlg?.dismiss()
                dlg = null
            }

            if (it == GameViewModel.ConnectionState.CONNECTION_ERROR ||
                it == GameViewModel.ConnectionState.CONNECTION_ENDED)
                finish()
        }

        if (model.connectionState.value != GameViewModel.ConnectionState.CONNECTION_ESTABLISHED) {
            when (intent.getIntExtra("mode", SERVER_MODE)) {
                SERVER_MODE -> startAsServer()
                CLIENT_MODE -> startAsClient()
            }
        }

    }

    private fun startAsServer() {
        val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        val ip = wifiManager.connectionInfo.ipAddress
        val strIPAddress = String.format("%d.%d.%d.%d",
            ip and 0xff,
            (ip shr 8) and 0xff,
            (ip shr 16) and 0xff,
            (ip shr 24) and 0xff
        )


        var dlg = AlertDialog.Builder(this)
        val inflater = layoutInflater
        dlg.setTitle("With EditText")
        val dialogLayout = inflater.inflate(R.layout.dialog_server_mode, null)
        val textView  = dialogLayout.findViewById<TextView>(R.id.tvServerDialog)
        textView.text = "IP: ${strIPAddress}"
        dlg.setView(dialogLayout)
        dlg.show()
    }

    private fun startAsClient() {
            var filtros = arrayOf(object : InputFilter {
                override fun filter(
                        source: CharSequence?,
                        start: Int,
                        end: Int,
                        dest: Spanned?,
                        dstart: Int,
                        dend: Int
                ): CharSequence? {
                    if (source?.none { it.isDigit() || it == '.' } == true)
                        return ""
                    return null
                }
            })

        var dl = AlertDialog.Builder(this)
        dl.setTitle("Client Modde")
        dl.setMessage("IP: ")
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dialog_client_mode, null)
        var btnPos = dialogLayout.findViewById<Button>(R.id.btnClientPos)
        var btnNeutro = dialogLayout.findViewById<Button>(R.id.btnClientNeutral)
        var btnNeg = dialogLayout.findViewById<Button>(R.id.btnClientNeg)
        var edit = dialogLayout.findViewById<EditText>(R.id.etClientDialog)
        edit.filters = filtros
        btnPos.setOnClickListener(){ v ->
            val strIP = edit.text.toString()
            if (strIP.isEmpty() || !Patterns.IP_ADDRESS.matcher(strIP).matches()) {
                Toast.makeText(this@GameActivity, "Erro", Toast.LENGTH_LONG).show()
                finish()
            } else {
                //model.startClient(edtBox.text.toString())
            }
        }
        btnNeutro.setOnClickListener(){ v ->
                // model.startClient("10.0.2.2", SERVER_PORT-1)
                // Add port redirect on the Server Emulator:
                // telnet localhost <5554|5556|5558|...>
                // auth <key>
                // redir add tcp:9998:9999
            }
        btnNeg.setOnClickListener(){ v ->
                finish()
            }
        dl.setView(dialogLayout)
        dl.show()
    }
}

private fun Button.setOnClickListener(string: String.Companion) {

}

