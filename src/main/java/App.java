import model.Board;

public class App {

    public static void main(String[] args) {
        Board board = new Board();
//        String fen = "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1";
        String fen = "rnbqkbnr/ppp1pppp/8/2PpP3/8/2N4Q/PP1P1PPP/R1B1KBNR w KQkq d6 0 1";
        board.loadFromFen(fen);
        System.out.println("BOARD:\n" + board.getBoardToString());

        board.showPossibleMoves();
    }

}
