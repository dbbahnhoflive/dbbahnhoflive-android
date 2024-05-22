package de.deutschebahn.bahnhoflive.util

import android.content.Context
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

object AssetX
{
    fun loadAssetAsString(context: Context, fileName:String) : String {
        val `is`: InputStream
        try {
            `is` = context.resources.assets.open(fileName)

                val inputStreamReader = InputStreamReader(`is`, StandardCharsets.UTF_8)
                val br = BufferedReader(inputStreamReader)
                var line: String?
                val sb = StringBuilder()
                while ((br.readLine().also { line = it }) != null) {
                    sb.append(line)
                }
                return sb.toString()

        } catch (e: IOException) {
            e.printStackTrace()
        }

        return ""
    }


}