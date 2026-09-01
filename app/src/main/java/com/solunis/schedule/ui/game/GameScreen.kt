package com.solunis.schedule.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Game screen dark theme colors (matches design_preview)
private val GameBg1       = Color(0xFF1A1330)
private val GameBg2       = Color(0xFF160F28)
private val GameBg3       = Color(0xFF1A1235)
private val GamePurple    = Color(0xFF7C3AED)
private val GamePink      = Color(0xFFEC4899)
private val GamePurpleLight = Color(0xFFA78BFA)
private val GamePinkLight   = Color(0xFFF472B6)
private val GameText      = Color.White
private val GameTextMuted = Color(0xFF9580B8)
private val GameSurface   = Color(0x14FFFFFF)
private val GameBorder    = Color(0x14FFFFFF)
private val GameCardPurple = Color(0xFF2D1B69)
private val GameCardPink   = Color(0xFF3D1040)
private val GameCardGreen  = Color(0xFF0F3020)

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onGameClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val coins by viewModel.coins.observeAsState(2480)
    val weekStats by viewModel.weekStats.observeAsState()

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
        // Decorative blobs
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
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

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(GameSurface)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text("🪙", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "%,d".format(coins),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFDFB04A)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Featured game banner
            FeaturedGameBanner(
                game = viewModel.featuredGame,
                onPlayClick = { onGameClick(viewModel.featuredGame.name) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Week stats
            SectionTitle("本周战绩")
            weekStats?.let { stats ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard("${stats.levelsCleared}", "闯关", Modifier.weight(1f))
                    StatCard("${stats.wordsPracticed}", "单词", Modifier.weight(1f))
                    StatCard("${stats.accuracy}%", "正确率", Modifier.weight(1f))
                    StatCard("${stats.streak}🔥", "连胜", Modifier.weight(1f))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // All games
            SectionTitle("全部游戏")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(viewModel.allGames) { game ->
                    GameCard(
                        game = game,
                        onClick = { onGameClick(game.name) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FeaturedGameBanner(
    game: GameViewModel.GameItem,
    onPlayClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF2D1466), Color(0xFF24104A)),
                    start = Offset(0f, 0f),
                    end = Offset(400f, 200f)
                )
            )
            .border(1.dp, GamePurple.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = game.emoji,
                fontSize = 48.sp
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = game.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = GameText,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = game.description,
                    fontSize = 12.sp,
                    color = GameTextMuted,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Play button
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.linearGradient(listOf(GamePurple, GamePink))
                        )
                        .clickable { onPlayClick() }
                        .padding(horizontal = 18.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "▶ 开始游戏",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // XP bar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Lv.${game.level}",
                        fontSize = 10.sp,
                        color = GameTextMuted
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color.White.copy(alpha = 0.1f))
                    ) {
                        val xpFraction = if (game.xpMax > 0) game.xp.toFloat() / game.xpMax else 0f
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(xpFraction)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    Brush.horizontalGradient(listOf(GamePurpleLight, GamePinkLight))
                                )
                        )
                    }
                    Text(
                        text = "${game.xp} / ${game.xpMax} XP",
                        fontSize = 10.sp,
                        color = GameTextMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun StatCard(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(GameSurface)
            .border(1.dp, GameBorder, RoundedCornerShape(16.dp))
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = GameText
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = GameTextMuted
        )
    }
}

@Composable
private fun GameCard(
    game: GameViewModel.GameItem,
    onClick: () -> Unit
) {
    val bgColor = when (game.name) {
        "单词消消乐" -> GameCardPurple
        "速算挑战"   -> GameCardPink
        "知识地图"   -> GameCardGreen
        else         -> GameCardPurple
    }

    Box(
        modifier = Modifier
            .width(140.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        if (game.tag != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFE8368F))
                    .padding(horizontal = 7.dp, vertical = 2.dp)
            ) {
                Text(text = game.tag, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        Column {
            Text(text = game.emoji, fontSize = 32.sp, modifier = Modifier.padding(bottom = 8.dp))
            Text(
                text = game.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 4.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = game.description,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = GameTextMuted,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
    )
}
