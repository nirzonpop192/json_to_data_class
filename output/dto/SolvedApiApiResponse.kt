import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class SolvedApiApiResponse(
    @Expose   
    @SerializedName("data")   
    val data: DataDto,

    @Expose   
    @SerializedName("success")   
    val success: Boolean?,

    @Expose   
    @SerializedName("extra")   
    val extra: Any?,

    @Expose   
    @SerializedName("message")   
    val message: Any?,

    @Expose   
    @SerializedName("status")   
    val status: Int?
)

data class DataDto(
    @Expose   
    @SerializedName("pagination")   
    val pagination: PaginationDto,

    @Expose   
    @SerializedName("topics")   
    val topics: List<TopicDto>
)

data class TopicDto(
    @Expose 
    @SerializedName("comment_count")
    val commentCount: Int?,

    @Expose 
    @SerializedName("is_visible")
    val isVisible: Boolean?,

    @Expose 
    @SerializedName("title_bn")
    val titleBn: String?,

    @Expose 
    @SerializedName("created_at")
    val createdAt: String?,

    @Expose 
    @SerializedName("title_en")
    val titleEn: String?,

    @Expose 
    @SerializedName("banner_url")
    val bannerUrl: String?,

    @Expose 
    @SerializedName("is_comments_open")
    val isCommentsOpen: Boolean?,

    @Expose   
    @SerializedName("id")   
    val id: Int?,

    @Expose   
    @SerializedName("slug")   
    val slug: String?
)

data class PaginationDto(
    @Expose 
    @SerializedName("per_page")
    val perPage: Int?,

    @Expose   
    @SerializedName("total")   
    val total: Int?,

    @Expose 
    @SerializedName("last_page")
    val lastPage: Int?,

    @Expose 
    @SerializedName("current_page")
    val currentPage: Int?
)