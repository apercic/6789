package com.game.game;

import lombok.Data;

@Data
class GameEntity {

    private Long id;
    private String state;
    private String turn;

    private boolean win;
    private String playerWin;
    private boolean over;

    private String player1;
    private String player2;

}