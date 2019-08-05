package br.com.leilao.retrofitBase.services

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class RetrofitBase {


        private val retrofit = Retrofit.Builder()
            .baseUrl("https://ccu-leilao.herokuapp.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()


        fun itemService() : ItemService{
            return retrofit.create(ItemService::class.java)
        }




}