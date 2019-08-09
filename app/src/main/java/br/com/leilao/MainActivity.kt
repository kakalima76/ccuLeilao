package br.com.leilao

import android.Manifest.permission.*
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import br.com.leilao.models.Item
import br.com.leilao.models.Lacre
import br.com.leilao.models.base64Converter
import br.com.leilao.retrofitBase.services.RetrofitBase
import com.google.android.material.floatingactionbutton.FloatingActionButton
import id.zelory.compressor.Compressor
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.*
import java.io.File
import java.io.IOException
import java.lang.IndexOutOfBoundsException
import java.text.SimpleDateFormat
import java.util.*
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


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
    private val PERMISSION_REQUEST_CODE: Int = 1
    private var mCurrentPhotoPath: String? = null;
    private var toast = this
    val GALLERY_REQUEST_CODE = 1
    val CAMERA_REQUEST_CODE = 102
    lateinit var base64String: String
    lateinit var item: Item
    var path = ""


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

            if (checkPersmission()){
                captureFromCamera()
                Toast.makeText(this,  Environment.DIRECTORY_DCIM, Toast.LENGTH_LONG).show()
            } else {
                requestPermission()
                Toast.makeText(this, BuildConfig.APPLICATION_ID, Toast.LENGTH_LONG).show()
            }

        })

        buscar.setOnClickListener(View.OnClickListener {
            buscarlacre()
            contarItens()
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


    GlobalScope.launch { // launch a new coroutine in background and continue
        delay(3000L) // non-blocking delay for 1 second (default time unit is ms)
        item = Item(
            lacre = editTExtLacre.text.toString(),
            processo = processo.text.toString(),
            posicao = posicao.text.toString(),
            grupo = grupo,
            path = path
        )

        Log.e("teu cu", item.path)

        val callSalvar = RetrofitBase().itemService().salvarItem(item)

        callSalvar.enqueue(object: Callback<Item?> {
            override fun onFailure(call: Call<Item?>, t: Throwable) {
                Log.e("onFailure error", t?.message)
            }

            override fun onResponse(call: Call<Item?>, response: Response<Item?>) {
                //Toast.makeText(toast, "item salvo com sucesso", Toast.LENGTH_LONG).show()
                Log.e("resposta", "com sucesso")
            }
        })


        }
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
            //Toast.makeText(this, "Informe todos os campos", Toast.LENGTH_LONG).show()
        }else{
            path = base64Converter().encoder(mCurrentPhotoPath)
            Log.e("teu cu", path)
            Thread.sleep(3000L) // block main thread for 2 seconds to keep JVM alive
        }





    }

    private fun salvarImagemPath() {
        val file: File = File(mCurrentPhotoPath)
        val fileReqBody: RequestBody = file.asRequestBody("image/".toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("upload", file.name, fileReqBody)
        val description = "image-type".toRequestBody("text/plain".toMediaTypeOrNull())
        val callImage = RetrofitBase().itemService().uploadImage(part, description)

        callImage.enqueue(object : Callback<ResponseBody?> {
            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                Log.e("onFailure error", t?.message)
            }

            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                Toast.makeText(toast, "imagem enviada", Toast.LENGTH_LONG).show()
            }
        })

    }

    //endregion


    //region CameraApp


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {

                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                    captureFromCamera()

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

    private fun checkPersmission(): Boolean {
        return (
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED
        )
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE,  INTERNET, CAMERA), PERMISSION_REQUEST_CODE)
    }


    private var cameraFilePath: String? = null
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        //This is the directory in which the file will be created. This is the default location of Camera photos
        val storageDir = File(
            Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM
            ), "Camera"
        )
        val image = File.createTempFile(
            imageFileName, /* prefix */
            ".jpg", /* suffix */
            storageDir      /* directory */
        )
        // Save a file: path for using again
        mCurrentPhotoPath = image.absolutePath
        cameraFilePath = "file://" + image.absolutePath

        return image
    }

    private fun captureFromCamera() {
        try {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(
                MediaStore.EXTRA_OUTPUT,
                FileProvider.getUriForFile(this, "br.com.leilao.fileprovider", createImageFile())
            )


            startActivityForResult(intent, CAMERA_REQUEST_CODE)
        } catch (ex: IOException) {
            ex.printStackTrace()
            Log.e("Erro: ", "error", ex)

        }

    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Result code is RESULT_OK only if the user captures an Image
        if (resultCode == Activity.RESULT_OK)

            when (requestCode) {
                CAMERA_REQUEST_CODE -> {//imageView.setImageURI(Uri.parse(cameraFilePath))
                    salvar.visibility = View.VISIBLE
                }
            }
    }

        //endregion


}
