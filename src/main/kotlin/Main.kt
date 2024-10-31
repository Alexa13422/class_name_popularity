package org.example

import com.google.gson.JsonParser
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.regex.Pattern

fun main() = runBlocking {
    val repos = getJavaRepos()
    val allClassNames = mutableListOf<String>()

    repos.map { repo ->
        async { getJavaFiles(repo) }
    }.awaitAll().flatten().forEach { classUrl ->
        val lastSegment = classUrl.substringAfterLast("/").removeSuffix(".java")
        allClassNames.add(lastSegment)
    }

    val wordCount = analyzeWordPopularity(allClassNames)
    val sortedWords = wordCount.entries.sortedByDescending { it.value }

    for ((word, count) in sortedWords.take(10)) {
        println("$word: $count")
    }
}
val client = OkHttpClient()
const val token = "your token here"

fun getJavaRepos() : List<String> {
    val client = OkHttpClient()
    val repos = mutableListOf<String>()
    val url = "https://api.github.com/search/repositories?q=language:Java&sort=stars&order=desc&per_page=100"
    val request = Request.Builder()
        .url(url)
        .header("Authorization", "token $token")
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) throw IOException("Unexpected code $response")

        val jsonData = response.body?.string()
        val jsonObject = JsonParser.parseString(jsonData).asJsonObject
        val items = jsonObject.getAsJsonArray("items")

        for (item in items) {
            val repo = item.asJsonObject.get("full_name").asString
            repos.add(repo)
        }

    }
    return repos;
}
fun getJavaFiles(repo: String): List<String> {
    val javaFiles = mutableListOf<String>()
    val url = "https://api.github.com/repos/$repo/git/trees/main?recursive=1"
    val request = Request.Builder()
        .url(url)
        .header("Authorization", "token $token")
        .build()

    client.newCall(request).execute().use { response ->
        if (response.isSuccessful) {
            val jsonData = response.body?.string()
            val jsonObject = JsonParser.parseString(jsonData).asJsonObject
            val tree = jsonObject.getAsJsonArray("tree")

            for (element in tree) {
                val path = element.asJsonObject.get("path").asString
                if (path.endsWith(".java")) {
                    javaFiles.add(path)
                }
            }
        }
    }
    return javaFiles
}

fun analyzeWordPopularity(classNames: List<String>): Map<String, Int> {
    val wordCount = mutableMapOf<String, Int>()
    val camelCasePattern = Pattern.compile("([A-Z]?[a-z]+|[A-Z]+(?![a-z]))")

    for (className in classNames) {
        val matcher = camelCasePattern.matcher(className)
        while (matcher.find()) {
            val word = matcher.group(1).lowercase()
            wordCount[word] = wordCount.getOrDefault(word, 0) + 1
        }
    }
    return wordCount
}


