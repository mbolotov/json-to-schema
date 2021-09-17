package me.mbolotov.json.schema.generator

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.JsonNodeType
import org.everit.json.schema.loader.SchemaLoader
import org.json.JSONObject
import org.json.JSONTokener

// based on the code from https://github.com/simplymequeeny/json-string-schema-generator
object JsonSchemaGenerator {
    private val objectMapper = ObjectMapper()
    fun outputAsString(
        title: String?, description: String?,
        json: String
    ): String {
        return cleanup(outputAsString(title, description, json, null))
    }

    private fun outputAsString(
        title: String?, description: String?,
        json: String, type: JsonNodeType?
    ): String {
        val jsonNode = objectMapper.readTree(json)
        val output = StringBuilder()
        output.append("{")
        if (type == null) output.append(
            "\"title\": \"" +
                title + "\", \"description\": \"" +
                description + "\", \"type\": \"object\", \"properties\": {"
        )
        val iterator = jsonNode.fieldNames()
        while (iterator.hasNext()) {
            val fieldName = iterator.next()
            val nodeType = jsonNode[fieldName].nodeType
            output.append(convertNodeToStringSchemaNode(jsonNode, nodeType, fieldName))
        }
        if (type == null) output.append("}")
        output.append("}")
        return output.toString()
    }

    private fun convertNodeToStringSchemaNode(
        jsonNode: JsonNode, nodeType: JsonNodeType, key: String
    ): String {
        val result = StringBuilder("\"$key\": { \"type\": \"")
        var node: JsonNode? = null
        when (nodeType) {
            JsonNodeType.ARRAY -> {
                node = jsonNode[key][0]
                result.append("array\", \"items\": { \"properties\":")
                result.append(outputAsString(null, null, node.toString(), JsonNodeType.ARRAY))
                result.append("}},")
            }
            JsonNodeType.BOOLEAN -> result.append("boolean\" },")
            JsonNodeType.NUMBER -> result.append("number\" },")
            JsonNodeType.OBJECT -> {
                node = jsonNode[key]
                result.append("object\", \"properties\": ")
                result.append(outputAsString(null, null, node.toString(), JsonNodeType.OBJECT))
                result.append("},")
            }
            JsonNodeType.STRING -> result.append("string\" },")
        }
        return result.toString()
    }

    private fun cleanup(dirty: String): String {
        val rawSchema = JSONObject(JSONTokener(dirty))
        val schema = SchemaLoader.load(rawSchema)
        return schema.toString()
    }
}
