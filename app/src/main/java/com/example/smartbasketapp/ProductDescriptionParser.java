package com.example.smartbasketapp;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import java.util.Map;

public final class ProductDescriptionParser {

    private static final int MAX_DEPTH = 5;

    private ProductDescriptionParser() {}

    public static String extract(String json) {
        if (json == null || json.trim().isEmpty()) return null;
        try {
            return find(JsonParser.parseString(json), 0);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static String find(JsonElement element, int depth) {
        if (element == null || element.isJsonNull() || depth > MAX_DEPTH) return null;

        if (element.isJsonArray()) {
            for (JsonElement item : element.getAsJsonArray()) {
                String result = find(item, depth + 1);
                if (result != null) return result;
            }
            return null;
        }

        if (element.isJsonPrimitive()) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (!primitive.isString()) return null;
            String text = primitive.getAsString().trim();
            if (text.startsWith("{") || text.startsWith("[")) {
                try {
                    return find(JsonParser.parseString(text), depth + 1);
                } catch (RuntimeException e) {
                    return null;
                }
            }
            return null;
        }

        JsonObject object = element.getAsJsonObject();
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            if (entry.getKey().equalsIgnoreCase("description")) {
                JsonElement value = entry.getValue();
                if (value.isJsonPrimitive()) {
                    String text = value.getAsString().trim();
                    if (!text.isEmpty()) return text;
                }
            }
        }
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            String result = find(entry.getValue(), depth + 1);
            if (result != null) return result;
        }
        return null;
    }
}
