package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BadgeEntity
import com.example.data.UserProgress

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    progress: UserProgress,
    badges: List<BadgeEntity>,
    onToggleSubjectCompletion: (String, Boolean) -> Unit,
    onResetAllProgress: () -> Unit,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()
    var showResetConfirmation by remember { mutableStateOf(false) }
    
    // Parse completed topics
    val completedTopics = progress.completedTopicsJson.split(",")
        .filter { it.isNotBlank() }
        .toSet()

    // Prefix mapping for the four subjects to count completed topics
    val prefixMap = mapOf(
        "math" to "math_",
        "english" to "eng_",
        "coding" to "cod_",
        "robotics" to "rob_"
    )

    // Vibrant colors matching the subject themes
    val colorMap = mapOf(
        "math" to Color(0xFF5B52F0),
        "english" to Color(0xFF059669),
        "coding" to Color(0xFFF59E0B),
        "robotics" to Color(0xFFD946A8)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile & Achievements", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF9FAFB)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEEF2F6)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("👤", fontSize = 52.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Murtaza",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1F2937)
                    )

                    Text(
                        text = "Student Explorer",
                        fontSize = 15.sp,
                        color = Color(0xFF6B7280),
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Stats Grid Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ProfileStatItem(
                            label = "Sphere Points",
                            value = progress.spherePoints.toString(),
                            icon = "⭐",
                            modifier = Modifier.weight(1f)
                        )
                        ProfileStatItem(
                            label = "Daily Streak",
                            value = "${progress.streakCount} Days",
                            icon = "🔥",
                            modifier = Modifier.weight(1f)
                        )
                        ProfileStatItem(
                            label = "Badges",
                            value = if (progress.unlockedBadgesJson.isEmpty()) "0" else progress.unlockedBadgesJson.split(",").filter { it.isNotBlank() }.size.toString(),
                            icon = "🏅",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Section: Digital Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Digital Badges",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Complete 15 topics to unlock",
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280),
                    fontWeight = FontWeight.Normal
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (badges.isEmpty()) {
                // Fallback placeholder if DB not loaded yet
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF5B52F0))
                }
            } else {
                badges.forEach { badge ->
                    val subjectId = badge.id.substringAfter("badge_")
                    val prefix = prefixMap[subjectId] ?: ""
                    val subjectColor = colorMap[subjectId] ?: Color.Gray
                    
                    val subjectCompletedTopics = completedTopics.filter { it.startsWith(prefix) }.size
                    val isUnlocked = subjectCompletedTopics >= 15
                    val progressFraction = subjectCompletedTopics.toFloat() / 15f

                    BadgeCard(
                        badge = badge,
                        completedCount = subjectCompletedTopics,
                        isUnlocked = isUnlocked,
                        progressFraction = progressFraction,
                        themeColor = subjectColor
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section: Sandbox Console
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4F6)),
                border = BoxBorder(1.dp, Color(0xFFE5E7EB))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⚙️", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Developer Sandbox Tools",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF374151)
                        )
                    }

                    Text(
                        text = "Quickly toggle all 15 topics in a subject to test badge unlocks & levels:",
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280),
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )

                    val subjectsList = listOf(
                        "math" to "Mathematics 📐",
                        "english" to "English 📝",
                        "coding" to "Coding 💻",
                        "robotics" to "Robotics 🤖"
                    )

                    subjectsList.forEach { (subjectId, label) ->
                        val prefix = prefixMap[subjectId] ?: ""
                        val hasCompletedAll = completedTopics.count { it.startsWith(prefix) } >= 15

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = label,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF4B5563)
                            )
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = if (hasCompletedAll) "Complete" else "Incomplete",
                                    fontSize = 12.sp,
                                    color = if (hasCompletedAll) Color(0xFF059669) else Color(0xFF6B7280),
                                    fontWeight = FontWeight.Bold
                                )
                                Switch(
                                    checked = hasCompletedAll,
                                    onCheckedChange = { completeAll ->
                                        onToggleSubjectCompletion(subjectId, completeAll)
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = colorMap[subjectId] ?: Color(0xFF059669)
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Section: Settings & Reset
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BoxBorder(1.dp, Color(0xFFE5E7EB)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🛠️", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "System Options",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937)
                        )
                    }

                    Text(
                        text = "Manage your application preferences and data.",
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280),
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )

                    HorizontalDivider(color = Color(0xFFF3F4F6), thickness = 1.dp)
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Reset All Progress",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFDC2626)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Permanently clear points, badges, completed topics, and quiz history. This action cannot be undone.",
                                fontSize = 11.sp,
                                color = Color(0xFF9CA3AF),
                                lineHeight = 14.sp
                            )
                        }
                        
                        Button(
                            onClick = { showResetConfirmation = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE2E2), contentColor = Color(0xFFDC2626)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .testTag("reset_progress_button")
                        ) {
                            Text("Reset", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showResetConfirmation) {
        AlertDialog(
            onDismissRequest = { showResetConfirmation = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("⚠️", fontSize = 24.sp)
                    Text(
                        text = "Reset All Progress?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF1F2937)
                    )
                }
            },
            text = {
                Text(
                    text = "Are you sure you want to delete all your progress, points, and digital badges? This will reset Murtaza Sphere back to its original state and cannot be undone.",
                    fontSize = 14.sp,
                    color = Color(0xFF4B5563)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResetConfirmation = false
                        onResetAllProgress()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626), contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("confirm_reset_button")
                ) {
                    Text("Yes, Reset Everything", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showResetConfirmation = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF4B5563)),
                    modifier = Modifier.testTag("dismiss_reset_button")
                ) {
                    Text("Cancel", fontWeight = FontWeight.SemiBold)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
fun ProfileStatItem(label: String, value: String, icon: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
        border = BoxBorder(1.dp, Color(0xFFE5E7EB))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(icon, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF111827),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                color = Color(0xFF6B7280),
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                lineHeight = 12.sp
            )
        }
    }
}

@Composable
fun BadgeCard(
    badge: BadgeEntity,
    completedCount: Int,
    isUnlocked: Boolean,
    progressFraction: Float,
    themeColor: Color
) {
    val backgroundBrush = if (isUnlocked) {
        Brush.linearGradient(
            colors = listOf(
                themeColor.copy(alpha = 0.9f),
                themeColor.copy(alpha = 0.7f)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFFF3F4F6),
                Color(0xFFE5E7EB)
            )
        )
    }

    val textColor = if (isUnlocked) Color.White else Color(0xFF1F2937)
    val descColor = if (isUnlocked) Color.White.copy(alpha = 0.85f) else Color(0xFF4B5563)
    val secondaryTextColor = if (isUnlocked) Color.White.copy(alpha = 0.8f) else Color(0xFF6B7280)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent,
        shadowElevation = if (isUnlocked) 6.dp else 1.dp
    ) {
        Box(
            modifier = Modifier
                .background(backgroundBrush)
                .padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Badge Icon Circle
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(
                            if (isUnlocked) Color.White.copy(alpha = 0.25f) else Color(0xFFD1D5DB)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = badge.icon,
                        fontSize = 36.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Badge Information
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = badge.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        
                        // Status Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isUnlocked) Color.White.copy(alpha = 0.3f) else Color(0xFFD1D5DB)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isUnlocked) "Unlocked 🎉" else "Locked 🔒",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isUnlocked) Color.White else Color(0xFF4B5563)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = badge.description,
                        fontSize = 13.sp,
                        color = descColor,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Progress section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Topics completed: $completedCount / 15",
                            fontSize = 11.sp,
                            color = secondaryTextColor,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        Text(
                            text = "${(progressFraction * 100).toInt()}%",
                            fontSize = 11.sp,
                            color = secondaryTextColor,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { progressFraction.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (isUnlocked) Color.White else themeColor,
                        trackColor = if (isUnlocked) Color.White.copy(alpha = 0.3f) else Color(0xFFE5E7EB)
                    )
                }
            }
        }
    }
}

// Custom Border function to avoid full layout styling issues
fun BoxBorder(width: androidx.compose.ui.unit.Dp, color: Color) = 
    androidx.compose.foundation.BorderStroke(width, color)
