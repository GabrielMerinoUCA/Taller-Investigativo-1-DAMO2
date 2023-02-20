package ni.edu.uca.socket__test

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import ni.edu.uca.socket__test.databinding.ActivityMainBinding
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var ss: ServerSocket
    private lateinit var jobListen: Job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        listen()
        binding.btnSend.setOnClickListener {
            send(binding.etMessage.text.toString())
        }
    }

    fun listen() {
        jobListen = GlobalScope.launch(Dispatchers.IO) {
            var isr: InputStreamReader
            var br: BufferedReader
            var message: String
            ss = ServerSocket(7800)
            while (true) {
                try {
                    if (isActive && !ss.isClosed) {
                        delay(50)
                        Log.wtf("DISCOVER THE SOCKET STATE: ", "" + ss.isClosed)
                        var s = ss.accept() // esta funcion bloquea el codigo que le sigue hasta que reciba respuesta
                        Log.wtf("DISCOVER THE SOCKET STATE: ", "" + ss.isClosed)
                        isr = InputStreamReader(s.getInputStream())
                        br = BufferedReader(isr)
                        message = br.readLine()
                        launch(Dispatchers.Main) {
                            if (binding.tvReceived.text.toString().equals("")) {
                                binding.tvReceived.text = "Android: $message"
                            } else {
                                binding.tvReceived.text =
                                    "${binding.tvReceived.text.toString()} \n $message"
                            }
                        }
                    }
                }catch (ex: Exception){
                    ex.printStackTrace()
                }
            }
        }
    }

    fun send(message: String) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                var s = Socket("192.168.1.5", 7800)// open connection
                var pw = PrintWriter(s.getOutputStream()) // sync datasender to connection target
                pw.write(message) // send message
                pw.flush() // clear buffer
                pw.close() // close sender
                s.close()
            }catch (ex: Exception){
                ex.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        runBlocking {
            jobListen.cancel() // detener el loop de la coroutine
            delay(2000)
            withContext(Dispatchers.IO) {
                try {
                    ss.close() // cerrar el puerto para poder reisntanciar en 7800 (linea 35)
                    ss = ServerSocket() //
                }catch (ex: Exception){
                    ex.printStackTrace()
                }
            }
        }
    }
}