package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TopicEntity
import com.example.data.LessonEntity
import com.example.ui.components.GlassCard
import com.example.ui.components.GlossyButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicDetailScreen(
    topic: TopicEntity,
    lessons: List<LessonEntity>,
    onBack: () -> Unit,
    onLessonSelect: (LessonEntity) -> Unit,
    onStartQuiz: (String) -> Unit,
    onAITutorClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        TopAppBar(
            title = { Text(topic.title, fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(onClick = onAITutorClick) {
                    Text("🤖", fontSize = 22.sp)
                }
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Lessons", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            items(lessons) { lesson ->
                LessonCard(lesson = lesson, onClick = { onLessonSelect(lesson) })
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                GlossyButton(
                    onClick = { onStartQuiz(topic.id) },
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF1478D1)
                ) {
                    Text("Take Unit Quiz ⚡", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun LessonCard(lesson: LessonEntity, onClick: () -> Unit) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("📄", fontSize = 20.sp)
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = lesson.title,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )
            Text("➡️", color = Color.LightGray)
        }
    }
}
