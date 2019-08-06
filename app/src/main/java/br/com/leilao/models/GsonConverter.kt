package br.com.leilao.models

import com.google.gson.Gson

class GsonConverter {



    fun converter(item: Item): String? {
        val gson = Gson()

        if(item != null){
            gson.toJson(item)
        }

        return null
    }


}