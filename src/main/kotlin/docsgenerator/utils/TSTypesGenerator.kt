package docsgenerator.utils

import docsgenerator.types.TypeName
import java.sql.Date
import java.sql.Timestamp
import java.time.Instant
import java.util.UUID
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.full.memberProperties

/**
 * Something that can:
 * - convert kotlin data classes to TS interfaces
 * - convert kotlin Map to TS object type definition
 * */
class TSTypesGenerator() {
    private val typesCreated = mutableMapOf<String, String>()

    data class ConversionResult(
        val result: String,
        val typesCreated: Map<TypeName, String>
    )

    fun fromMap(
        map: Map<String, KType>,
        typeName: String? = null
    ): ConversionResult {
        val properties = map.entries.joinToString(",\n") { (propName, type) ->
            "\t${propName}${if (type.isMarkedNullable) "?" else ""}: ${convertKTypeToTSType(type)}${if (type.isMarkedNullable) " | null" else ""}"
        }
        val typesCopy = typesCreated.toMap()
        typesCreated.clear()

        return ConversionResult(
            if (typeName == null) {
                "{\n$properties\n}"
            } else {
                "export interface $typeName {$properties}"
            },
            typesCopy
        )
    }

    fun fromKClass(klass: KClass<*>, typeName: String? = null): ConversionResult {
        val result = if (!isUserType(klass)) {
            convertKTypeToTSType(klass.createType())
        } else {
            convertKClassInternal(klass, typeName)
        }

        val typesCopy = typesCreated.toMap()
        typesCreated.clear()
        return ConversionResult(result, typesCopy)
    }

    private fun convertKClassInternal(
        klass: KClass<*>,
        finalInterfaceName: String? = null
    ): String {
        val className = klass.simpleName
            ?: finalInterfaceName
            ?: throw Error("Class with no name tried to be converted")

        val cachedType = typesCreated[className]
        if (cachedType != null)
            return cachedType

        val properties = klass.memberProperties.joinToString(",\n") {
            val propertyType = it.returnType
            // prop ?: string | null || Using both undefined and null to allow developer to express itself
            // better by passing null instead of undefined or nothing
            val propertyTypeName =
                "${convertKTypeToTSType(propertyType)} ${
                    if (propertyType.isMarkedNullable) "| null" else ""
                }"

            "\t${it.name}${if (propertyType.isMarkedNullable) "?" else ""}: $propertyTypeName"
        }
        val builtType = "export interface $className {\n$properties\n}"
        typesCreated[className] = builtType
        return builtType
    }

    private fun convertKTypeToTSType(type: KType): String {
        return when (type.classifier) {
            String::class -> "string"
            Timestamp::class -> "Date"
            Instant::class -> "Date"
            Date::class -> "Date"
            UUID::class -> "string"
            Char::class -> "string"
            Int::class -> "number"
            Long::class -> "number"
            Float::class -> "number"
            Double::class -> "number"
            Byte::class -> "number"
            Boolean::class -> "boolean"
            Unit::class -> "void"
            List::class -> {
                val nestedType = type.arguments.firstOrNull()?.type ?: return "any[]"
                if (nestedType.isMarkedNullable) {
                    "(${convertKTypeToTSType(nestedType)} | null)[]"
                } else {
                    "${convertKTypeToTSType(nestedType)}[]"
                }
            }

            Map::class -> {
                val keyType = type.arguments.firstOrNull()?.type ?: return "{ [key: string]: any }"
                val valueType = type.arguments.getOrNull(1)?.type ?: return "{ [key: string]: any }"
                "{ [key: ${convertKTypeToTSType(keyType)}]: ${
                    convertKTypeToTSType(
                        valueType
                    )
                } }"
            }

            else -> {
                val nestedClass = (type.classifier as? KClass<*>) ?: return "any"
                val nestedClassName = nestedClass.simpleName ?: return "any"
                // Will create an interface for another type
                convertKClassInternal(nestedClass).replace(nestedClass.qualifiedName!!, nestedClassName)
                nestedClassName
            }
        }
    }
}

fun isUserType(type: KClass<*>): Boolean {
    return when (type) {
        String::class -> false
        Timestamp::class -> false
        Instant::class -> false
        Date::class -> false
        UUID::class -> false
        Char::class -> false
        Int::class -> false
        Long::class -> false
        Float::class -> false
        Double::class -> false
        Byte::class -> false
        Boolean::class -> false
        Unit::class -> false
        List::class -> false
        Map::class -> false
        else -> true
    }
}
