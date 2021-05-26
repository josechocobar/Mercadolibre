import com.google.gson.annotations.SerializedName

data class Results (
    @SerializedName("results")
    var list: List<Item>? = null
        )