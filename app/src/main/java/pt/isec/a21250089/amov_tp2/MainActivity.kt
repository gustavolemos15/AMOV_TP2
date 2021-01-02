package pt.isec.a21250089.amov_tp2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //to do: should verify if the network is available

        btnServer.setOnClickListener {
            startGame(SERVER_MODE)
        }
        btnClient.setOnClickListener {
            startGame(CLIENT_MODE)
        }
    }

    fun startGame(mode : Int) {
        val intent = Intent(this,GameActivity::class.java).apply {
            putExtra("mode",mode)
        }
        startActivity(intent)
    }
}