package me.mbolotov.json.schema.generator

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.JsonNodeType
import com.fasterxml.jackson.databind.node.JsonNodeType.*
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
        val node: JsonNode?
        when (nodeType) {
            ARRAY -> {
                node = jsonNode[key][0]
                result.append("array\", \"items\": { \"properties\":")
                result.append(outputAsString(null, null, node.toString(), ARRAY))
                result.append("}},")
            }
            BOOLEAN -> result.append("boolean\" },")
            NUMBER -> result.append("number\" },")
            OBJECT -> {
                node = jsonNode[key]
                result.append("object\", \"properties\": ")
                result.append(outputAsString(null, null, node.toString(), OBJECT))
                result.append("},")
            }
            STRING -> result.append("string\" },")
            NULL -> result.append("null\" },")
            BINARY, MISSING, POJO -> {/*just skipping it*/}
        }
        return result.toString()
    }

    private fun cleanup(dirty: String): String {
        val rawSchema = JSONObject(JSONTokener(dirty))
        val schema = SchemaLoader.load(rawSchema)
        return schema.toString()
    }
}
