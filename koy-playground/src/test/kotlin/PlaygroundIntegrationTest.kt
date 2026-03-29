import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Integration tests for the playground server.
 * Uses the real interpreter to verify end-to-end behavior via HTTP.
 */
class PlaygroundIntegrationTest {
    @Nested
    inner class `when valid koy code is submitted` {
        @Test
        fun `should return println output`() = testApplication {
            application { configure() }

            val response = client.post("/run") {
                contentType(ContentType.Application.Json)
                setBody("""{"code":"println(42);"}""")
            }

            val body = Json.decodeFromString<RunResponse>(response.bodyAsText())
            assertEquals("42", body.output?.trim())
            assertNull(body.error)
        }

        @Test
        fun `should evaluate arithmetic and return result via println`() = testApplication {
            application { configure() }

            val response = client.post("/run") {
                contentType(ContentType.Application.Json)
                setBody("""{"code":"val x = 10;\nval y = 20;\nprintln(x + y);"}""")
            }

            val body = Json.decodeFromString<RunResponse>(response.bodyAsText())
            assertEquals("30", body.output?.trim())
            assertNull(body.error)
        }
    }

    @Nested
    inner class `when invalid koy code is submitted` {
        @Test
        fun `should return error on parse failure`() = testApplication {
            application { configure() }

            val response = client.post("/run") {
                contentType(ContentType.Application.Json)
                setBody("""{"code":"???invalid???"}""")
            }

            val body = Json.decodeFromString<RunResponse>(response.bodyAsText())
            assertNull(body.output)
            assertNotNull(body.error)
        }

        @Test
        fun `should return stack trace when undeclared variable is referenced`() = testApplication {
            application { configure() }

            val response = client.post("/run") {
                contentType(ContentType.Application.Json)
                setBody("""{"code":"println(undeclared);"}""")
            }

            val body = Json.decodeFromString<RunResponse>(response.bodyAsText())
            assertNull(body.output)
            assertTrue(body.error!!.isNotBlank())
        }
    }
}
