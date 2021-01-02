package pt.isec.a21250089.amov_tp2

import android.net.wifi.WifiManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
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
                //CLIENT_MODE -> startAsClient()
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



        dlg = AlertDialog.Builder(this).run {
            setTitle("Server Mode")
            setView(R.layout.dialog_server_mode)
            setOnCancelListener {
                //model.stopServer()
                finish()
            }
            create()
        }
        //model.startServer()
        if(tvServerDialog != null)
            tvServerDialog.text = String.format("IP: ", strIPAddress)
        dlg?.show()
    }

}