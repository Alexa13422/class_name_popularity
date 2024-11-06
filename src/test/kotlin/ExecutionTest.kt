package org.example

import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

class ExampleTests {
    private lateinit var server: MockWebServer

    @BeforeEach
    fun setup() {
        server = MockWebServer()
        server.start()
    }

    @AfterEach
    fun teardown() {
        server.shutdown()
    }

    @Test
    fun `test getJavaRepos`() {
        val mockJsonResponse = """
            {
                "items": [
                    {"full_name": "user/repo1"},
                    {"full_name": "user/repo2"}
                ]
            }
        """.trimIndent()

        server.enqueue(MockResponse().setBody(mockJsonResponse).setResponseCode(200))
        val baseUrl = server.url("/").toString()
        val repos = getJavaRepos(baseUrl)

        assertEquals(2, repos.size)
        assertTrue(repos.contains("user/repo1"))
        assertTrue(repos.contains("user/repo2"))
    }

    @Test
    fun `test getJavaFiles`() {
        val mockJsonResponse = """
            {
                "tree": [
                    {"path": "src/Main.java"},
                    {"path": "README.md"},
                    {"path": "src/Utils.java"}
                ]
            }
        """.trimIndent()

        server.enqueue(MockResponse().setBody(mockJsonResponse).setResponseCode(200))
        val baseUrl = server.url("/").toString()

        val javaFiles = getJavaFiles("user/repo1", baseUrl)
        assertEquals(2, javaFiles.size)
        assertTrue(javaFiles.contains("src/Main.java"))
        assertTrue(javaFiles.contains("src/Utils.java"))
    }

    @Test
    fun `test analyzeWordPopularity`() {
        val classNames = listOf("MainClass", "HelperUtils", "MyTestCase")
        val wordCount = analyzeWordPopularity(classNames)

        assertEquals(7, wordCount.size)
        assertEquals(1, wordCount["main"])
        assertEquals(1, wordCount["class"])
        assertEquals(1, wordCount["helper"])
        assertEquals(1, wordCount["utils"])
    }
}
