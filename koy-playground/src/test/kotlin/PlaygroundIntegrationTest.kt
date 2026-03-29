import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Integration tests for the playground server.
 * Uses the real interpreter to verify end-to-end behavior via HTTP.
 */
class PlaygroundIntegrationTest {
    private fun post(code: String, assert: suspend (RunResponse) -> Unit) = testApplication {
        application { configure() }

        val response = client.post("/run") {
            contentType(ContentType.Application.Json)
            setBody("""{"code":${Json.encodeToString(code)}}""")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assert(Json.decodeFromString(response.bodyAsText()))
    }

    @Test
    fun `should execute println and return output`() = post("println(42);") { body ->
        assertEquals("42", body.output?.trim())
        assertNull(body.error)
    }

    @Test
    fun `should execute arithmetic and string expression`() = post(
        """
        val x = 10;
        val y = 20;
        println(x + y);
        """.trimIndent(),
    ) { body ->
        assertEquals("30", body.output?.trim())
        assertNull(body.error)
    }

    @Test
    fun `should return error on parse failure`() = post("???invalid???") { body ->
        assertNull(body.output)
        assertNotNull(body.error)
    }

    @Test
    fun `should return stack trace on runtime error`() = post("println(undeclared);") { body ->
        assertNull(body.output)
        assertNotNull(body.error)
        assertTrue(body.error.isNotBlank())
    }
}
