package com.amazon.example.graphqllambda.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonConverter {

    Gson gson = new GsonBuilder().create();

    public <T> T fromJson(String json, Class<T> classOfT) {
        return gson.fromJson(json, classOfT);
    }

    public String toJson(Object src) {
        return gson.toJson(src);
    }

}
