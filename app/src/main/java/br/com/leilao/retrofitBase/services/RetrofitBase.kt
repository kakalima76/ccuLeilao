package br.com.leilao.retrofitBase.services

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class RetrofitBase {

        private val okHttpclient = OkHttpClient.Builder()
            .build()
        private val retrofit = Retrofit.Builder()
            .baseUrl("https://ccu-leilao.herokuapp.com/")
            .client(okHttpclient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()


        fun itemService() : ItemService{
            return retrofit.create(ItemService::class.java)
        }




}