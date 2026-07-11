package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LessonEntity
import com.example.ui.components.GlossyButton
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageLearningScreen(
    lesson: LessonEntity,
    locale: Locale,
    onBack: () -> Unit,
    onComplete: () -> Unit,
    onSpeak: (String, Locale) -> Unit,
    onListen: (Locale, (String) -> Unit, (String) -> Unit) -> Unit,
    onXpEarned: (Int) -> Unit,
    onAITutorClick: () -> Unit
) {
    var spokenText by remember { mutableStateOf("") }
    var feedback by remember { mutableStateOf("") }
    var isListening by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Language Lab", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onAITutorClick) {
                        Text("🤖", fontSize = 22.sp)
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Listen and Practice",
                fontSize = 14.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Surface(
                modifier = Modifier.fillMaxWidth().height(200.dp),
                color = Color(0xFFF5F5F5),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = lesson.title,
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Black
                    )
                    Text(
                        text = lesson.content,
                        fontSize = 20.sp,
                        color = Color.Gray
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Listening Mode
            GlossyButton(
                onClick = { onSpeak(lesson.title, locale) },
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF1478D1)
            ) {
                Icon(Icons.Default.VolumeUp, contentDescription = "Listen", tint = Color.White)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Listen & Learn 🔊", color = Color.White, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Speaking Mode
            GlossyButton(
                onClick = {
                    isListening = true
                    onListen(locale, { result ->
                        isListening = false
                        spokenText = result
                        if (result.contains(lesson.title, ignoreCase = true)) {
                            feedback = "Excellent! +25 Points ✅"
                            onXpEarned(25)
                        } else {
                            feedback = "Try again! You said: '$result'"
                        }
                    }, { error ->
                        isListening = false
                        feedback = "Error: $error"
                    })
                },
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFE53935)
            ) {
                Icon(
                    imageVector = Icons.Default.Mic, 
                    contentDescription = "Speak", 
                    tint = if (isListening) Color.Yellow else Color.White
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (isListening) "Listening..." else "Speak & Assess 🎤", 
                    color = Color.White, 
                    fontWeight = FontWeight.Bold
                )
            }
            
            if (feedback.isNotEmpty()) {
                Spacer(modifier = Modifier.height(32.dp))
                Surface(
                    color = if (feedback.contains("Excellent")) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = feedback,
                        modifier = Modifier.padding(16.dp),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (feedback.contains("Excellent")) Color(0xFF2E7D32) else Color.Red
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            OutlinedButton(
                onClick = onComplete,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Finish Lab", fontWeight = FontWeight.Bold)
            }
        }
    }
}
