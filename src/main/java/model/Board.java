package model;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class Board {

    private final EnumMap<Side, CastleRight> castleRight;
    private Side sideToMove;
    private Integer moveCounter;
    private Integer halfMoveCounter;
    private final Piece[] occupation;
    private static Square enPassant;

    private static Piece[][] board = {{Piece.NONE, Piece.NONE, Piece.NONE, Piece.NONE, Piece.NONE, Piece.NONE, Piece.NONE, Piece.NONE},
            {Piece.NONE, Piece.NONE, Piece.NONE, Piece.NONE, Piece.NONE, Piece.NONE, Piece.NONE, Piece.NONE},
            {Piece.NONE, Piece.NONE, Piece.NONE, Piece.NONE, Piece.NONE, Piece.NONE, Piece.NONE, Piece.NONE},
            {Piece.NONE, Piece.NONE, Piece.NONE, Piece.NONE, Piece.NONE, Piece.NONE, Piece.NONE, Piece.NONE},
            {Piece.NONE, Piece.NONE, Piece.NONE, Piece.NONE, Piece.NONE, Piece.NONE, Piece.NONE, Piece.NONE},
            {Piece.NONE, Piece.NONE, Piece.NONE, Piece.NONE, Piece.NONE, Piece.NONE, Piece.NONE, Piece.NONE},
            {Piece.NONE, Piece.NONE, Piece.NONE, Piece.NONE, Piece.NONE, Piece.NONE, Piece.NONE, Piece.NONE},
            {Piece.NONE, Piece.NONE, Piece.NONE, Piece.NONE, Piece.NONE, Piece.NONE, Piece.NONE, Piece.NONE}};

    private static Map<Square, Piece> squarePieceMap = new HashMap<>();

    public Board() {
        castleRight = new EnumMap<>(Side.class);
        setSideToMove(Side.WHITE);
        setMoveCounter(1);
        setHalfMoveCounter(0);
        occupation = new Piece[Square.values().length];
        Arrays.fill(occupation, Piece.NONE);
        fillPieceSquareMap();
        enPassant = Square.NONE;
    }

    public Side getSideToMove() {
        return sideToMove;
    }

    public void setSideToMove(Side sideToMove) {
        this.sideToMove = sideToMove;
    }

    public Integer getMoveCounter() {
        return moveCounter;
    }

    public void setMoveCounter(Integer moveCounter) {
        this.moveCounter = moveCounter;
    }

    public Integer getHalfMoveCounter() {
        return halfMoveCounter;
    }

    public void setHalfMoveCounter(Integer halfMoveCounter) {
        this.halfMoveCounter = halfMoveCounter;
    }

    public EnumMap<Side, CastleRight> getCastleRight() {
        return castleRight;
    }

    public Piece getPiece(Square sq) {
        return occupation[sq.ordinal()];
    }

    public void setPiece(Piece piece, Square square, int file, int rank) {
        if (!Piece.NONE.equals(piece) && !Square.NONE.equals(square)) {
            board[7-rank][file] = piece;
        }
    }

    public Piece[][] getBoard() {
        return board;
    }

    public Square getEnPassant() {
        return enPassant;
    }

    public void setEnPassant(Square square) {
        enPassant = square;
    }

    public void fillPieceSquareMap() {
        for (Square square : Square.values()) {
            squarePieceMap.put(square, Piece.NONE);
        }
    }

    // e.g.: rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1
    public void loadFromFen(String fen) {
        String squares = fen.substring(0, fen.indexOf(' '));
        String state = fen.substring(fen.indexOf(' ') + 1);

        String[] ranks = squares.split("/");
        int file;
        int rank = 7;
        for (String r : ranks) {
            file = 0;
            for (int i = 0; i < r.length(); i++) {
                char c = r.charAt(i);
                if (Character.isDigit(c)) {
                    file += Integer.parseInt(c + "");
                } else {
                    Square square = Square.encode(Rank.allRanks[rank], File.allFiles[file]);
                    Piece piece = Piece.fromFenSymbol(String.valueOf(c));
                    setPiece(piece, square, file, rank);
                    squarePieceMap.put(square, piece);
                    file++;
                }
            }
            rank--;
        }

        sideToMove = state.toLowerCase().charAt(0) == 'w' ? Side.WHITE : Side.BLACK;

        if (state.contains("KQ")) {
            castleRight.put(Side.WHITE, CastleRight.KING_AND_QUEEN_SIDE);
        } else if (state.contains("K")) {
            castleRight.put(Side.WHITE, CastleRight.KING_SIDE);
        } else if (state.contains("Q")) {
            castleRight.put(Side.WHITE, CastleRight.QUEEN_SIDE);
        } else {
            castleRight.put(Side.WHITE, CastleRight.NONE);
        }

        if (state.contains("kq")) {
            castleRight.put(Side.BLACK, CastleRight.KING_AND_QUEEN_SIDE);
        } else if (state.contains("k")) {
            castleRight.put(Side.BLACK, CastleRight.KING_SIDE);
        } else if (state.contains("q")) {
            castleRight.put(Side.BLACK, CastleRight.QUEEN_SIDE);
        } else {
            castleRight.put(Side.BLACK, CastleRight.NONE);
        }

        String[] flags = state.split(" ");

        if (flags.length >= 3) {
            String s = flags[2].toUpperCase().trim();
            if (!s.equals("-")) {
                Square ep = Square.valueOf(s);
                setEnPassant(ep);
                List<String> enPassantMoves = setEnPassantMoves(enPassant, sideToMove);
//                setEnPassantTarget(findEnPassantTarget(ep, sideToMove));
//                if (!(squareAttackedByPieceType(getEnPassant(), getSideToMove(), PieceType.PAWN) != 0 &&
//                        verifyNotPinnedPiece(getSideToMove().flip(), getEnPassant(), getEnPassantTarget()))) {
//                    setEnPassantTarget(Square.NONE);
//                }
            } else {
                setEnPassant(Square.NONE);
//                setEnPassantTarget(Square.NONE);
            }
            if (flags.length >= 4) {
                halfMoveCounter = Integer.parseInt(flags[3]);
                if (flags.length >= 5) {
                    moveCounter = Integer.parseInt(flags[4]);
                }
            }
        }
    }

    public List<String> setEnPassantMoves(Square enPassantSquare, Side side) {
        List<String> enPassantList = new ArrayList<>();
        int currentOrd = Side.WHITE.equals(side) ? enPassantSquare.ordinal() - 8 : enPassantSquare.ordinal() + 8;
        Rank currentRank = Square.values()[currentOrd].getRank();
        Piece currentPiece = squarePieceMap.get(Square.values()[currentOrd]);
        Square leftSquare = Square.values()[currentOrd - 1];
        Piece leftPiece = squarePieceMap.get(Square.values()[currentOrd - 1]);
        Square rightSquare = Square.values()[currentOrd + 1];
        Piece rightPiece = squarePieceMap.get(Square.values()[currentOrd + 1]);
        if (currentRank.equals(leftSquare.getRank()) && PieceType.PAWN.equals(leftPiece.getPieceType()) && !currentPiece.getPieceSide().equals(leftPiece.getPieceSide())) {
            enPassantList.add(leftSquare.value() + "->" + enPassantSquare.value());
        }
        if (currentRank.equals(rightSquare.getRank()) && PieceType.PAWN.equals(rightPiece.getPieceType()) && !currentPiece.getPieceSide().equals(rightPiece.getPieceSide())) {
            enPassantList.add(rightSquare.value() + "->" + enPassantSquare.value());
        }

        System.out.println("en passant moves:" + enPassantList);

        return enPassantList;
    }

//    private void assignPiecesToSquares(Piece piece, Square square) {
//        squarePieceMap.put(square, piece);
//    }

    @Override
    public String toString() {
        return boardToString();
    }

    public String boardToString() {
        StringBuilder sb = new StringBuilder();

        final Supplier<IntStream> rankIterator = Board::sevenToZero;
        final Supplier<IntStream> fileIterator = Board::zeroToSeven;

        rankIterator.get().forEach(i -> {
            Rank r = Rank.allRanks[i];
            fileIterator.get().forEach(n -> {
                File f = File.allFiles[n];
                if (!File.NONE.equals(f) && !Rank.NONE.equals(r)) {
                    Square sq = Square.encode(r, f);
                    Piece piece = getPiece(sq);
                    sb.append(piece.getFenSymbol());
                }
            });
            sb.append("\n");
        });

        return sb.toString();
    }

    private static IntStream zeroToSeven() {
        return IntStream.iterate(0, i -> i + 1).limit(8);
    }

    private static IntStream sevenToZero() {
        return IntStream.iterate(7, i -> i - 1).limit(8);
    }

    public String getBoardToString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                sb.append(board[i][j].getFenSymbol());
            }
            sb.append("\n");
        }
        sb.append("\n");
        squarePieceMap.forEach((key, value) -> sb.append(key).append(": ").append(value).append("   "));
        return sb.toString();
    }

    public void showPossibleMoves() {
        squarePieceMap.forEach((key, value) -> {
            if (this.sideToMove.equals(value.getPieceSide())) {
                if (value.getPieceType().equals(PieceType.PAWN)) {

                } else if (value.getPieceType().equals(PieceType.KNIGHT)) {

                } else if (value.getPieceType().equals(PieceType.BISHOP)) {

                } else if (value.getPieceType().equals(PieceType.ROOK)) {

                } else if (value.getPieceType().equals(PieceType.QUEEN)) {

                } else { // king

                }
            }
        });
    }

    public List<String> pawnMoves(Square squere) {
        int sideModyficator = sideToMove == Side.WHITE ? 1 : -1;

        // can move forward

        // can hit something

        //enPassant

        return new ArrayList<String>();
    }

}
