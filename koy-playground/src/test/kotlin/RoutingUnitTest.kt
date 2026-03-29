import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Unit tests for the routing layer.
 * The runCode function is replaced with a stub to isolate HTTP behavior from interpreter logic.
 */
class RoutingUnitTest {
    @Nested
    inner class `when runCode succeeds` {
        private fun test(block: suspend ApplicationTestBuilder.() -> Unit) = testApplication {
            application { configure(runCode = { RunResponse(output = "42") }) }
            block()
        }

        @Test
        fun `should respond with 200`() = test {
            val response = client.post("/run") {
                contentType(ContentType.Application.Json)
                setBody("""{"code":"val x = 42;"}""")
            }
            assertEquals(HttpStatusCode.OK, response.status)
        }

        @Test
        fun `should return output in response body`() = test {
            val response = client.post("/run") {
                contentType(ContentType.Application.Json)
                setBody("""{"code":"val x = 42;"}""")
            }
            val body = Json.decodeFromString<RunResponse>(response.bodyAsText())
            assertEquals("42", body.output)
            assertNull(body.error)
        }

        @Test
        fun `should pass received code string to runCode`() = testApplication {
            var capturedCode = ""
            application {
                configure(runCode = { code ->
                    capturedCode = code
                    RunResponse(output = "ok")
                })
            }
            client.post("/run") {
                contentType(ContentType.Application.Json)
                setBody("""{"code":"println(1);"}""")
            }
            assertEquals("println(1);", capturedCode)
        }
    }

    @Nested
    inner class `when runCode returns an error` {
        private fun test(block: suspend ApplicationTestBuilder.() -> Unit) = testApplication {
            application { configure(runCode = { RunResponse(error = "SomeException: bad code") }) }
            block()
        }

        @Test
        fun `should respond with 400`() = test {
            val response = client.post("/run") {
                contentType(ContentType.Application.Json)
                setBody("""{"code":"???"}""")
            }
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

        @Test
        fun `should return error in response body`() = test {
            val response = client.post("/run") {
                contentType(ContentType.Application.Json)
                setBody("""{"code":"???"}""")
            }
            val body = Json.decodeFromString<RunResponse>(response.bodyAsText())
            assertNull(body.output)
            assertNotNull(body.error)
        }
    }
}
