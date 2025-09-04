package me.mbolotov.json.schema.generator

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.JsonNodeType
import org.everit.json.schema.loader.SchemaLoader
import org.json.JSONObject
import org.json.JSONTokener

object JsonSchemaGenerator {
    private val objectMapper = ObjectMapper()

    fun outputAsString(
        title: String?,
        description: String?,
        json: String
    ): String {
        val jsonNode = objectMapper.readTree(json)
        val schema = generateSchema(title, description, jsonNode)
        return cleanup(schema.toString())
    }

    private fun generateSchema(
        title: String?,
        description: String?,
        jsonNode: JsonNode
    ): JSONObject {
        val schema = JSONObject()
        title?.let { schema.put("title", it) }
        description?.let { schema.put("description", it) }
        schema.put("type", "object")
        schema.put("properties", generateProperties(jsonNode))
        return schema
    }

    private fun generateProperties(jsonNode: JsonNode): JSONObject {
        val properties = JSONObject()
        jsonNode.fields().forEach { (fieldName, node) ->
            properties.put(fieldName, generateNodeSchema(node))
        }
        return properties
    }

    private fun generateNodeSchema(node: JsonNode): JSONObject {
        val schema = JSONObject()
        when (node.nodeType) {
            JsonNodeType.ARRAY -> {
                schema.put("type", "array")
                if (node.isArray && node.size() > 0) {
                    schema.put("items", generateNodeSchema(node[0]))
                }
            }
            JsonNodeType.BOOLEAN -> schema.put("type", "boolean")
            JsonNodeType.NUMBER -> schema.put("type", "number")
            JsonNodeType.OBJECT -> {
                schema.put("type", "object")
                schema.put("properties", generateProperties(node))
            }
            JsonNodeType.STRING -> schema.put("type", "string")
            JsonNodeType.NULL -> schema.put("type", "null")
            else -> {
                // Ignoring unsupported types
            }
        }
        return schema
    }

    private fun cleanup(dirty: String): String {
        val rawSchema = JSONObject(JSONTokener(dirty))
        val schema = SchemaLoader.load(rawSchema)
        return schema.toString()
    }
}
