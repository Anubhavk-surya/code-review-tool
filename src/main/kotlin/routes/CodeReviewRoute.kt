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
import utils.LoggerUtils

internal fun Route.codeReviewRoute() {
    val logger = LoggerUtils.logger<Route>()
    val codeReviewService by inject<CodeReviewService>()
    
    route("/api/review") {
        post {
            try {
                val request = call.receive<CodeReviewRequest>()
                val response = codeReviewService.reviewCode(request)
                call.respond(
                    status = HttpStatusCode.OK,
                    message = response
                )
            } catch (e: IllegalArgumentException) {
                logger.warn("Invalid request received: {}", e.message)
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = e.message ?: "Invalid request"
                )
            } catch (e: Exception) {
                logger.error("Error during code review: {}", e.message, e)
                call.respond(
                    status = HttpStatusCode.InternalServerError,
                    message = "An error occurred while processing your request"
                )
            }
        }
    }
}
