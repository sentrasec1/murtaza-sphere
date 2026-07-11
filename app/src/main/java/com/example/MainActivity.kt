package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.EduQuestViewModel
import com.example.ui.screens.*
import com.example.ui.theme.EduQuestTheme
import com.example.data.SubjectEntity
import com.example.data.TopicEntity
import com.example.data.LessonEntity
import com.example.ui.components.AITutorSidePanel
import java.util.Locale

sealed class Screen {
    object Dashboard : Screen()
    object Subjects : Screen()
    object Rewards : Screen()
    object Profile : Screen()
    data class SubjectDetail(val subject: SubjectEntity) : Screen()
    data class TopicDetail(val topic: TopicEntity) : Screen()
    data class LessonView(val subjectId: String, val lesson: LessonEntity) : Screen()
    data class QuizView(val topicId: String) : Screen()
    object AITutor : Screen()
}

class MainActivity : ComponentActivity() {

    private val viewModel: EduQuestViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EduQuestTheme {
                var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }
                var isAITutorOpen by remember { mutableStateOf(false) }
                val progress by viewModel.userProgress.collectAsStateWithLifecycle()
                val subjects by viewModel.subjects.collectAsStateWithLifecycle()
                val badges by viewModel.badges.collectAsStateWithLifecycle()

                val messages by viewModel.chatMessages.collectAsStateWithLifecycle()
                val loading by viewModel.chatLoading.collectAsStateWithLifecycle()
                val currentContext by viewModel.currentContext.collectAsStateWithLifecycle()

                Box(modifier = Modifier.fillMaxSize()) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = {
                            NavigationBar {
                                NavigationBarItem(
                                    icon = { Text("🌐") },
                                    label = { Text("Sphere") },
                                    selected = currentScreen is Screen.Dashboard,
                                    onClick = { currentScreen = Screen.Dashboard }
                                )
                                NavigationBarItem(
                                    icon = { Text("📚") },
                                    label = { Text("Subjects") },
                                    selected = currentScreen is Screen.Subjects,
                                    onClick = { currentScreen = Screen.Subjects }
                                )
                                NavigationBarItem(
                                    icon = { Text("🏅") },
                                    label = { Text("Rewards") },
                                    selected = currentScreen is Screen.Rewards,
                                    onClick = { currentScreen = Screen.Rewards }
                                )
                                NavigationBarItem(
                                    icon = { Text("👤") },
                                    label = { Text("Profile") },
                                    selected = currentScreen is Screen.Profile,
                                    onClick = { currentScreen = Screen.Profile }
                                )
                            }
                        }
                    ) { innerPadding ->
                        Surface(
                            modifier = Modifier.padding(innerPadding),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            when (val screen = currentScreen) {
                                is Screen.Dashboard -> {
                                    DashboardScreen(
                                        progress = progress,
                                        subjects = subjects.take(2),
                                        onSubjectClick = { currentScreen = Screen.SubjectDetail(it) }
                                    )
                                }
                                is Screen.Subjects -> {
                                    DashboardScreen(
                                        progress = progress,
                                        subjects = subjects,
                                        onSubjectClick = { currentScreen = Screen.SubjectDetail(it) }
                                    )
                                }
                                is Screen.Rewards -> {
                                    RewardsScreen(
                                        progress = progress,
                                        onBack = { currentScreen = Screen.Dashboard }
                                    )
                                }
                                is Screen.SubjectDetail -> {
                                    val topics by viewModel.getTopicsForSubject(screen.subject.id).collectAsStateWithLifecycle(emptyList())
                                    LaunchedEffect(screen.subject.id) {
                                        viewModel.setContext(subjectTitle = screen.subject.title)
                                    }
                                    SubjectDetailScreen(
                                        subject = screen.subject,
                                        topics = topics,
                                        onBack = { currentScreen = Screen.Dashboard },
                                        onTopicClick = { currentScreen = Screen.TopicDetail(it) },
                                        onAITutorClick = {
                                            viewModel.setContext(subjectTitle = screen.subject.title)
                                            isAITutorOpen = true
                                        }
                                    )
                                }
                                is Screen.TopicDetail -> {
                                    val lessons by viewModel.getLessonsForTopic(screen.topic.id).collectAsStateWithLifecycle(emptyList())
                                    val subjectName = screen.topic.subjectId.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                                    LaunchedEffect(screen.topic.id) {
                                        viewModel.setContext(subjectTitle = subjectName, topicTitle = screen.topic.title)
                                    }
                                    TopicDetailScreen(
                                        topic = screen.topic,
                                        lessons = lessons,
                                        onBack = { currentScreen = Screen.Dashboard },
                                        onLessonSelect = { currentScreen = Screen.LessonView(screen.topic.subjectId, it) },
                                        onStartQuiz = { currentScreen = Screen.QuizView(it) },
                                        onAITutorClick = {
                                            viewModel.setContext(subjectTitle = subjectName, topicTitle = screen.topic.title)
                                            isAITutorOpen = true
                                        }
                                    )
                                }
                                is Screen.LessonView -> {
                                    val subjectName = screen.subjectId.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                                    LaunchedEffect(screen.lesson.id) {
                                        viewModel.setContext(subjectTitle = subjectName, lessonTitle = screen.lesson.title)
                                    }
                                    if (screen.subjectId == "arabic" || screen.subjectId == "french") {
                                        LanguageLearningScreen(
                                            lesson = screen.lesson,
                                            locale = if (screen.subjectId == "arabic") Locale("ar", "SA") else Locale.FRANCE,
                                            onBack = { currentScreen = Screen.Dashboard },
                                            onComplete = {
                                                viewModel.completeTopic(screen.lesson.topicId)
                                                currentScreen = Screen.Dashboard
                                            },
                                            onSpeak = { text, loc -> viewModel.speak(text, loc) },
                                            onListen = { loc, res, err -> viewModel.listen(loc, res, err) },
                                            onXpEarned = { viewModel.addPoints(it) },
                                            onAITutorClick = {
                                                viewModel.setContext(subjectTitle = subjectName, lessonTitle = screen.lesson.title)
                                                isAITutorOpen = true
                                            }
                                        )
                                    } else {
                                        LessonScreen(
                                            lesson = screen.lesson,
                                            onBack = { currentScreen = Screen.Dashboard },
                                            onComplete = {
                                                viewModel.completeTopic(screen.lesson.topicId)
                                                currentScreen = Screen.Dashboard
                                            },
                                            onSpeak = { text, loc -> viewModel.speak(text, loc) },
                                            onAITutorClick = {
                                                viewModel.setContext(subjectTitle = subjectName, lessonTitle = screen.lesson.title)
                                                isAITutorOpen = true
                                            }
                                        )
                                    }
                                }
                                is Screen.QuizView -> {
                                    val questions by viewModel.getQuizForTopic(screen.topicId).collectAsStateWithLifecycle(emptyList())
                                    GenericQuizScreen(
                                        questions = questions,
                                        onBack = { currentScreen = Screen.Dashboard },
                                        onComplete = {
                                            viewModel.completeQuiz(screen.topicId)
                                            currentScreen = Screen.Dashboard
                                        },
                                        onXpEarned = { viewModel.addPoints(it) }
                                    )
                                }
                                is Screen.AITutor -> {
                                    AITutorScreen(
                                        messages = messages,
                                        isLoading = loading,
                                        onSendMessage = { viewModel.sendChatMessage(it) },
                                        onClearChat = { viewModel.clearChat() },
                                        onBack = { currentScreen = Screen.Dashboard }
                                    )
                                }
                                is Screen.Profile -> {
                                    ProfileScreen(
                                        progress = progress,
                                        badges = badges,
                                        onToggleSubjectCompletion = { subjectId, completeAll ->
                                            viewModel.toggleSubjectCompletion(subjectId, completeAll)
                                        },
                                        onResetAllProgress = {
                                            viewModel.resetAllProgress()
                                        },
                                        onBack = { currentScreen = Screen.Dashboard }
                                    )
                                }
                            }
                        }
                    }

                    AITutorSidePanel(
                        isOpen = isAITutorOpen,
                        onDismiss = { isAITutorOpen = false },
                        messages = messages,
                        isLoading = loading,
                        currentContext = currentContext,
                        onSendMessage = { viewModel.sendChatMessage(it) },
                        onClearChat = { viewModel.clearChat() }
                    )
                }
            }
        }
    }
}
