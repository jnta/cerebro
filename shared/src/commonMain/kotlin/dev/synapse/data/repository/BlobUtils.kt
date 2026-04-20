package dev.synapse.data.repository

object BlobUtils {
    fun toBlob(array: FloatArray): ByteArray {
        val bytes = ByteArray(array.size * 4)
        for (i in array.indices) {
            val bits = array[i].toBits()
            bytes[i * 4] = (bits shr 24).toByte()
            bytes[i * 4 + 1] = (bits shr 16).toByte()
            bytes[i * 4 + 2] = (bits shr 8).toByte()
            bytes[i * 4 + 3] = (bits).toByte()
        }
        return bytes
    }

    fun fromBlob(bytes: ByteArray): FloatArray {
        val array = FloatArray(bytes.size / 4)
        for (i in array.indices) {
            val bits = (bytes[i * 4].toInt() and 0xFF shl 24) or
                    (bytes[i * 4 + 1].toInt() and 0xFF shl 16) or
                    (bytes[i * 4 + 2].toInt() and 0xFF shl 8) or
                    (bytes[i * 4 + 3].toInt() and 0xFF)
            array[i] = Float.fromBits(bits)
        }
        return array
    }
}
