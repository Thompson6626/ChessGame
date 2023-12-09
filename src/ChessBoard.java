
import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class ChessBoard {

    private static final int GAME_ROWS= ChessGame.BOARD_ROWS;
    private static final int GAME_COLUMNS= ChessGame.BOARD_COLUMNS;
    private static final int BLOCK_WIDTH=80;
    private static final int BLOCK_HEIGHT=80;
    protected static final Color WHITE_SQUARES= new Color(225, 216, 216);
    protected static final Color DARK_SQUARES= new Color(21, 144, 28);

    static final String[][] startingPosition = {
            {"BR", "BN", "BB", "BQ", "BK", "BB", "BN", "BR"},
            {"BP", "BP", "BP", "BP", "BP", "BP", "BP", "BP"},
            {".", ".", ".", ".", ".", ".", ".", "."},
            {".", ".", ".", ".", ".", ".", ".", "."},
            {".", ".", ".", ".", ".", ".", ".", "."},
            {".", ".", ".", ".", ".", ".", ".", "."},
            {"WP", "WP", "WP", "WP", "WP", "WP", "WP", "WP"},
            {"WR", "WN", "WB", "WQ", "WK", "WB", "WN", "WR"}
    };

    private static final String[] pieceImages={
        "../ChessGamee/PieceImages/BlackRook.png",
        "../ChessGamee/PieceImages/BlackKnight.png",
        "../ChessGamee/PieceImages/BlackBishop.png",
        "../ChessGamee/PieceImages/BlackQueen.png",
        "../ChessGamee/PieceImages/BlackKing.png",
        "../ChessGamee/PieceImages/BlackPawn.png",
        "../ChessGamee/PieceImages/WhiteRook.png",
        "../ChessGamee/PieceImages/WhiteKnight.png",
        "../ChessGamee/PieceImages/WhiteBishop.png",
        "../ChessGamee/PieceImages/WhiteQueen.png",
        "../ChessGamee/PieceImages/WhiteKing.png",
        "../ChessGamee/PieceImages/WhitePawn.png",
    };



    public  void fill(JPanel chessboard,JButton[][] squares){
        for (int row = 0; row < GAME_ROWS; row++) {
            for (int col = 0; col < GAME_COLUMNS; col++) {
                JButton square = new JButton();
                square.setPreferredSize(new Dimension(BLOCK_WIDTH, BLOCK_HEIGHT));
                square.setBackground((row + col) % 2 == 0 ? WHITE_SQUARES : DARK_SQUARES);
                square.setFocusable(false);
                String piece = startingPosition[row][col];
                if(!piece.equals(".")){
                    ImageIcon icon = new ImageIcon(Objects.requireNonNull(getClass().getResource(pieceImages[getPieceIndex(piece)])));
                    square.setIcon(icon);
                }
                squares[row][col] = square;
                chessboard.add(square);
            }
        }
    }

    private int getPieceIndex(String piece) {
        return switch (piece) {
            case "BR" -> 0;
            case "BN" -> 1;
            case "BB" -> 2;
            case "BQ" -> 3;
            case "BK" -> 4;
            case "BP" -> 5;
            case "WR" -> 6;
            case "WN" -> 7;
            case "WB" -> 8;
            case "WQ" -> 9;
            case "WK" -> 10;
            default -> 11;
        };
    }
}
