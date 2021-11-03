package com.game.game;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class GameApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void testAsignMarkXToPlayer() throws IOException {
        GameController gameController = new GameController();
        GameEntity game = new GameEntity();
        game.setPlayer2(null);
        String mark = gameController.asignMarkToPlayer(game);
        assertEquals(mark, "X");
    }

    @Test
    void testAsignMarkYToPlayer() throws IOException {
        GameController gameController = new GameController();
        GameEntity game = new GameEntity();
        game.setPlayer2("player2");
        String mark = gameController.asignMarkToPlayer(game);
        assertEquals(mark, "Y");
    }

}
