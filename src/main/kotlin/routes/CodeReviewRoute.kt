package routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.route
import io.ktor.server.routing.post
import io.ktor.server.request.receive
import model.CodeReviewRequest
import service.CodeReviewService
import org.koin.ktor.ext.inject

fun Route.codeReviewRoute() {
    val codeReviewService by inject<CodeReviewService>()
    
    route("/api/review") {
        post {
            try {
                val request = call.receive<CodeReviewRequest>()
                val response = codeReviewService.reviewCode(request)
                call.respond(HttpStatusCode.OK, response)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
            } catch (e: Exception) {
                println("Error during code review: ${e.message}")
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "An error occurred while processing your request")
            }
        }
    }
}