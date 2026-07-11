package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SubjectEntity
import com.example.data.UserProgress
import com.example.data.MockDataGenerator

@Composable
fun QuestCard(
    subject: SubjectEntity,
    progress: UserProgress,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val subjectColor = remember(subject.color) {
        try {
            Color(android.graphics.Color.parseColor(subject.color))
        } catch (e: Exception) {
            Color(0xFF5B52F0) // Fallback Indigo
        }
    }

    val subjectTopicIds = remember(subject.id) {
        MockDataGenerator.getTopics()
            .filter { it.subjectId == subject.id }
            .map { it.id }
            .toSet()
    }

    val completedTopicsList = remember(progress.completedTopicsJson) {
        progress.completedTopicsJson.split(",").filter { it.isNotBlank() }
    }
    val completedQuizzesList = remember(progress.completedQuizzesJson) {
        progress.completedQuizzesJson.split(",").filter { it.isNotBlank() }
    }

    val completedTopicsCount = remember(completedTopicsList, subjectTopicIds) {
        completedTopicsList.count { it in subjectTopicIds }
    }
    val completedQuizzesCount = remember(completedQuizzesList, subjectTopicIds) {
        completedQuizzesList.count { it in subjectTopicIds }
    }

    val totalElements = subjectTopicIds.size * 2
    val completedElements = completedTopicsCount + completedQuizzesCount
    val progressFraction = if (totalElements > 0) completedElements.toFloat() / totalElements else 0f

    var isExpanded by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (isExpanded) 1.02f else 1.0f, label = "ScaleAnimation")

    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(1.5.dp, subjectColor.copy(alpha = if (isExpanded) 0.6f else 0.15f)),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isExpanded) 6.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(subjectColor.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = subject.icon,
                            fontSize = 30.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = subject.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1A1D2E)
                        )
                        Text(
                            text = "Level Up Quest",
                            fontSize = 12.sp,
                            color = subjectColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Surface(
                    color = subjectColor.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "15 Units",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = subjectColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sub-headline features list
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuestFeatureChip(icon = "📚", text = "15 Topics", color = subjectColor)
                QuestFeatureChip(icon = "🎥", text = "Videos", color = subjectColor)
                QuestFeatureChip(icon = "⚡", text = "Quizzes", color = subjectColor)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress Bar Section
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Quest Progress",
                        fontSize = 12.sp,
                        color = Color(0xFF4A5068),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${(progressFraction * 100).toInt()}% (${completedTopicsCount}/15 Topics, ${completedQuizzesCount}/15 Quizzes)",
                        fontSize = 11.sp,
                        color = subjectColor,
                        fontWeight = FontWeight.Black
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = subjectColor,
                    trackColor = subjectColor.copy(alpha = 0.12f)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Divider(color = Color(0xFFF1F3F9), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "What you'll learn in this Quest:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4A5068)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val highlights = when (subject.id) {
                        "math" -> listOf("Master fundamental Algebra and equations", "Explore 2D and 3D shapes and area rules", "Calculate probability, mean, median and mode")
                        "english" -> listOf("Build stellar sentence construction", "Understand complex grammar and irregularity", "Write persuasive and structured essays")
                        "coding" -> listOf("Learn loops, list indexing, and functions", "Grasp object-oriented class blueprints", "Deconstruct algorithms and binary values")
                        "robotics" -> listOf("Explore physical actuators and distance sensors", "Program autonomous collision avoidance algorithms", "Inspect high-torque industrial robotics and AI")
                        else -> listOf("Broaden your topic knowledge base", "Participate in fun and interactive quizzes", "Earn points and expand your streaks")
                    }

                    highlights.forEach { bullet ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text("✨", fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp, end = 8.dp))
                            Text(
                                text = bullet,
                                fontSize = 13.sp,
                                color = Color(0xFF4A5068),
                                lineHeight = 18.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = subjectColor,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Begin Quest 🚀",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuestFeatureChip(
    icon: String,
    text: String,
    color: Color
) {
    Surface(
        color = Color(0xFFF4F6FB),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFEEF1F8))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 12.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4A5068)
            )
        }
    }
}
