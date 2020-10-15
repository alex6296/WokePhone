package com.example.wokephone.utilz

import android.content.Context
import android.util.Log
import com.example.wokephone.LocationPoint
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException


class JsonReader {

    fun loadFile(context: Context,fileName: String): List<LocationPoint>?{
        val jsonString = getJsonDataFromAsset(context,fileName)

     return convertToObject(context,jsonString)
    }

    private fun getJsonDataFromAsset(context: Context, fileName: String): String? {
        val jsonString: String
        try {
            jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            return null
        }
        return jsonString
    }

    private fun convertToObject(context: Context, jsonString: String?):List<LocationPoint>?{
        if (jsonString != null) {
            Log.i("data", jsonString)
        }

        val gson = Gson()
        val listPointType = object : TypeToken<List<LocationPoint>>() {}.type

        var points: List<LocationPoint> = gson.fromJson(jsonString, listPointType)
        points.forEachIndexed { idx, point -> Log.i("data", "> Item $idx:\n$point") }
        return  points
    }
}
