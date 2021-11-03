package com.game.game;

import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GameApplication {

    public static void main(String[] args) throws InterruptedException {
        GameController gameController = new GameController();
        gameController.lala();

    }
}
