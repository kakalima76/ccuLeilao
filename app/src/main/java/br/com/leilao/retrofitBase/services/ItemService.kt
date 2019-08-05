package br.com.leilao.retrofitBase.services

import br.com.leilao.models.Item
import retrofit2.Call
import retrofit2.http.GET

interface ItemService {

    @GET("/contar")
    //abstract fun Contar(): Call<Int>

    //parei aqui. É preciso implementar o post quey kotrim

    fun total() : Call<Int>
    fun inserir(): Call<Item>

}