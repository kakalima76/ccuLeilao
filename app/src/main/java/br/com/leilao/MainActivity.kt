package br.com.leilao

import android.Manifest.permission.CAMERA
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory.*
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Environment.getExternalStorageDirectory
import android.os.Environment.getExternalStoragePublicDirectory
import android.provider.MediaStore
import android.text.Editable
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import br.com.leilao.models.GsonConverter
import br.com.leilao.models.Item
import br.com.leilao.models.Lacre
import br.com.leilao.retrofitBase.services.RetrofitBase
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.lang.IndexOutOfBoundsException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    lateinit var posicao: TextView
    lateinit var processo: TextView
    lateinit var gpo: Spinner
    lateinit var buscar: Button
    lateinit var grupo: String
    lateinit var salvar: Button
    lateinit var imagemPath: String
    lateinit var lacre: EditText
    lateinit var btnFotografar: FloatingActionButton
    val REQUEST_IMAGE_CAPTURE = 1
    private val PERMISSION_REQUEST_CODE: Int = 101
    private var mCurrentPhotoPath: String? = null;
    private var toast = this


    //region InterfaceApp

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        gpo = findViewById(R.id.ItensGrupo)
        lacre = findViewById(R.id.editTExtLacre)
        btnFotografar = findViewById(R.id.fab_camera)
        buscar = findViewById(R.id.Buscar)
        posicao = findViewById(R.id.Posicao)
        processo = findViewById(R.id.Processo)
        salvar = findViewById(R.id.Salvar)

        salvar.visibility = View.INVISIBLE

        val listaGrupos = arrayOf<String>(
            "",
            "ELETRODOMESTICO",
            "ELETROELETONICO",
            "EQUIPAMENTO",
            "FERRAMENTAS",
            "MAQUINA",
            "MESAS",
            "MODULO DE ALIMENTACAO",
            "MODULO DE ATIVIDADE",
            "MOENDA",
            "TRANSPORTE"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_expandable_list_item_1, listaGrupos)

        gpo.adapter = adapter

        gpo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
                grupo = listaGrupos[i]
            }
            override fun onNothingSelected(adapterView: AdapterView<*>) {
            }
        }

        lacre.setOnTouchListener { v, event ->
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    this.lacre.setText("")
                    this.processo.text = ""
                    this.posicao.text = ""
                }
            }

            v?.onTouchEvent(event) ?: true
        }

        btnFotografar.setOnClickListener(View.OnClickListener {

            if (checkPersmission()) takePicture() else requestPermission()
        })

        buscar.setOnClickListener(View.OnClickListener {
            buscarlacre()
            contarItens()

//            Toast.makeText(this, getExternalStorageDirectory().toString(), Toast.LENGTH_SHORT).show()
//            Toast.makeText(this, filesDir.toString(), Toast.LENGTH_SHORT).show()

        })

        salvar.setOnClickListener(View.OnClickListener {
           salvarItem()

        })

    }//endregion


    //region Retrofit
    private fun contarItens(){

        val callGet = RetrofitBase().itemService().total()

        callGet.enqueue(object : Callback<Int?> {
            override fun onFailure(call: Call<Int?>, t: Throwable) {
                Log.e("onFailure error", t?.message)
            }

            override fun onResponse(call: Call<Int?>, response: Response<Int?>) {
                var pos: Int? = response.body()
                if(pos !== null){
                    pos+=1
                    posicao.text = (pos).toString()
                }

            }
        })


    }

    private fun buscarlacre(){
        val numeroLacre = Lacre()
        numeroLacre.numero = editTExtLacre.text.toString()
        val callPost = RetrofitBase().itemService().buscarLacre(numeroLacre)
        callPost.enqueue(object: Callback<List<Lacre>?> {
            override fun onFailure(call: Call<List<Lacre>?>, t: Throwable) {
                Log.e("onFailure error", t?.message)
            }

            override fun onResponse(call: Call<List<Lacre>?>, response: Response<List<Lacre>?>) {
               try {
                   processo.text = response.body()!![0].processo
               }catch (err : IndexOutOfBoundsException){
                   processo.text = "SEM PROCESSO"
               }
            }
        })
    }

    private fun salvarItem(){

        if(
            editTExtLacre.text.toString().isNullOrBlank()
            ||
            processo.text.toString().isNullOrBlank()
            ||
            posicao.text.toString().isNullOrBlank()
            ||
            grupo.isNullOrBlank()
            ||
            mCurrentPhotoPath.isNullOrBlank()
        ){
            Toast.makeText(this, "Informe todos os campos", Toast.LENGTH_LONG).show()
        }else{
            val item = Item(
                lacre = editTExtLacre.text.toString(),
                processo = processo.text.toString(),
                posicao = posicao.text.toString(),
                grupo = grupo,
                path = mCurrentPhotoPath
            )

            val callSalvar = RetrofitBase().itemService().salvarItem(item)

            callSalvar.enqueue(object: Callback<Item?> {
                override fun onFailure(call: Call<Item?>, t: Throwable) {
                    Log.e("onFailure error", t?.message)
                }

                override fun onResponse(call: Call<Item?>, response: Response<Item?>) {
                    Toast.makeText(toast, "item salvo com sucesso", Toast.LENGTH_LONG).show()
                }
            })
        }

        Toast.makeText(this, mCurrentPhotoPath, Toast.LENGTH_LONG).show()
    }

    //endregion


    //region CameraApp


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {

                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                    takePicture()

                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
                return
            }

            else -> {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }




    private fun takePicture() {

        val intent: Intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val file: File = createFile()

        //Toast.makeText(this, getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString(), Toast.LENGTH_SHORT).show()

        val uri: Uri = FileProvider.getUriForFile(
            this,
            "br.com.leilao.fileprovider",
            file
        )
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {

            val auxFile = File(mCurrentPhotoPath)
            var bitmap: Bitmap = decodeFile(mCurrentPhotoPath)
            salvar.visibility = View.VISIBLE

        }
    }

    private fun checkPersmission(): Boolean {
        return (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
            android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(READ_EXTERNAL_STORAGE, CAMERA), PERMISSION_REQUEST_CODE)
    }

    @Throws(IOException::class)
    private fun createFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            mCurrentPhotoPath = this.absolutePath
        }
    }//endregion


}
