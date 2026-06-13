package ru.sapozhnikov.aiagent.data.local

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import javax.inject.Inject
import javax.inject.Singleton

/** Сериализует и десериализует карту фактов в JSON. */
@Singleton
internal class FactsJsonSerializer @Inject constructor() {

    private val gson = Gson()
    private val mapType = object : TypeToken<Map<String, String>>() {}.type

    fun toJson(facts: Map<String, String>): String {
        return gson.toJson(facts)
    }

    fun fromJson(json: String): Map<String, String> {
        if (json.isBlank()) return emptyMap()
        return gson.fromJson(json, mapType) ?: emptyMap()
    }
}
