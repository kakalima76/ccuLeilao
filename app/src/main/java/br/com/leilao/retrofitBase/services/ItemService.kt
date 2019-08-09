package br.com.leilao.retrofitBase.services

import br.com.leilao.models.Item
import br.com.leilao.models.Lacre
import org.json.JSONObject
import retrofit2.Call
import okhttp3.RequestBody
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.*


interface ItemService {

    @GET("/contar")
    fun total() : Call<Int>

    @POST("/lacre/buscar")
    fun buscarLacre(@Body lacre: Lacre) : Call<List<Lacre>>

    @POST("/base64")
    fun salvarItem(@Body item: Item) : Call<Item>

    @Multipart
    @POST("/")
    fun uploadImage(@Part file: MultipartBody.Part, @Part("path") requestBody: RequestBody): Call<ResponseBody>

}