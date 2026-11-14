package com.ktuongfx.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

const val WORKER = "https://tmdb-proxy.tuongisdabest.workers.dev"
const val API_KK = "$WORKER/kkphim"
const val IMG_KK = "$WORKER/kkphimimg/"

@Serializable
data class Movie(
    val name: String = "",
    val slug: String = "",
    val origin_name: String = "",
    val thumb_url: String = "",
    val poster_url: String = "",
    val year: String = "",
    val quality: String = "",
    val episode_current: String = "",
    val lang: String = ""
)

@Serializable
data class ListResponse(val data: ListData? = null)

@Serializable
data class ListData(val items: List<Movie> = emptyList())

@Serializable
data class Tag(val name: String = "", val slug: String = "")

@Serializable
data class MovieDetail(
    val name: String = "",
    val slug: String = "",
    val origin_name: String = "",
    val thumb_url: String = "",
    val poster_url: String = "",
    val content: String = "",
    val year: String = "",
    val quality: String = "",
    val episode_current: String = "",
    val episode_total: String = "",
    val time: String = "",
    val lang: String = "",
    val type: String = "",
    val category: List<Tag> = emptyList(),
    val country: List<Tag> = emptyList(),
    val actor: List<String> = emptyList()
)

@Serializable
data class Episode(
    val name: String = "",
    val slug: String = "",
    val link_m3u8: String = "",
    val link_embed: String = ""
)

@Serializable
data class Server(
    val server_name: String = "",
    val server_data: List<Episode> = emptyList()
)

@Serializable
data class DetailResponse(
    val movie: MovieDetail? = null,
    val episodes: List<Server> = emptyList()
)

data class HomeRow(val title: String, val type: String, val slug: String)

val HOME_ROWS = listOf(
    HomeRow("Phim mới phát hành", "danh-sach", "phim-moi-cap-nhat"),
    HomeRow("Phim Hàn thịnh hành", "quoc-gia", "han-quoc"),
    HomeRow("Hành động kịch tính", "the-loai", "hanh-dong"),
    HomeRow("Hoạt hình mới và hot", "the-loai", "hoat-hinh"),
    HomeRow("Kinh dị và ám ảnh", "the-loai", "kinh-di"),
    HomeRow("Hài hước xả stress", "the-loai", "hai-huoc"),
    HomeRow("Phim lẻ đặc sắc", "danh-sach", "phim-le")
)

object Api {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private suspend fun get(url: String): String = withContext(Dispatchers.IO) {
        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { res ->
            if (!res.isSuccessful) throw IOException("HTTP ${res.code}")
            res.body?.string().orEmpty()
        }
    }

    suspend fun list(type: String, slug: String, limit: Int = 12): List<Movie> {
        val isDanhSach = slug in listOf("phim-bo", "phim-le", "tv-shows", "hoat-hinh", "phim-moi-cap-nhat")
        val base = if (isDanhSach) "/v1/api/danh-sach/$slug" else "/v1/api/$type/$slug"
        val text = get("$API_KK$base?limit=$limit&sort_field=modified.time&sort_type=desc")
        return json.decodeFromString<ListResponse>(text).data?.items ?: emptyList()
    }

    suspend fun detail(slug: String): DetailResponse {
        val text = get("$API_KK/phim/$slug")
        return json.decodeFromString<DetailResponse>(text)
    }

    fun image(path: String): String {
        if (path.isBlank()) return ""
        if (path.startsWith("http")) {
            val m = Regex("^https?://phimimg\\.com/(.+)$", RegexOption.IGNORE_CASE).find(path)
            return if (m != null) IMG_KK + m.groupValues[1] else path
        }
        return IMG_KK + path.trimStart('/')
    }
}
