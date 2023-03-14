package docsgenerator.docslib

import docsgenerator.utils.buildHandlersMetadata
import docsgenerator.types.ResponseStatus
import docsgenerator.types.TypeName
import docsgenerator.utils.TSTypesGenerator
import docsgenerator.utils.buildHandlersMetadata
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping
import java.io.File

data class HandlerMetadataWithTypes(
    val controllerName: String,
    val functionName: String,
    val method: RequestMethod,
    val responseStatus: ResponseStatus?,

    val path: String,
    val pathVars: String?,
    val queryStringType: String?,
    val requestBodyType: String?,
    val successResponseType: String,
    val errorResponseType: String?
)

fun generateDocs(
    requestMappingHandlerMapping: RequestMappingInfoHandlerMapping,
    buildDirectory: String
) {
    val typesGen = TSTypesGenerator()

    val handlersMetadata =
        buildHandlersMetadata(requestMappingHandlerMapping)

    handlersMetadata.map {
        val requestBodyType = if (it.requestBodyType != null)
            typesGen.fromKClass(it.requestBodyType)
        else null
        val successType = typesGen.fromKClass(it.successResponseType)
        val errorType = if (it.errorResponseType != null)
            typesGen.fromKClass(it.errorResponseType)
        else null

        // Anonymous types
        val paramsType = if (it.paramsType.isEmpty()) null else typesGen.fromMap(it.paramsType)
        val queryStringType = if (it.queryStringType.isEmpty()) null else typesGen.fromMap(it.queryStringType)

        val mergedTypesForHandler: Map<TypeName, String> = (
                (requestBodyType?.typesCreated ?: mapOf()) +
                        (paramsType?.typesCreated ?: mapOf()) +
                        (queryStringType?.typesCreated ?: mapOf()) +
                        successType.typesCreated +
                        (errorType?.typesCreated ?: mapOf())
                ).mapValues { e -> e.value.removePrefix("export interface ") }

        return@map Pair(
            HandlerMetadataWithTypes(
                it.controllerName, it.functionName, it.method, it.handlerResponseStatus, it.path,
                paramsType?.result?.removePrefix("export interface "),
                queryStringType?.result?.removePrefix("export interface "),
                requestBodyType?.result?.removePrefix("export interface "),
                successType.result.removePrefix("export interface "),
                errorType?.result?.removePrefix("export interface ")
            ),
            mergedTypesForHandler
        )
    }
        .let {
            val typesToWrite = it.map { p -> p.second }.flatMap { fp -> fp.entries }
                .fold(mutableMapOf<TypeName, String>()) { acc, e ->
                    acc[e.key] = e.value
                    acc
                }
                .values
                .joinToString("\n\n") { typeStr -> "```json\n${typeStr}\n```" }

            // Create build directory if not exists
            File(buildDirectory).mkdir()

            // Write all the involved named types to a file
            File("${buildDirectory}/api-types.md")
                .writeText(typesToWrite)

            return@let it.map { p -> p.first }
        }
        .groupBy { it.controllerName }
        .also {
            val md = StringBuilder()
            md.appendLine("# Spring API Documentation\n")
            md.appendLine("###### (Auto-generated)\n")
            md.appendLine("---\n")

            val errorResponseType = it.values.flatten().find { h -> h.errorResponseType != null }?.errorResponseType
                ?: throw Error("No Error Type Found")

            md.appendLine("## Global Error Format\n")
            md.appendLine("```json\n${errorResponseType}\n```")

            // Build Markdown docs
            for ((controllerName, controllerHandlers) in it) {
                md.appendLine("---\n")
                md.appendLine("## $controllerName\n")

                for (handler in controllerHandlers) {
                    // Put path variables inline with {path}
                    val actualPath = if (handler.pathVars != null) {
                        var pathTransformed = handler.path
                        handler.pathVars.split("\n")
                            .mapNotNull { line ->
                                if (!line.contains(":")) null else line.trim().removeSuffix(",").replace("?", "")
                            }.map { nameType -> nameType.split(": ") }
                            .forEach { (name, type) ->
                                pathTransformed = pathTransformed.replace("{${name}}", "{$name: $type}")
                            }
                        pathTransformed
                    } else {
                        handler.path
                    }

                    md.append("- ### ${handler.method} $actualPath")

                    if (handler.queryStringType != null) {
                        md.append("?")
                        val types = handler.queryStringType.split("\n")
                            .mapNotNull { line ->
                                if (!line.contains(":")) null else line.trim().removeSuffix(",").replace("?", "")
                            }
                            .map { nameType -> nameType.split(": ") }

                        var i = 0
                        for ((name, type) in types) {
                            md.append("{$name: $type}")
                            if (i != types.size - 1)
                                md.append("&")
                            i++
                        }
                    }

                    md.appendLine("\n")

                    if (handler.requestBodyType != null) {
                        md.appendLine("#### Request Body\n")
                        md.appendLine("```json\n${handler.requestBodyType}\n```")
                    }

                    md.appendLine("#### Response Body\n")
                    md.appendLine("```json\n${handler.successResponseType}\n```")

                    md.appendLine("#### Success Responses\n")
                    if (handler.responseStatus == null) {
                        md.appendLine("`200` OK\n")
                    } else {
                        handler.responseStatus.success.forEach { status ->
                            md.appendLine("`${status.value()}` ${status.name}\n")
                        }
                        md.appendLine("#### Error Responses\n")
                        handler.responseStatus.error.forEach { status ->
                            md.appendLine("`${status.value()}` ${status.name}\n")
                        }
                    }
                }
                md.appendLine("\n")
            }

            File("${buildDirectory}/api-docs.md")
                .writeText(md.toString())

            println("[API Documentation Generator] DONE | Output at: $buildDirectory")
        }
}