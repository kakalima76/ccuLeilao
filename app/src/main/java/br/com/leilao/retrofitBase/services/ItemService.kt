package br.com.leilao.retrofitBase.services

import br.com.leilao.models.Item
import br.com.leilao.models.Lacre
import org.json.JSONObject
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ItemService {

    @GET("/contar")
    fun total() : Call<Int>

    @POST("/lacre/buscar")
    fun buscarLacre(@Body lacre: Lacre) : Call<List<Lacre>>

    @POST("/")
    fun salvarItem(@Body item: Item) : Call<Item>

}