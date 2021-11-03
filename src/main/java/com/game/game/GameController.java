package com.game.game;

import com.google.gson.Gson;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Scanner;

public class GameController {
    private String host = "http://localhost:8080";
    int n = 5; //size of grid = n*n

    Gson gson = new Gson();
    RestTemplate restTemplate = new RestTemplate();
    Scanner sc = new Scanner(System.in);
    GameEntity game;

    /**
     * Main REPL loop for interacting with user
     *
     * @throws InterruptedException wait between request
     */
    void gameReplLoop() throws InterruptedException {
        registerHook();

        System.out.println("Enter your name: ");
        String userName = sc.nextLine();

        getInitialGame(userName);

        String mark = asignMarkToPlayer(game);
        System.out.printf("You are playing mark '%s'\n", mark);

        waitForOtherPlayer(game);

        while (!(game.isWin() || game.isOver())) {
            waitForOurTurn(userName);

            if (!(game.isWin() || game.isOver())) {
                printStateOfGame(game);
                inputNextMove(mark);
                printStateOfGame(game);
            }
        }

        if (game.isWin()) {
            System.out.println("The game has been won by: " + game.getPlayerWin());
        }
        if (game.isOver()) {
            System.out.println("The game is over - the other player disconected.");
        }
    }

    /**
     * We get the initial game
     * if both player are joined the game starts
     * else we wait for the other player
     *
     * @param userName inputed username by the user
     */
    private void getInitialGame(String userName) {
        ResponseEntity<String> response = restTemplate.getForEntity(host + "/game/new?userName=" + userName, String.class);
        game = gson.fromJson(response.getBody(), GameEntity.class);
    }

    /**
     * Prompt the user to input it's next move
     * update the game with move
     *
     * @param mark X/Y the mark with which the user plays
     */
    private void inputNextMove(String mark) {
        System.out.println("Place your next move(1-9): ");
        int nextMoveIndex = sc.nextInt() - 1;
        while (nextMoveIndex < 0 || nextMoveIndex > 8) {
            System.out.println("Your input is not in the range (1-9), please try again: ");
            nextMoveIndex = sc.nextInt() - 1;
        }

        ResponseEntity<String> response = restTemplate.postForEntity(host + "/game/" + game.getId() + "/" + nextMoveIndex + "/" + mark, null, String.class);
        game = gson.fromJson(response.getBody(), GameEntity.class);
    }

    /**
     * Wait for our turn to make a move
     *
     * @param userName inputed username by the user
     * @throws InterruptedException wait between request
     */
    private void waitForOurTurn(String userName) throws InterruptedException {
        if (!game.getTurn().equals(userName)) System.out.println("Waiting for the other player to place mark...");
        while (!game.getTurn().equals(userName)) {
            Thread.sleep(1000);

            ResponseEntity<String> response = restTemplate.getForEntity(host + "/game/" + game.getId(), String.class);
            game = gson.fromJson(response.getBody(), GameEntity.class);

            /* We check if game was won by other player */
            if (game.isWin() || game.isOver()) break;
        }
    }

    /**
     * Asigns the mark with which the player
     * marks it's moves in the game
     *
     * @param game game entity
     * @return X or Y
     */
    private String asignMarkToPlayer(GameEntity game) {
        return game.getPlayer2() == null ? "X" : "Y";
    }

    /**
     * Wait for the other player to join the game
     *
     * @param game game entity
     * @throws InterruptedException wait between request
     */
    private void waitForOtherPlayer(GameEntity game) throws InterruptedException {
        if (game.getPlayer2() == null) System.out.println("Waiting for the second player to join...");
        while (game.getPlayer2() == null) {
            ResponseEntity<String> response = restTemplate.getForEntity(host + "/game/" + game.getId(), String.class);
            game = gson.fromJson(response.getBody(), GameEntity.class);

            Thread.sleep(1000);
        }
    }

    /**
     * Prints the current state of the game table
     *
     * @param game game entity
     */
    private void printStateOfGame(GameEntity game) {
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++)
                System.out.print(game.getState().charAt(i * n + j) + " ");
            System.out.println();
        }
        System.out.println();
    }

    /**
     * Hook in case one of the players disconects
     */
    void registerHook() {
        Thread printingHook = new Thread(() -> {
            game.setOver(true);
            restTemplate.postForEntity(host + "/game/over/" + game.getId(), null, String.class);
        });
        Runtime.getRuntime().addShutdownHook(printingHook);
    }
}
