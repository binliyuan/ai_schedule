package com.solunis.schedule.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

private val GameBg1 = Color(0xFF1A1330)
private val GameBg2 = Color(0xFF160F28)
private val GameBg3 = Color(0xFF1A1235)
private val GamePurple = Color(0xFF7C3AED)
private val GamePink = Color(0xFFEC4899)
private val GamePurpleLight = Color(0xFFA78BFA)
private val GameTextMuted = Color(0xFF9580B8)
private val GameSurface = Color(0x14FFFFFF)

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onGameClick: (GameViewModel.GameItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val games by viewModel.games.observeAsState(emptyList())

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(GameBg1, GameBg2, GameBg3),
                    start = Offset(0f, 0f),
                    end = Offset(400f, 1200f)
                )
            )
    ) {
        Box(
            modifier = Modifier
                .size(260.dp)
                .offset(x = 100.dp, y = (-80).dp)
                .clip(CircleShape)
                .background(GamePurple.copy(alpha = 0.5f))
                .blur(60.dp)
        )
        Box(
            modifier = Modifier
                .size(200.dp)
                .offset(x = (-60).dp, y = 500.dp)
                .clip(CircleShape)
                .background(GamePink.copy(alpha = 0.4f))
                .blur(60.dp)
        )

        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "游戏中心",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = GamePurpleLight
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(games, key = { it.id }) { game ->
                    GameCard(game = game, onClick = { onGameClick(game) })
                }
            }
        }
    }
}

@Composable
private fun GameCard(
    game: GameViewModel.GameItem,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(GameSurface)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.2f)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(Color(0xFF2D1B69))
        ) {
            AsyncImage(
                model = game.imageUrl,
                contentDescription = game.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            if (game.tag != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.horizontalGradient(listOf(GamePurple, GamePink))
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = game.tag,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            val typeLabel = when (game.actionType) {
                GameViewModel.ActionType.H5 -> "H5"
                GameViewModel.ActionType.QUICK_APP -> "快应用"
                GameViewModel.ActionType.DOWNLOAD -> "下载"
                GameViewModel.ActionType.DEEPLINK -> "打开"
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = typeLabel,
                    fontSize = 9.sp,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }

        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = game.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = game.description,
                fontSize = 11.sp,
                color = GameTextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
