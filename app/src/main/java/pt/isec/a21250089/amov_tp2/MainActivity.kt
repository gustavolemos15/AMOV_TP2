package pt.isec.a21250089.amov_tp2

import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.wifi.WifiManager
import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.util.Patterns
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import java.io.InputStream
import java.io.OutputStream
import java.io.PrintStream
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread

//  API KEY -> AIzaSyDzAHftYPxUaqg4QTyBaINUEvn_BH3Vlik
class MainActivity : AppCompatActivity() {
    val SERVER_PORT: Int = 9999
    var nome :String = ""
    var nplayers :Int = 2
    var idPlayer :String = ""
    private var socket: Socket? = null
    private val socketI: InputStream?
        get() = socket?.getInputStream()
    private val socketO: OutputStream?
        get() = socket?.getOutputStream()
    var serverSocket: ServerSocket? = null
    var strIpAdress :String = ""
    var db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //to do: should verify if the network is available

        val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        val ip = wifiManager.connectionInfo.ipAddress
        strIpAdress = String.format(
            "%d.%d.%d.%d",
            ip and 0xff,
            (ip shr 8) and 0xff,
            (ip shr 16) and 0xff,
            (ip shr 24) and 0xff
        )
        println(strIpAdress)

        btnServer.setOnClickListener {
            startServerMode();
        }
        btnClient.setOnClickListener {
            clientMode()
        }
    }


    //TODO: colocar stings nos res
    fun startServerMode() {
        val edNomeEquipa = EditText(this).apply {
            maxLines = 1
        }
        val edNumJogadores = EditText(this).apply {
            maxLines = 1
        }
        var ll = LinearLayout(this).apply {
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            this.setPadding(50, 50, 50, 50)
            layoutParams = params
            setBackgroundColor(Color.rgb(240, 224, 208))
            orientation = LinearLayout.VERTICAL
            addView(TextView(context).apply {
                val paramsTV = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                layoutParams = paramsTV
                text = String.format(context.getString(R.string.nomeEquipa))
                textSize = 20f
                setTextColor(Color.rgb(96, 96, 32))
                textAlignment = View.TEXT_ALIGNMENT_CENTER
            })
            addView(edNomeEquipa)
            addView(TextView(context).apply {
                val paramsTV = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                layoutParams = paramsTV
                text = String.format(context.getString(R.string.numElem))
                textSize = 20f
                setTextColor(Color.rgb(96, 96, 32))
                textAlignment = View.TEXT_ALIGNMENT_CENTER
            })
            addView(edNumJogadores)
        }

        val dlg = AlertDialog.Builder(this).run {
            setTitle(getString(R.string.novoJogo))
            setPositiveButton(getString(R.string.conectar)) { _: DialogInterface, _: Int ->
                var fl = true
                nome = edNomeEquipa.text.toString()
                nplayers = edNumJogadores.text.toString().toInt()
                if (nome.isEmpty()) {
                    Toast.makeText(this@MainActivity, getString(R.string.erro), Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    var i = 1
                    while (i <= nplayers){

                        var doc = hashMapOf(
                            "latitude" to 0,
                            "longitude" to  0,
                            "nPlayers" to nplayers
                        )
                        db.collection("${nome}").document("${i}").set(doc)
                        i++
                    }
                    conectServer()

                }
            }
                setNegativeButton(getString(R.string.voltar)) { _: DialogInterface, _: Int ->
                    finish()
                }
                setCancelable(false)
                setView(ll)
                create()
            }
            dlg.show()
        }

    fun conectServer(){

            val ll = LinearLayout(this).apply {
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                this.setPadding(50, 50, 50, 50)
                layoutParams = params
                setBackgroundColor(Color.rgb(240, 224, 208))
                orientation = LinearLayout.HORIZONTAL
                addView(ProgressBar(context).apply {
                    isIndeterminate = true
                    val paramsPB = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    paramsPB.gravity = Gravity.CENTER_VERTICAL
                    layoutParams = paramsPB
                    indeterminateTintList = ColorStateList.valueOf(Color.rgb(96, 96, 32))
                })
                addView(TextView(context).apply {
                    val paramsTV = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    layoutParams = paramsTV
                    text = "IP: ${strIpAdress}"
                    textSize = 20f
                    setTextColor(Color.rgb(96, 96, 32))
                    textAlignment = View.TEXT_ALIGNMENT_CENTER
                })
            }

            var dlg = AlertDialog.Builder(this).run {
                setTitle(getString(R.string.titulo))
                setView(ll)
                setOnCancelListener {
                    serverSocket?.close()
                    serverSocket = null
                    finish()
                }
                create()
            }
        if (serverSocket != null || socket != null)
            return

        thread {
            var i = 1
            serverSocket = ServerSocket(SERVER_PORT)
            do {
                var menssagem: Mensagem = Mensagem(nome, "${i + 1}", nplayers)
                var strMenssagem :String = Gson().toJson(menssagem)
                serverSend(serverSocket!!.accept(), strMenssagem)
                i++
            }while (i < nplayers)

            val intent = Intent(this, GameActivity::class.java)
            intent.putExtra(Constants.INTENT_NOME_EQUIPA, nome)
            intent.putExtra(Constants.INTENT_NJOGADORES, nplayers)
            intent.putExtra(Constants.INTENT_IDJOGADOR, "1")
            startActivity(intent)
       }
            dlg?.show()

        }

    fun serverSend(newSocket: Socket, menssagem: String){
                socket = newSocket
           try{
                //val bufI = socketI!!.bufferedReader()
                //val message = bufI.readLine()
                socketO?.run {
                    try {
                        val printStream = PrintStream(this)
                        printStream.println(menssagem)
                        printStream.flush()
                    } catch (_: Exception) {
                    }
                }


            } catch (_: Exception) {
                serverSocket?.close()
                serverSocket = null
            }

    }

    fun clientMode(){
        val edtBox = EditText(this).apply {
            maxLines = 1
            filters = arrayOf(object : InputFilter {
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
        }
        val dlg = AlertDialog.Builder(this).run {
            setTitle(getString(R.string.ligar))
            setMessage(getString(R.string.setIp))
            setPositiveButton(getString(R.string.conectar)) { _: DialogInterface, _: Int ->
                val strIP = edtBox.text.toString()
                if (strIP.isEmpty() || !Patterns.IP_ADDRESS.matcher(strIP).matches()) {
                    Toast.makeText(this@MainActivity, getString(R.string.erro), Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    conectClient(strIP)

                }
            }
            setNegativeButton(getString(R.string.cancelar)) { _: DialogInterface, _: Int ->
                finish()
            }
            setCancelable(false)
            setView(edtBox)
            create()
        }
        dlg.show()

    }

    fun conectClient(ip: String){
        if (socket != null)
            return
        thread {

            try {
                socket = Socket(ip, SERVER_PORT)
                val bufI = socketI!!.bufferedReader()
                var strMen = bufI.readLine()
                var mens = Gson().fromJson(strMen, Mensagem::class.java)
                nome = mens.nome
                println(nome)
                nplayers = mens.nPlayers
                idPlayer = mens.idPlayer
            } catch (_: Exception) {
                finish()
            }

            val intent = Intent(this, GameActivity::class.java)
            intent.putExtra(Constants.INTENT_NOME_EQUIPA, nome)
            intent.putExtra(Constants.INTENT_NJOGADORES, nplayers)
            intent.putExtra(Constants.INTENT_IDJOGADOR, idPlayer)
            startActivity(intent)
        }

    }

    }

class Mensagem(n: String, id: String, np: Int){
    var nome :String = n
    var idPlayer :String = id
    var nPlayers : Int = np
}


