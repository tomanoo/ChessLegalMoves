package model;

public enum PieceType {

    PAWN(""),
    KNIGHT("N"),
    BISHOP("B"),
    ROOK("R"),
    QUEEN("Q"),
    KING("K"),
    NONE("NONE");

    private String symbol;

    PieceType(String symbol) {
        this.symbol = symbol;
    }

    public String value() {
        return name();
    }
}
