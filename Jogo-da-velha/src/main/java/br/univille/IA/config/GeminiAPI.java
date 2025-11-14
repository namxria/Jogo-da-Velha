package br.univille.IA.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GeminiAPI {

    @Value("${gemini.api.key}")
    private String apiKey;

    public String getKey() {
        return apiKey;
    }
}