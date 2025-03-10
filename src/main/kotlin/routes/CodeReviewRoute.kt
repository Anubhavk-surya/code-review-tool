package routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import service.CodeReviewService

fun Route.codeReviewRoute(codeReviewService: CodeReviewService) {
    route("/api/review") {
        get("{fileName}") {
            try {
                val fileName = call.parameters["fileName"] 
                    ?: return@get call.respond(HttpStatusCode.BadRequest, "File name is required")
                
                val response = codeReviewService.reviewCode(fileName)
                call.respond(HttpStatusCode.OK, response)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "An error occurred while processing your request")
            }
        }
    }
}