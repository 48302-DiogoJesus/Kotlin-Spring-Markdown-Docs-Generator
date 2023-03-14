package docsgenerator.utils

import org.springframework.web.bind.annotation.RequestMethod
import kotlin.reflect.KClass
import kotlin.reflect.KType

data class ExtendedTypeInformation(
    val type: KClass<*>,
    val isUserType: Boolean
)

data class BuildTSFunctionHandlerMD(
    val path: String,
    val functionName: String,
    val method: RequestMethod,

    val paramsType: Map<String, KType>?,
    val queryStringType: Map<String, KType>?,

    val requestBodyType: ExtendedTypeInformation?,

    val successResponseType: ExtendedTypeInformation,
    val errorResponseType: ExtendedTypeInformation?
)

/**
 * ! REMOVE COMMENT LATER
 * - is it a user type? request, response, error
 * - ts types already created in the previous step for NAMED types
 * */
fun buildTSFunction(
    handlerMD: BuildTSFunctionHandlerMD
): String {
    val tsTypeGenerator = TSTypesGenerator()
    val stringBuilder = StringBuilder()

    // function arguments
    stringBuilder.appendLine("(args: { ")

    if (handlerMD.paramsType != null) {
        stringBuilder.appendLine(
            "\t${tsTypeGenerator.fromMap(handlerMD.paramsType).result.drop(1).dropLast(1)},"
        )
    }

    if (handlerMD.queryStringType != null) {
        stringBuilder.appendLine(
            "\t${tsTypeGenerator.fromMap(handlerMD.queryStringType).result.drop(1).dropLast(1)},"
        )
    }

    if (handlerMD.requestBodyType != null) {
        val toAppend = if (handlerMD.requestBodyType.isUserType) {
            "} & UserTypes.${handlerMD.requestBodyType.type.simpleName}"
        } else {
            "\t\t${tsTypeGenerator.fromKClass(handlerMD.requestBodyType.type).result.drop(1).dropLast(1)} }"
        }

        stringBuilder.appendLine("\t$toAppend")
    } else {
        stringBuilder.append("}")
    }
    stringBuilder.append("):")

    // return type
    val successResponseType = if (handlerMD.successResponseType.type == Unit::class) {
        "void"
    } else {
        if (handlerMD.successResponseType.isUserType) {
            "UserTypes.${handlerMD.successResponseType.type.simpleName}"
        } else {
            tsTypeGenerator.fromKClass(handlerMD.successResponseType.type).result
        }
    }

    val errorResponseType =
        if (handlerMD.errorResponseType == null)
            null
        else if (handlerMD.errorResponseType.type == Unit::class) {
            "void"
        } else {
            if (handlerMD.errorResponseType.isUserType) {
                "UserTypes.${handlerMD.errorResponseType.type.simpleName}"
            } else {
                tsTypeGenerator.fromKClass(handlerMD.errorResponseType.type).result
            }
        }

    if (errorResponseType == null) {
        stringBuilder.appendLine(" Promise<$successResponseType> => ")
    } else {
        stringBuilder.appendLine(" Promise<HandlerResponse<$successResponseType, $errorResponseType>> => ")
    }

    // Builds function body
    stringBuilder.appendLine("\tfetch(")
    stringBuilder.appendLine(
        "\t\treplacePathAndQueryVariables(`\${apiBaseUrl}${handlerMD.path}`, " +
                "${if (handlerMD.paramsType != null) "args" else "undefined"}, " +
                (if (handlerMD.queryStringType != null) "args" else "undefined") +
                "),"
    )

    stringBuilder.appendLine("\t\t{")

    stringBuilder.appendLine("\t\t\tmethod: \"${handlerMD.method}\",")

    if (handlerMD.requestBodyType != null) {
        // Builds request body if it exists
        stringBuilder.appendLine("\t\t\theaders: {\"Content-Type\": \"application/json\"},")
        // BY send {args} we are sending more bytes on the wire
        stringBuilder.appendLine("\t\t\tbody: JSON.stringify(args)")
    }

    stringBuilder.appendLine("\t\t}")

    stringBuilder.appendLine("\t)")
    stringBuilder.appendLine("\t\t.then(res => res.json())")

    return stringBuilder.toString()
}
