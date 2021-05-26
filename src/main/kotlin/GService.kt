import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface GService {
    @GET("{id}")
    suspend fun getG(@Path("id")id:Long ): Response<User>
}