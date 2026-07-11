package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.data.UserProgress
import com.example.data.SubjectEntity
import com.example.ui.components.QuestCard

@Composable
fun DashboardScreen(
    progress: UserProgress,
    subjects: List<SubjectEntity>,
    onSubjectClick: (SubjectEntity) -> Unit
) {
    val pointsToNextLevel = 1000
    val currentLevel = 1 + (progress.spherePoints / pointsToNextLevel)
    val xpInCurrentLevel = progress.spherePoints % pointsToNextLevel
    val progressPercent = xpInCurrentLevel.toFloat() / pointsToNextLevel

    val animatedProgress by animateFloatAsState(
        targetValue = progressPercent,
        animationSpec = tween(durationMillis = 1000),
        label = "XP Progress Animation"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F6FB)),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Surface(
                            color = Color(0xFFEEF0FF),
                            shape = RoundedCornerShape(50.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF5B52F0).copy(alpha = 0.2f))
                        ) {
                            Text(
                                text = "Level $currentLevel · ${progress.spherePoints} SP",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF5B52F0)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Learn Smarter.",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF1A1D2E)
                        )
                        Text(
                            text = "Level Up Daily.",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF5B52F0)
                        )
                    }

                    // Sphere Graphic Accent
                    Image(
                        painter = painterResource(id = R.drawable.img_app_logo_murtaza_sphere_1783775161280),
                        contentDescription = "Murtaza Sphere",
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Progress Indicator
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Progress to Level ${currentLevel + 1}",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$xpInCurrentLevel / $pointsToNextLevel XP",
                            fontSize = 11.sp,
                            color = Color(0xFF5B52F0),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFF5B52F0),
                        trackColor = Color(0xFFEEF1F8)
                    )
                }
            }
        }

        // Stats Strip
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(label = "Active Streak", value = "${progress.streakCount} 🔥", modifier = Modifier.weight(1f))
                StatCard(label = "Sphere Points", value = "${progress.spherePoints} 💎", modifier = Modifier.weight(1f))
                StatCard(label = "Active Quests", value = "${subjects.size}", modifier = Modifier.weight(1f))
            }
        }

        // Active Quests Label
        item {
            Text(
                text = "Interactive Learning Quests",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1A1D2E),
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp)
            )
        }

        // Quest Cards List
        items(subjects) { subject ->
            QuestCard(
                subject = subject,
                progress = progress,
                onClick = { onSubjectClick(subject) },
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = Color.White,
        shape = RoundedCornerShape(18.dp),
        shadowElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEF1F8))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF1A1D2E)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
