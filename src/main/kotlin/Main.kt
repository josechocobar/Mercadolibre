import Meli.Companion.user
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess


fun main() {


    val meli = Meli()

    println("Ingrese el id_seller o varios id_seller separados por coma")
    val id_seller = readLine()
    if (id_seller != null) {
        val usersMap = readlineGetID(id_seller)
        if (usersMap.size > 0) {
            for (item in usersMap) {
                val job =
                    MainScope().launch(Dispatchers.IO) {
                        meli.getSellerWithCoroutines(item)
                    }
                runBlocking {
                    job.join()

                    user?.let {
                        val job2 = MainScope().launch(Dispatchers.IO) {

                            meli.getListItems(user!!.nickname,id_seller)
                        }
                        job2.join()
                        exitProcess(1)
                    }
                }
            }
        }

    }


}

fun readlineGetID(users: String): MutableList<Long> {
    val mapOfUsers = mutableListOf<Long>()
    if (users.contains(",")) {
        try {
            val usersList = users.split(",")

            if (usersList.isNotEmpty()) {
                for (item in usersList) {
                    if (!readLineChecker(item)) {
                        mapOfUsers.add(item.toLong())
                    } else {
                        println("el id:$item no es correcto")
                    }
                }
            }
        } catch (e: Exception) {
            println("algun caracter esta mal ${e.message}")
        }
    } else {
        mapOfUsers.add(users.toLong())
    }

    return mapOfUsers


}

fun readLineChecker(ent: String?): Boolean {

    return ent != null && ent.all {
        it.isDigit()
    }
}


data class User(
    @SerializedName("id")
    val id: String,
    @SerializedName("nickname")
    val nickname: String
)


data class Item(
    @SerializedName("id")
    val id: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("category_id")
    val category: String,
    @SerializedName("domain_id")
    val categoryName: String
)



class Meli {
    companion object {
        var user: User? = null
    }

    suspend fun getSellerWithCoroutines(id: Long) {
        val code = RetrofitMeli.gServices.getG(id).code()
        when (code) {
            200 -> {
                val body = RetrofitMeli.gServices.getG(id).body()

                user = body
            }
            else -> {
                println("fallo porque $code")
            }

        }


    }

    object RetrofitMeli {

        val url = "https://api.mercadolibre.com/users/"
        val gclient: OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .callTimeout(5, TimeUnit.SECONDS)
            .build()
        val gServices: GService by lazy {
            Retrofit.Builder()
                .baseUrl(url)
                .client(gclient)
                .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
                .build().create(GService::class.java)
        }
        val site = "MLA"
        val urlItems = "https://api.mercadolibre.com/sites/$site/"
        val itemsService: ItemsService by lazy {
            Retrofit.Builder()
                .baseUrl(urlItems)
                .client(gclient)
                .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
                .build().create(ItemsService::class.java)
        }

    }

    suspend fun getListItems(userNickname: String, id:String) {

        val code = RetrofitMeli.itemsService.getG(userNickname).code()
        when (code) {
            200 -> {

                val list = RetrofitMeli.itemsService.getG(userNickname).body()?.list

                if (list?.size!! > 0) {
                    val file = getAllItems(list)
                    printCsv(file, userNickname,id)
                }


            }
            else -> {
                println("Error en la busqueda de items ${user!!.nickname} $code")
            }
        }
    }

    fun getAllItems(list: List<Item>): String {
        var csv = ""
        for (item in list) {
            csv += "id : ${item.id},title : ${item.title},category_id : ${item.category},category_name : ${item.categoryName};\n"
        }
        return csv
    }

    fun checkCsv(csv: String): Boolean {
        return csv == ""
    }

    fun printCsv(csv: String, seller: String,id: String) {
        if (!checkCsv(csv)) {
            val outputStream = FileOutputStream("./${seller}_MLA_$id.log")
            val strToBytes = csv.toByteArray()
            outputStream.write(strToBytes)
            user = null
            outputStream.close()
        }
    }

}