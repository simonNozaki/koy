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

/**
 * Unit tests for the routing layer.
 * The runCode function is replaced with a stub to isolate HTTP behavior from interpreter logic.
 */
class RoutingUnitTest {
    @Test
    fun `should return output when runCode succeeds`() = testApplication {
        application { configure(runCode = { RunResponse(output = "42") }) }

        val response = client.post("/run") {
            contentType(ContentType.Application.Json)
            setBody("""{"code":"val x = 42;"}""")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.decodeFromString<RunResponse>(response.bodyAsText())
        assertEquals("42", body.output)
        assertNull(body.error)
    }

    @Test
    fun `should return error when runCode fails`() = testApplication {
        application { configure(runCode = { RunResponse(error = "SomeException: bad code\n\tat ...") }) }

        val response = client.post("/run") {
            contentType(ContentType.Application.Json)
            setBody("""{"code":"???"}""")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.decodeFromString<RunResponse>(response.bodyAsText())
        assertNull(body.output)
        assertNotNull(body.error)
    }

    @Test
    fun `should pass received code to runCode`() = testApplication {
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
