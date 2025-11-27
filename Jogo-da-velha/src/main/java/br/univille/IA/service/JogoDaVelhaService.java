package br.univille.IA.service;

import br.univille.IA.config.GeminiAPI;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class JogoDaVelhaService {

    private final ObjectMapper mapper = new ObjectMapper();
    private final RestTemplateBuilder restTemplateBuilder;
    private final GeminiAPI geminiAPI;

    private String lastGeminiRaw = "";
    private String lastGeminiJson = "";

    public JogoDaVelhaService(RestTemplateBuilder restTemplateBuilder, GeminiAPI geminiAPI) {
        this.restTemplateBuilder = restTemplateBuilder;
        this.geminiAPI = geminiAPI;
    }

    public String getLastGeminiRaw() { return lastGeminiRaw; }
    public String getLastGeminiJson() { return lastGeminiJson; }

    public List<Integer> jogarJogoVelha(String jogada) {
        try {
            var rest = restTemplateBuilder.build();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-goog-api-key", geminiAPI.getKey());

            String body = """
                {
                  "contents": [
                    {
                      "parts": [
                        {
                          "text": "
                          Vou te enviar o tabuleiro do jogo da velha como um vetor de 9 posições.
                              Os valores significam:
                              - 0 = posição vazia
                              - 1 = jogada do humano
                              - 2 = sua jogada (IA)
                    
                              Suas regras obrigatórias:
                              1) Jogue APENAS em posições com valor 0.
                              2) Nunca altere posições que já são 1 ou 2.
                              3) Faça exatamente UMA jogada por turno, colocando um único número 2.
                              4) Devolva APENAS o vetor final, em JSON puro, sem nenhum texto extra.
                    
                              Regras de inteligência:
                              5) Se você puder vencer com uma jogada, vença.
                              6) Se o humano puder vencer na próxima jogada, bloqueie.
                              7) Caso nenhuma das anteriores se aplique, escolha a melhor jogada disponível: centro → cantos → laterais.
                    
                    
                              Tabuleiro: :TAB"
                        }
                      ]
                    }
                  ]
                }
                """.replace(":TAB", jogada);

            HttpEntity<String> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = rest.exchange(
                    "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent",
                    HttpMethod.POST,
                    request,
                    String.class
            );

            String json = response.getBody();

            String text = mapper.readTree(json)
                    .path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();

            String cleanJson = text
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();

            lastGeminiRaw = text;
            lastGeminiJson = cleanJson;

            return mapper.readValue(
                    cleanJson,
                    new TypeReference<List<Integer>>() {}
            );

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    public List<Integer> fromJson(String json) {
        try {
            return mapper.readValue(json, new TypeReference<List<Integer>>() {});
        } catch (Exception e) {
            e.printStackTrace();
            return List.of(0,0,0,0,0,0,0,0,0);
        }
    }
}
