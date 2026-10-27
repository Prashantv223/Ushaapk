package com.example.service

import android.content.Context
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import java.util.concurrent.TimeUnit

class TtsManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var androidTts: TextToSpeech? = null
    private var isTtsInitialized = false
    private var mediaPlayer: MediaPlayer? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    init {
        androidTts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = androidTts?.setLanguage(Locale.getDefault())
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                androidTts?.setLanguage(Locale.US)
            }
            isTtsInitialized = true
        }
    }

    fun speak(
        text: String,
        onStart: () -> Unit = {},
        onDone: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (text.isBlank()) return

        stop()
        onStart()

        val apiKey = BuildConfig.GEMINI_API_KEY

        // If Gemini API key is provided and valid, attempt gemini-3.8-flash-tts first
        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            scope.launch {
                val success = tryGeminiTts(apiKey, text, onDone)
                if (!success) {
                    withContext(Dispatchers.Main) {
                        speakLocal(text, onDone, onError)
                    }
                }
            }
        } else {
            // Local fallback
            speakLocal(text, onDone, onError)
        }
    }

    private suspend fun tryGeminiTts(
        apiKey: String,
        text: String,
        onDone: () -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            // gemini-3.8-flash-tts as specified by feature requirement
            val modelName = "gemini-3.8-flash-tts"
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"

            val jsonBody = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val partsArray = JSONArray().apply {
                            put(JSONObject().apply { put("text", text) })
                        }
                        put("parts", partsArray)
                    }
                    put(contentObj)
                }
                put("contents", contentsArray)

                val genConfig = JSONObject().apply {
                    put("responseModalities", JSONArray().apply { put("AUDIO") })
                    put("speechConfig", JSONObject().apply {
                        put("voiceConfig", JSONObject().apply {
                            put("prebuiltVoiceConfig", JSONObject().apply {
                                put("voiceName", "Kore")
                            })
                        })
                    })
                }
                put("generationConfig", genConfig)
            }

            val request = Request.Builder()
                .url(url)
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.w("TtsManager", "Gemini TTS returned status ${response.code}")
                return@withContext false
            }

            val respBody = response.body?.string() ?: return@withContext false
            val jsonResp = JSONObject(respBody)
            val candidates = jsonResp.optJSONArray("candidates") ?: return@withContext false
            if (candidates.length() == 0) return@withContext false
            val firstCandidate = candidates.getJSONObject(0)
            val parts = firstCandidate.getJSONObject("content").getJSONArray("parts")
            var audioBase64: String? = null
            for (i in 0 until parts.length()) {
                val part = parts.getJSONObject(i)
                if (part.has("inlineData")) {
                    audioBase64 = part.getJSONObject("inlineData").optString("data")
                    break
                }
            }

            if (!audioBase64.isNullOrEmpty()) {
                val audioBytes = Base64.decode(audioBase64, Base64.DEFAULT)
                val tempFile = File(context.cacheDir, "gemini_tts_${System.currentTimeMillis()}.mp3")
                FileOutputStream(tempFile).use { it.write(audioBytes) }

                withContext(Dispatchers.Main) {
                    playAudioFile(tempFile, onDone)
                }
                return@withContext true
            }

            return@withContext false
        } catch (e: Exception) {
            Log.e("TtsManager", "Error in Gemini TTS", e)
            return@withContext false
        }
    }

    private fun playAudioFile(file: File, onDone: () -> Unit) {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                setOnCompletionListener {
                    onDone()
                    file.delete()
                }
                setOnErrorListener { _, _, _ ->
                    onDone()
                    file.delete()
                    true
                }
                prepare()
                start()
            }
        } catch (e: Exception) {
            onDone()
        }
    }

    private fun speakLocal(text: String, onDone: () -> Unit, onError: (String) -> Unit) {
        if (isTtsInitialized && androidTts != null) {
            androidTts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "CLR_TTS_ID")
            // Give estimate duration callback
            val durationMs = (text.length * 70L).coerceAtLeast(1000L)
            scope.launch {
                delay(durationMs)
                withContext(Dispatchers.Main) {
                    onDone()
                }
            }
        } else {
            onError("TTS engine initializing...")
            onDone()
        }
    }

    fun stop() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.stop()
            }
            mediaPlayer?.release()
            mediaPlayer = null
            androidTts?.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun shutdown() {
        stop()
        androidTts?.shutdown()
        androidTts = null
        scope.cancel()
    }
}
