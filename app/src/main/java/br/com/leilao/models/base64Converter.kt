package br.com.leilao.models
import android.content.Context
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.Base64


class base64Converter {

    fun encoder(filePath: String?): String{
        val bytes = File(filePath).readBytes()
        val base64 = Base64.getEncoder().encodeToString(bytes)
        Log.e("cu", "cu")
        return base64
    }

    fun decoder(base64Str: String?, pathFile: String): Unit{
        val imageByteArray = Base64.getDecoder().decode(base64Str)
        File(pathFile).writeBytes(imageByteArray)
    }

    fun main(context: Context) {
        GlobalScope.launch { // launch a new coroutine in background and continue
            delay(3000L) // non-blocking delay for 1 second (default time unit is ms)
            Log.e("teu cu", "World!") // print after delay
            //Toast.makeText(context, "teu cu", Toast.LENGTH_LONG).show()
        }
        Log.e("É meu", "World!") // print after delay
        Thread.sleep(2000L) // block main thread for 2 seconds to keep JVM alive
    }


}