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
            board[7 - rank][file] = piece;
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
        List<String> list = new ArrayList<>();

        squarePieceMap.forEach((key, value) -> {
            if (this.sideToMove.equals(value.getPieceSide())) {
                if (value.getPieceType().equals(PieceType.PAWN)) {
                    list.addAll(pawnMoves(key));
                } else if (value.getPieceType().equals(PieceType.KNIGHT)) {
                    list.addAll(knightMove(key));
                } else if (value.getPieceType().equals(PieceType.BISHOP)) {
                    list.addAll(bishopMove(key));
                } else if (value.getPieceType().equals(PieceType.ROOK)) {
                    list.addAll(rookMoves(key));
                } else if (value.getPieceType().equals(PieceType.QUEEN)) {
                    list.addAll(rookMoves(key));
                    list.addAll(bishopMove(key));
                } else { // king
                    list.addAll(kingMoves(key));
                }
            }
        });

        list.forEach(string -> {
            System.out.println(string + ", ");
        });
    }

    private List<String> pawnMoves(Square square) {
        List<String> list = new ArrayList<>();
        int sideModificator = sideToMove == Side.WHITE ? 1 : -1;

        // can move forward
        int movePos = square.ordinal() + 8 * sideModificator;
        if (movePos >= 0 && movePos < 64 && squarePieceMap.get(Square.values()[movePos]).getPieceType() == null) {
            list.add(square.value() + "->" + Square.values()[movePos].value());
        }

        // can hit something
        int leftAttackPos = square.ordinal() + 7 * sideModificator;
        if (leftAttackPos >= 0 && leftAttackPos < 64 &&
                (isPieceSideSameAs(leftAttackPos, (sideToMove.ordinal() + 1) % 2) || // attack move
                        enPassant.ordinal() == Square.values()[leftAttackPos].ordinal())) { //enPassant
            list.add(square.value() + "->" + Square.values()[leftAttackPos].value());
        }

        int rightAttackPos = square.ordinal() + 9 * sideModificator;
        if (rightAttackPos >= 0 && rightAttackPos < 64 &&
                (isPieceSideSameAs(rightAttackPos, (sideToMove.ordinal() + 1) % 2) ||
                        enPassant.ordinal() == Square.values()[rightAttackPos].ordinal())) {
            list.add(square.value() + "->" + Square.values()[rightAttackPos].value());
        }

        return list;
    }

    private List<String> kingMoves(Square square) {
        List<String> list = new ArrayList<>();

        list.addAll(findPossibleMovesInDirection(square, 1, 1));
        list.addAll(findPossibleMovesInDirection(square, -1, 1));
        list.addAll(findPossibleMovesInDirection(square, 7, 1));
        list.addAll(findPossibleMovesInDirection(square, -7, 1));
        list.addAll(findPossibleMovesInDirection(square, 8, 1));
        list.addAll(findPossibleMovesInDirection(square, -8, 1));
        list.addAll(findPossibleMovesInDirection(square, 9, 1));
        list.addAll(findPossibleMovesInDirection(square, -9, 1));

        return list;
    }

    private List<String> knightMove(Square square) {
        List<String> list = new ArrayList<>();

        list.addAll(findPossibleMovesInDirection(square, 6, 1));
        list.addAll(findPossibleMovesInDirection(square, -6, 1));
        list.addAll(findPossibleMovesInDirection(square, 10, 1));
        list.addAll(findPossibleMovesInDirection(square, -10, 1));
        list.addAll(findPossibleMovesInDirection(square, 15, 1));
        list.addAll(findPossibleMovesInDirection(square, -15, 1));
        list.addAll(findPossibleMovesInDirection(square, 17, 1));
        list.addAll(findPossibleMovesInDirection(square, -17, 1));

        return list;
    }

    private List<String> bishopMove(Square square) {
        List<String> list = new ArrayList<>();

        list.addAll(findPossibleMovesInDirection(square, 9, 8));
        list.addAll(findPossibleMovesInDirection(square, -9, 8));
        list.addAll(findPossibleMovesInDirection(square, 7, 8));
        list.addAll(findPossibleMovesInDirection(square, -7, 8));

        return list;
    }

    private List<String> rookMoves(Square square) {
        List<String> list = new ArrayList<>();

        list.addAll(findPossibleMovesInDirection(square, 8, 8));
        list.addAll(findPossibleMovesInDirection(square, -8, 8));
        list.addAll(findPossibleMovesInDirection(square, 1, 8));
        list.addAll(findPossibleMovesInDirection(square, -1, 8));

        return list;
    }

    private List<String> findPossibleMovesInDirection(Square square, int direction, int iterations) {
        List<String> list = new ArrayList<>();
        int nextDirection = direction;

        for (int i = 0; i < iterations; i++) {
            int nextPos = square.ordinal() + nextDirection;
            // is even really possible bro? xD
            if (nextPos < 0 || nextPos >= 64) {
                break;
            }

            // can move here or attack this shit
            if (squarePieceMap.get(Square.values()[nextPos]).getPieceType() == null) { // empty field
                list.add(square.value() + "->" + Square.values()[nextPos].value());
            } else if (isPieceSideSameAs(nextPos, sideToMove.ordinal())) { // same piece type
                break;
            } else if (isPieceSideSameAs(nextPos, (sideToMove.ordinal() + 1) % 2)) { // enemies :)
                list.add(square.value() + "->" + Square.values()[nextPos].value());
                break;
            }

            nextDirection = nextDirection + direction;
        }

        return list;
    }


    private boolean isPieceSideSameAs(int piecePos, int sideOrdinal) {
        Side pieceSide = squarePieceMap.get(Square.values()[piecePos]).getPieceSide();
        if (pieceSide == null) {
            return false;
        } else if (pieceSide.ordinal() == sideOrdinal) {
            return true;
        }
        return false;
    }

}
