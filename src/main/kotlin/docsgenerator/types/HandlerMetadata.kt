package docsgenerator.types

import org.springframework.web.bind.annotation.RequestMethod
import kotlin.reflect.KClass
import kotlin.reflect.KType

data class HandlerMetadata(
    val controllerName: String,
    val functionName: String,
    val handlerResponseStatus: ResponseStatus?,

    val method: RequestMethod,
    val path: String,

    val paramsType: Map<String, KType>,
    val queryStringType: Map<String, KType>,
    val requestBodyType: KClass<*>?,

    val successResponseType: KClass<*>,
    val errorResponseType: KClass<*>?
)
