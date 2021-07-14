import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.serialization.*
import kotlinx.serialization.json.Json

import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL
import kr.co.shineware.nlp.komoran.core.Komoran

fun main(args: Array<String>){
    val nlpEngine = Komoran(DEFAULT_MODEL.LIGHT)

    val server = embeddedServer(Netty, 80){
        install(ContentNegotiation){
            json(Json {
                prettyPrint = true
                isLenient = true
            })
        }
        routing {
            post("/nlp"){
                val requestedText = call.receiveText()
                println("requested: $requestedText")
                val processed = nlpEngine.analyze(requestedText)
                processed.tokenList.forEach { token ->
                    println(" ${token.morph}(${token.pos})")
                }
                val intendEngine = IntendParser(processed.tokenList.toTypedArray())
                val res = intendEngine.getIoTProcotol()

                call.respond(HttpStatusCode(200, "OK"), res)

            }


        }
    }

    server.start(wait = true)
}