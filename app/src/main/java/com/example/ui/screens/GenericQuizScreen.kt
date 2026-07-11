package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.QuizQuestionEntity
import com.example.ui.components.GlossyButton
import com.example.ui.components.ThreeDCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenericQuizScreen(
    questions: List<QuizQuestionEntity>,
    onBack: () -> Unit,
    onComplete: () -> Unit,
    onXpEarned: (Int) -> Unit
) {
    var currentIndex by remember { mutableIntStateOf(0) }
    var selectedOption by remember { mutableStateOf<Int?>(null) }
    var showFeedback by remember { mutableStateOf(false) }
    var score by remember { mutableIntStateOf(0) }
    var quizFinished by remember { mutableStateOf(false) }

    // Filter questions based on the topic if needed, but the list passed should already be topic-specific
    val currentQuestion = questions.getOrNull(currentIndex)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Topic Quiz", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
                .padding(24.dp)
        ) {
            if (quizFinished) {
                QuizResult(score = score, total = questions.size, onBack = onComplete)
            } else if (currentQuestion != null) {
                // Progress Bar
                LinearProgressIndicator(
                    progress = { (currentIndex + 1).toFloat() / questions.size },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = Color(0xFF5B52F0),
                    trackColor = Color(0xFFEEF1F8)
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Text(
                    text = "Question ${currentIndex + 1} of ${questions.size}",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                ThreeDCard(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = currentQuestion.question,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1A1D2E),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                val options = currentQuestion.options.split(",")
                options.forEachIndexed { index, option ->
                    val isCorrect = index == currentQuestion.correctIndex
                    val isSelected = selectedOption == index
                    
                    val color = when {
                        showFeedback && isCorrect -> Color(0xFF059669) // Mint Correct
                        showFeedback && isSelected && !isCorrect -> Color(0xFFF05252) // Coral Wrong
                        isSelected -> Color(0xFF5B52F0) // Indigo Selected
                        else -> Color.White
                    }
                    
                    val textColor = if (isSelected || (showFeedback && isCorrect)) Color.White else Color(0xFF1A1D2E)

                    OutlinedButton(
                        onClick = { if (!showFeedback) selectedOption = index },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = color,
                            contentColor = textColor
                        ),
                        border = if (isSelected || (showFeedback && isCorrect)) null else androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
                    ) {
                        Text(
                            text = option,
                            modifier = Modifier.padding(8.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                if (showFeedback) {
                    Surface(
                        color = if (selectedOption == currentQuestion.correctIndex) Color(0xFFE6FBF3) else Color(0xFFFFF0F0),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    ) {
                        Text(
                            text = currentQuestion.explanation,
                            color = Color(0xFF4A5068),
                            fontSize = 14.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    
                    GlossyButton(
                        onClick = {
                            if (currentIndex < questions.size - 1) {
                                currentIndex++
                                selectedOption = null
                                showFeedback = false
                            } else {
                                quizFinished = true
                                onXpEarned(score * 50)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF5B52F0)
                    ) {
                        Text("Next Question ➡️", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                } else {
                    GlossyButton(
                        onClick = {
                            if (selectedOption != null) {
                                if (selectedOption == currentQuestion.correctIndex) score++
                                showFeedback = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF5B52F0)
                    ) {
                        Text("Check Answer", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun QuizResult(score: Int, total: Int, onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🎉", fontSize = 64.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Text("Quiz Completed!", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color(0xFF1A1D2E))
        Spacer(modifier = Modifier.height(12.dp))
        Text("You got $score out of $total correct!", fontSize = 18.sp, color = Color(0xFF4A5068))
        Spacer(modifier = Modifier.height(8.dp))
        Text("Earned ${score * 50} Sphere Points!", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
        
        Spacer(modifier = Modifier.height(48.dp))
        
        GlossyButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF5B52F0)
        ) {
            Text("Finish & Level Up 🚀", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}
