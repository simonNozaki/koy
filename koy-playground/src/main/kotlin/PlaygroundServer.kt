import io.github.simonnozaki.koy.Interpreter
import io.github.simonnozaki.koy.Parsers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.staticResources
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.serialization.Serializable
import org.javafp.parsecj.input.Input
import java.io.ByteArrayOutputStream
import java.io.PrintStream

@Serializable
data class RunRequest(
    val code: String,
)

@Serializable
data class RunResponse(
    val output: String? = null,
    val error: String? = null,
)

fun main() {
    embeddedServer(Netty, port = 8080) {
        configure()
    }.start(wait = true)
}

fun Application.configure(runCode: (String) -> RunResponse = ::executeCode) {
    install(ContentNegotiation) {
        json()
    }
    install(CORS) {
        allowMethod(HttpMethod.Post)
        allowHeader(HttpHeaders.ContentType)
        anyHost()
    }
    routing {
        post("/run") {
            val request = call.receive<RunRequest>()
            call.respond(runCode(request.code))
        }
        staticResources("/", "static")
    }
}

internal fun executeCode(code: String): RunResponse =
    try {
        val baos = ByteArrayOutputStream()
        val ps = PrintStream(baos)
        val interpreter = Interpreter(out = ps)
        Parsers
            .lines()
            .parse(Input.of(code))
            .result
            .forEach { interpreter.interpret(it) }
        ps.flush()
        RunResponse(output = baos.toString())
    } catch (e: Exception) {
        RunResponse(error = e.stackTraceToString())
    }
