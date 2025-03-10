package routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.request.*
import model.CodeReviewRequest
import service.CodeReviewService

fun Route.codeReviewRoute(codeReviewService: CodeReviewService) {
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