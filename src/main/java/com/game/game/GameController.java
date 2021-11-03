package com.game.game;

import com.google.gson.Gson;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;

public class GameController {
    Resource resource = new ClassPathResource("/application.properties");
    Properties props = PropertiesLoaderUtils.loadProperties(resource);

    private final String host = props.getProperty("base.url");
    private final int columns = Integer.parseInt(props.getProperty("grid.size.columns"));
    private final int rows = Integer.parseInt(props.getProperty("grid.size.rows"));

    Gson gson = new Gson();
    RestTemplate restTemplate = new RestTemplate();
    Scanner sc = new Scanner(System.in);
    GameEntity game;

    public GameController() throws IOException {
    }

    /**
     * Main REPL loop for interacting with user
     *
     * @throws InterruptedException wait between request
     */
    void gameReplLoop() throws InterruptedException {
        System.out.println("Enter your name: ");
        String userName = sc.nextLine() + new Random().nextLong();
        System.out.println("Your game id is: " + userName);

        getInitialGame(userName);
        registerHook();

        String mark = asignMarkToPlayer(game);
        System.out.printf("You are playing mark '%s'\n", mark);

        waitForOtherPlayer(game);

        while (!(game.isWin() || game.isOver())) {
            waitForOurTurn(userName);

            if (!(game.isWin() || game.isOver())) {
                printStateOfGame(game);
                boolean madeMove = inputNextMove(mark);
                if (!madeMove)
                    System.out.println("Could not place mark in selected column, please select another column: ");
                else printStateOfGame(game);
            }
        }

        if (game.isWin()) {
            System.out.println("The game has been won by: " + game.getPlayerWin());
        } else if (game.isOver()) {
            System.out.println("The game is over - the other player disconnected.");
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
        ResponseEntity<String> response = restTemplate.getForEntity(String.format("%s/game/new?userName=%s", host, userName), String.class);
        game = gson.fromJson(response.getBody(), GameEntity.class);
    }

    /**
     * Prompt the user to input it's next move
     * update the game with move
     *
     * @param mark X/Y the mark with which the user plays
     * @return true if we could place mark/false if not
     */
    private boolean inputNextMove(String mark) {
        System.out.println("Place your next move(1-9): ");

        int nextMoveIndex = sc.nextInt() - 1;
        while (nextMoveIndex < 0 || nextMoveIndex > 8) {
            System.out.println("Your input is not in the range (1-9), please try again: ");
            nextMoveIndex = sc.nextInt() - 1;
        }

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(String.format("%s/game/%d/%d/%s", host, game.getId(), nextMoveIndex, mark), null, String.class);
            game = gson.fromJson(response.getBody(), GameEntity.class);
        } catch (RuntimeException e) {
            return false;
        }
        return true;
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
            Thread.sleep(300);

            ResponseEntity<String> response = restTemplate.getForEntity(String.format("%s/game/%d", host, game.getId()), String.class);
            game = gson.fromJson(response.getBody(), GameEntity.class);

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
    public String asignMarkToPlayer(GameEntity game) {
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
            ResponseEntity<String> response = restTemplate.getForEntity(String.format("%s/game/%d", host, game.getId()), String.class);
            game = gson.fromJson(response.getBody(), GameEntity.class);

            Thread.sleep(300);
        }
    }

    /**
     * Prints the current state of the game table
     *
     * @param game game entity
     */
    private void printStateOfGame(GameEntity game) {
        if (game.isWin() || game.isOver()) return;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++)
                System.out.print(game.getState().charAt(i * columns + j) + " ");
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
            restTemplate.postForEntity(String.format("%s/game/%d/over", host, game.getId()), null, String.class);
        });
        Runtime.getRuntime().addShutdownHook(printingHook);
    }
}
