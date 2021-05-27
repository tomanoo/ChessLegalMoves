package model;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
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
                sb.append(board[i][j].getFenSymbol() + " ");
            }
            sb.append("\n");
        }
//        sb.append("\n");
//        squarePieceMap.forEach((key, value) -> sb.append(key).append(": ").append(value).append("   "));
        return sb.toString();
    }

    public void showPossibleMoves() {
        AtomicBoolean moveKingOnly = new AtomicBoolean(false);
        String kingPos = this.findKingPos(sideToMove);
        List<String> moves = new ArrayList<>();
        List<String> enemiesMoves = this.showPossibleMovesForSide(Side.values()[(sideToMove.ordinal() + 1) % 2]);

        // find any piece trying to attack king
        enemiesMoves.forEach(string -> {
            if (string.contains(kingPos)) {
                moveKingOnly.set(true);
            }
        });

        if (moveKingOnly.get()) {
            moves = this.showPossibleKingMoves(sideToMove);
        } else {
            moves = this.showPossibleMovesForSide(sideToMove);
        }

        moves.addAll(checkCastling());

        System.out.print("\nMove: " + sideToMove + "\nMoves:");

        Collections.sort(moves);
//        System.out.println("Moves: " + moves);

        for (int i = 0; i < moves.size(); i++) {
            if (i % 10 == 0) {
                System.out.println("");
            }
            System.out.print(moves.get(i) + ", ");
        }
//        moves.forEach(string -> {
//            System.out.println(string + ", ");
//        });
    }

    private List<String> showPossibleKingMoves(Side side) {
        List<String> list = new ArrayList<>();

        squarePieceMap.forEach((key, value) -> {
            if (side.equals(value.getPieceSide())) {
                if (value.getPieceType().equals(PieceType.KING)) {
                    list.addAll(pawnMoves(key));
                }
            }
        });

        return list;
    }

    private String findKingPos(Side side) {
        AtomicReference<String> kingPos = new AtomicReference<>("");
        squarePieceMap.forEach((key, value) -> {
            if (side.equals(value.getPieceSide())) {
                if (value.getPieceType().equals(PieceType.KING)) {
                    kingPos.set(Square.values()[key.ordinal()].value());
                }
            }
        });

        return kingPos.get();
    }

    private List<String> showPossibleMovesForSide(Side side) {
        List<String> list = new ArrayList<>();

        squarePieceMap.forEach((key, value) -> {
            if (side.equals(value.getPieceSide())) {
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

        return list;
    }

    private List<String> pawnMoves(Square square) {
        List<String> list = new ArrayList<>();
        Side side = squarePieceMap.get(square).getPieceSide();
        int sideModificator = side == Side.WHITE ? 1 : -1;

        // can move forward
        int movePos = square.ordinal() + 8 * sideModificator;
        if (movePos >= 0 && movePos < 64 && squarePieceMap.get(Square.values()[movePos]).getPieceType() == null) {
            list.add(square.value() + "->" + Square.values()[movePos].value());

            // is his first move
            int secLine = side == Side.WHITE ? 1 : 6; // 1 = 2 line && 6 = 7 line
            movePos = square.ordinal() + 16 * sideModificator;
            if (square.getRank().ordinal() == secLine && squarePieceMap.get(Square.values()[movePos]).getPieceType() == null) {
                list.add(square.value() + "->" + Square.values()[movePos].value());
            }
        }

        // can hit something
        int leftAttackPos = square.ordinal() + 7 * sideModificator;
        if (leftAttackPos >= 0 && leftAttackPos < 64 &&
                (isPieceSideSameAs(Square.values()[leftAttackPos], (side.ordinal() + 1) % 2) || // attack move
                        enPassant.ordinal() == Square.values()[leftAttackPos].ordinal())) { //enPassant
            list.add(square.value() + "->" + Square.values()[leftAttackPos].value());
        }

        int rightAttackPos = square.ordinal() + 9 * sideModificator;
        if (rightAttackPos >= 0 && rightAttackPos < 64 &&
                (isPieceSideSameAs(Square.values()[rightAttackPos], (side.ordinal() + 1) % 2) ||
                        enPassant.ordinal() == Square.values()[rightAttackPos].ordinal())) {
            list.add(square.value() + "->" + Square.values()[rightAttackPos].value());
        }

        return list;
    }

    private List<String> kingMoves(Square square) {
        List<String> list = new ArrayList<>();

        list.addAll(findPossibleMovesInDirection(square, 0, 1, 1));
        list.addAll(findPossibleMovesInDirection(square, 0, -1, 1));
        list.addAll(findPossibleMovesInDirection(square, 1, 0, 1));
        list.addAll(findPossibleMovesInDirection(square, -1, 0, 1));
        list.addAll(findPossibleMovesInDirection(square, 1, 1, 1));
        list.addAll(findPossibleMovesInDirection(square, 1, -1, 1));
        list.addAll(findPossibleMovesInDirection(square, -1, 1, 1));
        list.addAll(findPossibleMovesInDirection(square, -1, -1, 1));

        return list;
    }

    private List<String> knightMove(Square square) {
        List<String> list = new ArrayList<>();

        list.addAll(findPossibleMovesInDirection(square, 2, 1, 1));
        list.addAll(findPossibleMovesInDirection(square, 2, -1, 1));
        list.addAll(findPossibleMovesInDirection(square, -2, 1, 1));
        list.addAll(findPossibleMovesInDirection(square, -2, -1, 1));
        list.addAll(findPossibleMovesInDirection(square, 1, 2, 1));
        list.addAll(findPossibleMovesInDirection(square, 1, -2, 1));
        list.addAll(findPossibleMovesInDirection(square, -1, 2, 1));
        list.addAll(findPossibleMovesInDirection(square, -1, -2, 1));

        return list;
    }

    private List<String> bishopMove(Square square) {
        List<String> list = new ArrayList<>();

        list.addAll(findPossibleMovesInDirection(square, 1, 1, 8));
        list.addAll(findPossibleMovesInDirection(square, 1, -1, 8));
        list.addAll(findPossibleMovesInDirection(square, -1, 1, 8));
        list.addAll(findPossibleMovesInDirection(square, -1, -1, 8));

        return list;
    }

    private List<String> rookMoves(Square square) {
        List<String> list = new ArrayList<>();

        list.addAll(findPossibleMovesInDirection(square, 0, 1, 8));
        list.addAll(findPossibleMovesInDirection(square, 0, -1, 8));
        list.addAll(findPossibleMovesInDirection(square, 1, 0, 8));
        list.addAll(findPossibleMovesInDirection(square, -1, 0, 8));

        return list;
    }

    private List<String> findPossibleMovesInDirection(Square square, int directionFile, int directionRank, int iterations) {
        Side side = squarePieceMap.get(square).getPieceSide();
        List<String> list = new ArrayList<>();
        File curFile = square.getFile();
        Rank curRank = square.getRank();

        for (int i = 0; i < iterations; i++) {
            int nextFileIndex = curFile.ordinal() + directionFile;
            int nextRankIndex = curRank.ordinal() + directionRank;

            // check that we are still one the map :)
            if (nextRankIndex < 0 || nextRankIndex > 7) {
                break;
            } else if (nextFileIndex < 0 || nextFileIndex > 7) {
                break;
            }

            // check if can attack or move piece
            Square newSquare = Square.encode(Rank.values()[nextRankIndex], File.values()[nextFileIndex]);


            // can move here or attack this shit
            if (squarePieceMap.get(newSquare).getPieceType() == null) { // empty field
                list.add(square.value() + "->" + newSquare.value());
            } else if (isPieceSideSameAs(newSquare, side.ordinal())) { // same piece type
                break;
            } else if (isPieceSideSameAs(newSquare, (side.ordinal() + 1) % 2)) { // enemies :)
                list.add(square.value() + "->" + newSquare.value());
                break;
            }

            // set new curFile and curRank
            curFile = newSquare.getFile();
            curRank = newSquare.getRank();
        }

        return list;
    }

    private boolean isPieceSideSameAs(Square square, int sideOrdinal) {
        Side pieceSide = squarePieceMap.get(square).getPieceSide();
        if (pieceSide == null) {
            return false;
        } else if (pieceSide.ordinal() == sideOrdinal) {
            return true;
        }
        return false;
    }

    public List<String> checkCastling() {
        List<String> castlingMovesList = new ArrayList<>();
        CastleRight castling = castleRight.get(sideToMove);
        if (Piece.WHITE_ROOK.equals(squarePieceMap.get(Square.A1)) && Piece.WHITE_KING.equals(squarePieceMap.get(Square.E1))
                && Piece.NONE.equals(squarePieceMap.get(Square.B1)) && Piece.NONE.equals(squarePieceMap.get(Square.C1)) && Piece.NONE.equals(squarePieceMap.get(Square.D1))
                && (CastleRight.KING_AND_QUEEN_SIDE.equals(castling) || CastleRight.QUEEN_SIDE.equals(castling)) && Side.WHITE.equals(sideToMove)) {
            castlingMovesList.add("Castling: O-O-O");
        }
        if (Piece.WHITE_ROOK.equals(squarePieceMap.get(Square.H1)) && Piece.WHITE_KING.equals(squarePieceMap.get(Square.E1))
                && Piece.NONE.equals(squarePieceMap.get(Square.F1)) && Piece.NONE.equals(squarePieceMap.get(Square.G1))
                && (CastleRight.KING_AND_QUEEN_SIDE.equals(castling) || CastleRight.KING_SIDE.equals(castling)) && Side.WHITE.equals(sideToMove)) {
            castlingMovesList.add("Castling: O-O");
        }
        if (Piece.BLACK_ROOK.equals(squarePieceMap.get(Square.A8)) && Piece.BLACK_KING.equals(squarePieceMap.get(Square.E8))
                && Piece.NONE.equals(squarePieceMap.get(Square.B8)) && Piece.NONE.equals(squarePieceMap.get(Square.C8)) && Piece.NONE.equals(squarePieceMap.get(Square.D8))
                && (CastleRight.KING_AND_QUEEN_SIDE.equals(castling) || CastleRight.QUEEN_SIDE.equals(castling)) && Side.BLACK.equals(sideToMove)) {
            castlingMovesList.add("Castling: O-O-O");
        }
        if (Piece.BLACK_ROOK.equals(squarePieceMap.get(Square.H8)) && Piece.BLACK_KING.equals(squarePieceMap.get(Square.E8))
                && Piece.NONE.equals(squarePieceMap.get(Square.F8)) && Piece.NONE.equals(squarePieceMap.get(Square.G8))
                && (CastleRight.KING_AND_QUEEN_SIDE.equals(castling) || CastleRight.KING_SIDE.equals(castling)) && Side.BLACK.equals(sideToMove)) {
            castlingMovesList.add("Castling: O-O");
        }
        return castlingMovesList;
    }

}
