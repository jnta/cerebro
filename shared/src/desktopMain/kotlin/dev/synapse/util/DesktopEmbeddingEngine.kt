package dev.synapse.util

import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class DesktopEmbeddingEngine : EmbeddingEngine {
    private val env = OrtEnvironment.getEnvironment()
    private var session: OrtSession? = null
    private var tokenizer: HuggingFaceTokenizer? = null

    init {
        try {
            val modelStream = this::class.java.classLoader.getResourceAsStream("model.onnx")
            val tokenizerStream = this::class.java.classLoader.getResourceAsStream("tokenizer.json")
            
            if (modelStream != null && tokenizerStream != null) {
                session = env.createSession(modelStream.readAllBytes())
                
                val tempFile = File.createTempFile("tokenizer", ".json")
                tempFile.deleteOnExit()
                tempFile.outputStream().use { tokenizerStream.copyTo(it) }
                tokenizer = HuggingFaceTokenizer.newInstance(tempFile.toPath())
            } else {
                println("Resonance model files not found in resources.")
            }
        } catch (e: Exception) {
            println("Failed to initialize DesktopEmbeddingEngine: ${e.message}")
        }
    }

    override suspend fun generateEmbedding(text: String): FloatArray? = withContext(Dispatchers.IO) {
        val tok = tokenizer ?: return@withContext null
        val sess = session ?: return@withContext null
        
        try {
            val encoding = tok.encode(text)
            val ids = encoding.ids
            val mask = encoding.attentionMask
            val typeIds = encoding.typeIds
            
            val idsTensor = OnnxTensor.createTensor(env, arrayOf(ids))
            val maskTensor = OnnxTensor.createTensor(env, arrayOf(mask))
            val typeIdsTensor = OnnxTensor.createTensor(env, arrayOf(typeIds))
            
            val inputs = mapOf(
                "input_ids" to idsTensor,
                "attention_mask" to maskTensor,
                "token_type_ids" to typeIdsTensor
            )
            
            val result = sess.run(inputs)
            @Suppress("UNCHECKED_CAST")
            val output = result.get(0).value as Array<Array<FloatArray>>
            
            val dim = output[0][0].size
            val pooled = FloatArray(dim)
            for (i in output[0].indices) {
                for (j in 0 until dim) {
                    pooled[j] += output[0][i][j]
                }
            }
            for (j in 0 until dim) {
                pooled[j] /= output[0].size.toFloat()
            }
            
            return@withContext normalize(pooled)
        } catch (e: Exception) {
            println("Embedding generation failed: ${e.message}")
            null
        }
    }

    private fun normalize(v: FloatArray): FloatArray {
        var norm = 0f
        for (x in v) norm += x * x
        norm = kotlin.math.sqrt(norm)
        if (norm == 0f) return v
        for (i in v.indices) v[i] /= norm
        return v
    }
}
