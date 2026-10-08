package com.example.duplicatefinder

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.security.MessageDigest

data class MediaItem(
    val uri: Uri,
    val name: String,
    val sizeBytes: Long,
    val dateAdded: Long
)

data class DuplicateGroup(
    val title: String,
    val isExact: Boolean,
    val items: List<MediaItem>
)

object DuplicateDetector {

    suspend fun calculateSha256(context: Context, uri: Uri): String = withContext(Dispatchers.IO) {
        val digest = MessageDigest.getInstance("SHA-256")
        context.contentResolver.openInputStream(uri)?.use { stream ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (stream.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        } ?: return@withContext ""
        digest.digest().joinToString("") { "%02x".format(it) }
    }

    suspend fun calculateDHash(context: Context, uri: Uri): Long = withContext(Dispatchers.IO) {
        try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = false
                inSampleSize = 2
            }
            val originalBitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            } ?: return@withContext 0L

            // Resize to 9x8 for difference hash
            val small = Bitmap.createScaledBitmap(originalBitmap, 9, 8, true)
            var hash = 0L

            for (y in 0 until 8) {
                for (x in 0 until 8) {
                    val p1 = small.getPixel(x, y)
                    val p2 = small.getPixel(x + 1, y)

                    val b1 = (p1 and 0xFF) + ((p1 shr 8) and 0xFF) + ((p1 shr 16) and 0xFF)
                    val b2 = (p2 and 0xFF) + ((p2 shr 8) and 0xFF) + ((p2 shr 16) and 0xFF)

                    if (b1 > b2) {
                        hash = hash or (1L shl (y * 8 + x))
                    }
                }
            }
            if (small != originalBitmap) small.recycle()
            originalBitmap.recycle()
            hash
        } catch (e: Exception) {
            0L
        }
    }

    fun hammingDistance(hash1: Long, hash2: Long): Int {
        return java.lang.Long.bitCount(hash1 xor hash2)
    }

    suspend fun analyzeDuplicates(
        context: Context,
        items: List<MediaItem>,
        onProgress: (Int, Int) -> Unit
    ): Pair<List<DuplicateGroup>, List<DuplicateGroup>> = withContext(Dispatchers.Default) {
        val exactMap = mutableMapOf<String, MutableList<MediaItem>>()
        val hashes = mutableListOf<Pair<MediaItem, Long>>()

        items.forEachIndexed { index, item ->
            onProgress(index + 1, items.size)
            // Exact SHA-256
            val sha = calculateSha256(context, item.uri)
            if (sha.isNotBlank()) {
                exactMap.getOrPut(sha) { mutableListOf() }.add(item)
            }

            // Perceptual dHash
            val dHash = calculateDHash(context, item.uri)
            if (dHash != 0L) {
                hashes.add(Pair(item, dHash))
            }
        }

        // Exact duplicates groups (where count > 1)
        val exactGroups = exactMap.values
            .filter { it.size > 1 }
            .mapIndexed { idx, list ->
                DuplicateGroup(
                    title = "Exact Duplicate Set #${idx + 1}",
                    isExact = true,
                    items = list
                )
            }

        // Visually similar groups
        val visited = mutableSetOf<Uri>()
        val similarGroups = mutableListOf<DuplicateGroup>()
        var simGroupCount = 1

        for (i in 0 until hashes.size) {
            val (itemA, hashA) = hashes[i]
            if (visited.contains(itemA.uri)) continue

            val groupMembers = mutableListOf(itemA)
            for (j in i + 1 until hashes.size) {
                val (itemB, hashB) = hashes[j]
                if (visited.contains(itemB.uri)) continue

                // Distance <= 6 out of 64 bits indicates visual similarity
                if (hammingDistance(hashA, hashB) <= 6) {
                    groupMembers.add(itemB)
                    visited.add(itemB.uri)
                }
            }

            if (groupMembers.size > 1) {
                visited.add(itemA.uri)
                similarGroups.add(
                    DuplicateGroup(
                        title = "Visually Similar Set #${simGroupCount++}",
                        isExact = false,
                        items = groupMembers
                    )
                )
            }
        }

        Pair(exactGroups, similarGroups)
    }
}
