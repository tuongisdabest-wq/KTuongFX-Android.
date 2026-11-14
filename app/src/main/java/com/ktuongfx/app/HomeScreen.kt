package com.ktuongfx.app

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

val AppGold = Color(0xFFFECF59)
val AppBlack = Color(0xFF0A0A0D)
val AppCard = Color(0xFF1C1F2E)

class HomeViewModel : ViewModel() {
    var rows by mutableStateOf<List<Pair<HomeRow, List<Movie>>>>(emptyList())
        private set
    var loading by mutableStateOf(true)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            loading = true
            error = null
            val result = HOME_ROWS.map { r ->
                async { r to runCatching { Api.list(r.type, r.slug) }.getOrDefault(emptyList()) }
            }.awaitAll()
            rows = result.filter { it.second.isNotEmpty() }
            if (rows.isEmpty()) error = "Không tải được danh sách phim. Kiểm tra mạng rồi thử lại."
            loading = false
        }
    }
}

@Composable
fun HomeScreen(onMovieClick: (Movie) -> Unit, vm: HomeViewModel = viewModel()) {
    val rows = vm.rows
    Box(Modifier.fillMaxSize().background(AppBlack)) {
        when {
            vm.loading && rows.isEmpty() -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = AppGold
            )
            rows.isEmpty() -> Column(
                modifier = Modifier.align(Alignment.Center).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(vm.error ?: "Không có dữ liệu", color = Color(0xFFA0A0B0), textAlign = TextAlign.Center)
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { vm.load() },
                    colors = ButtonDefaults.buttonColors(containerColor = AppGold, contentColor = Color(0xFF412402))
                ) { Text("Thử lại") }
            }
            else -> LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
                item { Hero(rows.first().second.first(), onMovieClick) }
                items(rows) { (row, movies) -> MovieRow(row.title, movies, onMovieClick) }
            }
        }
        Text(
            "KTUONGFX",
            color = AppGold,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.TopStart).statusBarsPadding().padding(16.dp)
        )
    }
}

@Composable
private fun Hero(movie: Movie, onClick: (Movie) -> Unit) {
    Box(Modifier.fillMaxWidth().height(440.dp).background(AppCard)) {
        AsyncImage(
            model = Api.image(movie.poster_url.ifBlank { movie.thumb_url }),
            contentDescription = movie.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color.Transparent, Color(0x990A0A0D), AppBlack))
            )
        )
        Column(
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                movie.name,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
            val info = listOf(movie.year, movie.quality, movie.episode_current)
                .filter { it.isNotBlank() }.joinToString(" · ")
            if (info.isNotEmpty()) {
                Text(info, color = Color(0xFFD0D0D8), fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { onClick(movie) },
                colors = ButtonDefaults.buttonColors(containerColor = AppGold, contentColor = Color(0xFF412402))
            ) { Text("Phát", fontWeight = FontWeight.Medium) }
        }
    }
}

@Composable
private fun MovieRow(title: String, movies: List<Movie>, onClick: (Movie) -> Unit) {
    Column(Modifier.padding(top = 16.dp)) {
        Text(
            title,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(movies) { m ->
                Column(Modifier.width(110.dp).clickable { onClick(m) }) {
                    AsyncImage(
                        model = Api.image(m.thumb_url.ifBlank { m.poster_url }),
                        contentDescription = m.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(2f / 3f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(AppCard)
                    )
                    Text(
                        m.name,
                        color = Color(0xFFD0D0D8),
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
