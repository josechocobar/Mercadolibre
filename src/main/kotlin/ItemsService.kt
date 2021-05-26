import retrofit2.Response
import retrofit2.http.POST
import retrofit2.http.Query

interface ItemsService {


    @POST("search")
    suspend fun getG(@Query("nickname")id:String ): Response<Results>
}