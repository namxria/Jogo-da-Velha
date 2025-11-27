package br.univille.IA.controller;

import br.univille.IA.service.JogoDaVelhaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class JogoDaVelhaController {

    @Autowired
    private JogoDaVelhaService service;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("board", List.of(0,0,0,0,0,0,0,0,0));
        model.addAttribute("msg", "");
        model.addAttribute("iaRaw", "");
        model.addAttribute("iaJson", "");
        return "jogo-da-velha";
    }

    @PostMapping("/jogar")
    public String jogar(@RequestParam("vetor") String vetor, Model model) {

        List<Integer> humano = service.fromJson(vetor);

        if (humano == null || humano.size() != 9)
            humano = List.of(0,0,0,0,0,0,0,0,0);

        if (ganhou(humano, 1)) {
            model.addAttribute("board", humano);
            model.addAttribute("msg", "Você ganhou! 🎉");

            model.addAttribute("iaRaw", service.getLastGeminiRaw());
            model.addAttribute("iaJson", service.getLastGeminiJson());
            return "jogo-da-velha";
        }

        List<Integer> board = service.jogarJogoVelha(vetor);

        model.addAttribute("iaRaw", service.getLastGeminiRaw());
        model.addAttribute("iaJson", service.getLastGeminiJson());

        if (board == null || board.size() != 9)
            board = List.of(0,0,0,0,0,0,0,0,0);

        if (ganhou(board, 2)) {
            model.addAttribute("board", board);
            model.addAttribute("msg", "A IA ganhou! 🤖");
            return "jogo-da-velha";
        }

        if (!board.contains(0)) {
            model.addAttribute("board", board);
            model.addAttribute("msg", "Deu velha! 🤝");
            return "jogo-da-velha";
        }

        model.addAttribute("board", board);
        model.addAttribute("msg", "");
        return "jogo-da-velha";
    }

    private boolean ganhou(List<Integer> b, int p) {
        int[][] c = {
                {0,1,2}, {3,4,5}, {6,7,8},
                {0,3,6}, {1,4,7}, {2,5,8},
                {0,4,8}, {2,4,6}
        };

        for (int[] r : c) {
            if (b.get(r[0]) == p && b.get(r[1]) == p && b.get(r[2]) == p)
                return true;
        }
        return false;
    }
}
