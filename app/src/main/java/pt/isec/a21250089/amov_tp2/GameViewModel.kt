package pt.isec.a21250089.amov_tp2

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.*
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread

class GameViewModel : ViewModel() {

    enum class State {
        STARTING, PLAYING_BOTH, PLAYING_ME, PLAYING_OTHER, ROUND_ENDED, GAME_OVER
    }

    enum class ConnectionState {
        SETTING_PARAMETERS, SERVER_CONNECTING, CLIENT_CONNECTING, CONNECTION_ESTABLISHED,
        CONNECTION_ERROR, CONNECTION_ENDED
    }

    val state = MutableLiveData(State.STARTING)
    val connectionState = MutableLiveData(ConnectionState.SETTING_PARAMETERS)
}