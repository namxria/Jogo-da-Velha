package br.univille.IA.service;

import br.univille.IA.config.GeminiAPI;
import br.univille.IA.dto.Velha;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

    @Service
    public class JogoDaVelhaService {

        private final RestTemplateBuilder restTemplateBuilder;
        private final GeminiAPI geminiAPI;

        public JogoDaVelhaService(RestTemplateBuilder restTemplateBuilder,  GeminiAPI geminiAPI) {
            this.restTemplateBuilder = restTemplateBuilder;
            this.geminiAPI = geminiAPI;
        }

        public List<Velha> jogarJogoVelha(String jogada){
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
                      "text": "Vou te passar o tabuleiro do jogo da velha em formato de vetor. 0 representa campo vazio, 1 representa o jogador 1 e 2 representa você, quero que você jogue e me retorne APENAS o vetor em formato json, sem vídeos, responda somente com o vetor dessa forma, exemplo: "[1,0,1,0,0,2,0,0,2]". minha jogada: :JOGADA"
                    }
                  ]
                }
              ]
            }
            """;
                body = body.replace(":JOGADA", jogada);
                HttpEntity<String> request = new HttpEntity<>(body, headers);


                ResponseEntity<String> response = rest.exchange(
                        "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent",
                        HttpMethod.POST,
                        request,
                        String.class
                );
                String json = response.getBody();

                ObjectMapper mapper = new ObjectMapper();
                String text = mapper.readTree(json)                 // raiz
                        .path("candidates").get(0)                  // primeiro candidate
                        .path("content").path("parts").get(0)       // primeiro part
                        .path("text")                               // pega text
                        .asText();                                  // converte para String

                System.out.println(text);
                String cleanJson = text
                        .replace("```json", "")
                        .replace("```", "")
                        .trim();
                System.out.println(cleanJson);
                mapper = new ObjectMapper();

                return mapper.readValue(
                        cleanJson,
                        new TypeReference<List<Velha>>() {}
                );
            }catch (Exception e){
                e.printStackTrace();
            }
            return new ArrayList<>();
        }

    }
