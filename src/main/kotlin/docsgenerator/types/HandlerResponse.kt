package docsgenerator.types

import docsgenerator.exampleAPI.utils.ErrorFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

// If the API has a global error format it's more readable/concise to do it like this
typealias HandlerResponseType<S> = ResponseEntity<HandlerResponse<S, ErrorFormat>>

class HandlerResponse<S, E> private constructor(
    val data: S?,
    val error: E?
) {
    companion object {
        fun <S, E> success(data: S, httpStatus: HttpStatus = HttpStatus.OK) =
            ResponseEntity
                .status(httpStatus)
                .body(HandlerResponse<S, E>(data, null))


        fun <S, E> error(httpStatus: HttpStatus, error: E) =
            ResponseEntity
                .status(httpStatus)
                .body(HandlerResponse<S, E>(null, error))
    }
}
