package exceptions;

public class GameCantPlaceMarkException extends RuntimeException {

    public GameCantPlaceMarkException(Integer column) {
        super("Could place mark in column " + column);
    }
}
