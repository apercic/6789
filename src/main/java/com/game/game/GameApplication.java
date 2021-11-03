package com.game.game;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class GameApplication {

    public static void main(String[] args) throws InterruptedException, IOException {
        GameController gameController = new GameController();
        gameController.gameReplLoop();

    }
}
