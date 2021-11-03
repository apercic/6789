package com.game.game;

import com.google.gson.Gson;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Scanner;

public class GameController {
    private String host = "http://localhost:8080";
    int n = 5; //size of grid = n*n

    void lala() throws InterruptedException {
        Gson gson = new Gson();
        RestTemplate restTemplate = new RestTemplate();

        Scanner myObj = new Scanner(System.in);
        System.out.println("Enter your name: ");
        String userName = myObj.nextLine();

        String url = host + "/game/new?userName=" + userName;
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        GameEntity game = gson.fromJson(response.getBody(), GameEntity.class);

        //which mark the player has
        String mark = "";
        if (game.getPlayer2() == null) mark = "X";
        else mark = "Y";

        //wait for the other player
        while (game.getPlayer2() == null) {
            response = restTemplate.getForEntity(host + "/game/" + game.getId(), String.class);
            game = gson.fromJson(response.getBody(), GameEntity.class);

            Thread.sleep(1000);
        }

        while (!(game.isWin() || game.isOver())) {
            //wait for our turn
            while (!game.getTurn().equals(userName)) {
                Thread.sleep(1000);

                response = restTemplate.getForEntity(host + "/game/" + game.getId(), String.class);
                game = gson.fromJson(response.getBody(), GameEntity.class);
            }

            //print out state of game
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++)
                    System.out.print(game.getState().charAt(i * n + j) + " ");
                System.out.println();
            }

            System.out.println("Which is your next move(1-9): ");
            int nextMoveIndex = myObj.nextInt()-1;

            response = restTemplate.postForEntity(host + "/game/" + game.getId() + "/" + nextMoveIndex + "/" + mark,
                    null, String.class);
            game = gson.fromJson(response.getBody(), GameEntity.class);
        }


    }
}
