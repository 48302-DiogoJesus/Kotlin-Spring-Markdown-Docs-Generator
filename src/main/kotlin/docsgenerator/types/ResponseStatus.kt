package docsgenerator.types

import org.springframework.http.HttpStatus

@Target(AnnotationTarget.FUNCTION)
annotation class ResponseStatus(
    val success: Array<HttpStatus> = [HttpStatus.OK],
    val error: Array<HttpStatus>
)
