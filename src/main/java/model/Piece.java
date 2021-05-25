package model;

import java.util.HashMap;
import java.util.Map;

public enum Piece {

    WHITE_PAWN(Side.WHITE, PieceType.PAWN, "P"),
    WHITE_KNIGHT(Side.WHITE, PieceType.KNIGHT, "N"),
    WHITE_BISHOP(Side.WHITE, PieceType.BISHOP, "B"),
    WHITE_ROOK(Side.WHITE, PieceType.ROOK, "R"),
    WHITE_QUEEN(Side.WHITE, PieceType.QUEEN, "Q"),
    WHITE_KING(Side.WHITE, PieceType.KING, "K"),
    BLACK_PAWN(Side.BLACK, PieceType.PAWN, "p"),
    BLACK_KNIGHT(Side.BLACK, PieceType.KNIGHT, "n"),
    BLACK_BISHOP(Side.BLACK, PieceType.BISHOP, "b"),
    BLACK_ROOK(Side.BLACK, PieceType.ROOK, "r"),
    BLACK_QUEEN(Side.BLACK, PieceType.QUEEN, "q"),
    BLACK_KING(Side.BLACK, PieceType.KING, "k"),
    NONE(null, null, ".");


    private static final Map<String, Piece> fenToPiece = new HashMap<>(13);

    static {
        for (final Piece piece : Piece.values()) {
            fenToPiece.put(piece.getFenSymbol(), piece);
        }
    }

    private final Side side;
    private final PieceType type;
    private String fenSymbol;

    Piece(Side side, PieceType type, String fenSymbol) {
        this.side = side;
        this.type = type;
        this.fenSymbol = fenSymbol;
    }

    public String value() {
        return name();
    }

    public PieceType getPieceType() {
        return type;
    }

    public Side getPieceSide() {
        return side;
    }

    public String getFenSymbol() {
        return fenSymbol;
    }

    public static Piece fromFenSymbol(String fenSymbol) {
        final Piece piece = fenToPiece.get(fenSymbol);
        if (piece == null) {
            throw new IllegalArgumentException(String.format("Unknown piece '%s'", fenSymbol));
        }
        return piece;
    }

}
