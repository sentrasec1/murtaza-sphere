package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.*
import com.example.data.network.Content
import com.example.data.network.GeminiRequest
import com.example.data.network.Part
import com.example.data.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

data class ChatMessage(val text: String, val isUser: Boolean)

class EduQuestViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SphereRepository
    private val audioService: AudioService

    val userProgress: StateFlow<UserProgress> = flow {
        val database = SphereDatabase.getDatabase(application)
        val repo = SphereRepository(database.progressDao(), database.sphereDao())
        emitAll(repo.userProgress.map { it ?: UserProgress() })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProgress())

    val subjects: StateFlow<List<SubjectEntity>> = flow {
        val database = SphereDatabase.getDatabase(application)
        val repo = SphereRepository(database.progressDao(), database.sphereDao())
        emitAll(repo.subjects)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val badges: StateFlow<List<BadgeEntity>> = flow {
        val database = SphereDatabase.getDatabase(application)
        val dao = database.sphereDao()
        emitAll(dao.getAllBadges())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        val database = SphereDatabase.getDatabase(application)
        repository = SphereRepository(database.progressDao(), database.sphereDao())
        audioService = AudioService(application)
        
        viewModelScope.launch {
            repository.updateStreak()
            repository.populateMockData()
        }
    }

    fun speak(text: String, locale: Locale) {
        audioService.speak(text, locale)
    }

    fun listen(locale: Locale, onResult: (String) -> Unit, onError: (String) -> Unit) {
        audioService.listen(locale, onResult, onError)
    }

    fun addPoints(points: Int) {
        viewModelScope.launch {
            repository.addPoints(points)
        }
    }

    fun completeTopic(topicId: String) {
        viewModelScope.launch {
            repository.completeTopic(topicId)
        }
    }

    fun completeQuiz(topicId: String) {
        viewModelScope.launch {
            repository.completeQuiz(topicId)
        }
    }

    fun toggleSubjectCompletion(subjectId: String, completeAll: Boolean) {
        viewModelScope.launch {
            repository.toggleSubjectCompletion(subjectId, completeAll)
        }
    }

    fun resetAllProgress() {
        viewModelScope.launch {
            repository.resetAllProgress()
        }
    }

    // Navigation and State
    fun getTopicsForSubject(subjectId: String): Flow<List<TopicEntity>> = repository.getTopicsForSubject(subjectId)
    fun getLessonsForTopic(topicId: String): Flow<List<LessonEntity>> = repository.getLessonsForTopic(topicId)
    fun getQuizForTopic(topicId: String): Flow<List<QuizQuestionEntity>> = repository.getQuizForTopic(topicId)

    // AI Tutor
    private val _currentContext = MutableStateFlow<String?>(null)
    val currentContext = _currentContext.asStateFlow()

    fun setContext(subjectTitle: String, topicTitle: String? = null, lessonTitle: String? = null) {
        val contextStr = buildString {
            append("Subject: $subjectTitle")
            if (topicTitle != null) {
                append(", Topic: $topicTitle")
            }
            if (lessonTitle != null) {
                append(", Lesson: $lessonTitle")
            }
        }
        _currentContext.value = contextStr
        
        // Reset/update greeting with current topic/subject context if there is no user chat history yet
        val welcomeMsg = "Hi! I am your AI Tutor. I see you are studying **${topicTitle ?: subjectTitle}**. Ask me any question related to it!"
        _chatMessages.value = listOf(ChatMessage(welcomeMsg, false))
    }

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(ChatMessage("Hi! I am your AI Tutor. Ask me any question!", false))
    )
    val chatMessages = _chatMessages.asStateFlow()
    private val _chatLoading = MutableStateFlow(false)
    val chatLoading = _chatLoading.asStateFlow()

    fun clearChat() {
        val topicOrSubject = _currentContext.value?.split(",")?.firstOrNull()?.replace("Subject: ", "") ?: "your current topic"
        _chatMessages.value = listOf(ChatMessage("Hi! I am your AI Tutor. Ask me any question about $topicOrSubject!", false))
    }

    fun sendChatMessage(text: String) {
        if (text.isBlank() || _chatLoading.value) return
        _chatMessages.value = _chatMessages.value + ChatMessage(text, true)
        _chatLoading.value = true
        viewModelScope.launch {
            val response = callGeminiTutor()
            _chatMessages.value = _chatMessages.value + ChatMessage(response, false)
            _chatLoading.value = false
        }
    }

    private suspend fun callGeminiTutor(): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey == "YOUR_GEMINI_API_KEY_HERE" || apiKey.isBlank()) {
            return@withContext "I'm in offline demo mode. Please configure your GEMINI_API_KEY in the Secrets panel in AI Studio to talk to me live!"
        }
        
        val contextStr = _currentContext.value ?: "General Subjects"
        val systemPrompt = """
            You are a friendly, encouraging, and highly knowledgeable educational tutor for kids aged 10-15.
            The student is currently studying: $contextStr.
            Provide clear, age-appropriate explanations, fun analogies, and short, engaging examples.
            Format your responses with clean, readable layout: use bullet points and bold text where appropriate.
            If the student asks a question unrelated to the learning topic, gently guide them back or answer it briefly while relating it back to their studies.
            Keep your answers structured, readable, and concise.
        """.trimIndent()

        // Build conversation history starting with user role
        val historyContents = mutableListOf<Content>()
        _chatMessages.value.forEach { msg ->
            val role = if (msg.isUser) "user" else "model"
            if (historyContents.isEmpty() && role != "user") {
                // First turn must be user
                return@forEach
            }
            historyContents.add(Content(parts = listOf(Part(text = msg.text)), role = role))
        }

        if (historyContents.isEmpty()) {
            return@withContext "Please ask a question first!"
        }

        val request = GeminiRequest(
            contents = historyContents,
            systemInstruction = Content(parts = listOf(Part(text = systemPrompt)))
        )
        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "I am having trouble understanding that. Could you rephrase your question?"
        } catch (e: Exception) {
            "Sorry, I encountered an error connecting to my server. Please try again! Error: ${e.message}"
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioService.destroy()
    }
}
