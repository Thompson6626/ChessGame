
import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class ChessGame implements ActionListener {

    //div

    File[] files={
                new File(Objects.requireNonNull(getClass().getResource("../ChessGamee/Sound/Move.wav")).toURI()),
                new File(Objects.requireNonNull(getClass().getResource("../ChessGamee/Sound/Capture.wav")).toURI()),
                };
    private final int filesNum=files.length;
    Clip[] clips= new Clip[filesNum];


// div
    protected static final int BOARD_ROWS = 8;
    protected static final int BOARD_COLUMNS = 8;
    private final JButton[][] squares;
    private boolean whiteTurn=true;

    protected static String[][] positions = ChessBoard.startingPosition;
    static Player white = new Player('W',"74");
    static Player black = new Player('B',"04");
    ChessBoard board= new ChessBoard();

    private static ArrayList<String> possible= new ArrayList<>();

    // Colors
    private static final Color  WHITE_SQUARES= ChessBoard.WHITE_SQUARES;
    private static final Color  DARK_SQUARES= ChessBoard.DARK_SQUARES;
    private static final Color ATTACK_SQUARES= new Color(236, 58, 58);
    private static final Color WHITE_POSSIBLE_SQUARES=new Color(155, 151, 151);
    private static final Color DARK_POSSIBLE_SQUARES=new Color(23, 98, 29);


    private static final String[][] CASTLE_POS={{"72","76"},{"02","06"}}; // 0 for white , 1 for black
    private static final String[][] PROMOTION_PIECES={{"WN", "WQ", "WR", "WB"},{"BN", "BQ", "BR", "BB"}};
    private static final String[][] PROMOTION_ICONS={
                    {"../ChessGamee/PieceImages/WhiteKnight.png", "../ChessGamee/PieceImages/WhiteQueen.png",
                    "../ChessGamee/PieceImages/WhiteRook.png", "../ChessGamee/PieceImages/WhiteBishop.png"},
                    {"../ChessGamee/PieceImages/BlackKnight.png", "../ChessGamee/PieceImages/BlackQueen.png",
                    "../ChessGamee/PieceImages/BlackRook.png", "../ChessGamee/PieceImages/WhiteBishop.png"}
                    };


    public ChessGame() throws UnsupportedAudioFileException, IOException, LineUnavailableException, URISyntaxException {
        JFrame frame = new JFrame("Chess Board");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel chessboardPanel = new JPanel(new GridLayout(BOARD_ROWS, BOARD_COLUMNS));
        chessboardPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        squares = new JButton[BOARD_ROWS][BOARD_COLUMNS];

        board.fill(chessboardPanel,squares);

        for(int i=0;i<BOARD_ROWS;i++){
            for(int j=0;j<BOARD_COLUMNS;j++){
                squares[i][j].addActionListener(this);
            }
        }
        openAudioStreams();
        updatePlayerMoves(white,black);
        frame.add(chessboardPanel);
        frame.pack();
        frame.setVisible(true);
    }
    public void openAudioStreams() throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        for(int i=0;i<filesNum;i++){
            AudioInputStream audioStream= AudioSystem.getAudioInputStream(files[i]);
            clips[i]=AudioSystem.getClip();
            clips[i].open(audioStream);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {

            Player player=(whiteTurn)? white : black;
            Player opponent=(whiteTurn)? black: white;

            for(int i=0;i<BOARD_ROWS;i++){
                for(int j=0;j<BOARD_COLUMNS;j++) {
                    if (e.getSource() == squares[i][j]) {
                        try {
                            playerMove(player,opponent, i, j);
                        } catch (LineUnavailableException | IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }
            }

        }

    public void playerMove(Player player,Player opponent, int i, int j) throws LineUnavailableException, IOException {
        char playerColor=player.getColorChar();

        String check = i + String.valueOf(j);

        char clickedPosFirstChar=positions[i][j].charAt(0);
        char clickedPosSecChar = 0;
        if(clickedPosFirstChar!='.') clickedPosSecChar=positions[i][j].charAt(1);

        if (player.isPieceTouched()) {
            int prevRow = player.getPrePos()[0];
            int prevCol = player.getPrePos()[1];

            if (!possible.contains(check) || (prevRow == i && prevCol == j) || clickedPosFirstChar==playerColor) {
                player.setPieceTouched(false);
                colorPossibleMoves(false, playerColor);
                possible=null;
                return;
            }

            boolean captSomething=false;

            // Castling
            if(player.lastPieceTouched=='K'&& Math.max(prevCol,j)-Math.min(prevCol,j)==2){
                castleHelper(player,i,j);
            }else if(player.lastPieceTouched=='P' && prevCol!=j && positions[i][j].equals(".")){ // En passant
                captSomething=enPassantHelper(i,j,player, prevRow,prevCol);
            }else{  // Normal moves (attacks or just moves to empty squares)
                captSomething=updateVisual(i,j,prevRow,prevCol, player,opponent);
            }

            player.pawnsWithEnPassantPotential.clear(); // Clearing en passant

            player.setPieceTouched(false);

            char lastPiecMoved=player.getLastPieceMoved();

            if(lastPiecMoved =='K'){
                player.kingPos=check;
                player.kingMoved=true;
            }else if(lastPiecMoved =='R') {   // In the boolean array the index 0 is for the left and 1 for the right rook
                if (prevCol == 0) {
                    player.rooksMoved[0] = true;  // If the last piece moved was a rook and it came from the colum 0 make the left rook moved  true
                } else if (prevCol == 7) {
                    player.rooksMoved[1] = true;
                }
            }else if (lastPiecMoved == 'P') { // Checking if the last piece moved is a pawn and depending of the color if it was a promotion row
                if ((i == 0 && playerColor == 'W') || (i == 7 && playerColor == 'B')) {
                    checkPromotion(i, j, playerColor);
                }
                if(Math.max(prevRow,i)-Math.min(prevRow,i)==2){ // Checking if the pawn moved 2 spaces for en passant
                    checkEnPassant(i,j,opponent); // Checking if theres another pawn on the sides
                }
            }

            updatePlayerMoves(player,opponent);

            if(!player.enemyKingAttackers.isEmpty()) opponent.isInCheck=true; //If after updating the players moves
            opponent.enemyKingAttackers.clear();
            player.isInCheck=false;

            updatePlayerMoves(opponent,player);

            if (opponent.isInCheck || captSomething) clipPlayer(clips[1]);
            else clipPlayer(clips[0]);


            // Checking if the enemy is checkmated
            if(opponent.isInCheck) checkCheckMate(opponent,player);

            whiteTurn = !whiteTurn;  // Changing turns

            colorPossibleMoves(false, playerColor);
        } else {
            if (clickedPosFirstChar == playerColor) {
                player.setPrePos(i, j);
                player.setPieceTouched(true);

                possible=player.possMovementsperPiece.get(check);

                player.lastPieceTouched=clickedPosSecChar;

                colorPossibleMoves(true, playerColor);
            }
        }
    }

    public boolean updateVisual(int i, int j, int prevRow, int prevCol, Player player, Player opponent){
        squares[i][j].setIcon(squares[prevRow][prevCol].getIcon());
        squares[prevRow][prevCol].setIcon(null);

        boolean captured=positions[i][j].charAt(0) == opponent.getColorChar();
        positions[i][j] = positions[prevRow][prevCol];
        positions[prevRow][prevCol] = ".";
        player.setLastPieceMoved(positions[i][j].charAt(1));

        return captured;
    }


    public boolean enPassantHelper(int i, int j, Player player, int prevRow, int prevCol) {

        int pawnPassedRow=(player.getColorChar()=='W')? i+1:i-1;
        squares[pawnPassedRow][j].setIcon(null);

        squares[i][j].setIcon(squares[prevRow][prevCol].getIcon());
        squares[prevRow][prevCol].setIcon(null);

        positions[i][j]=positions[prevRow][prevCol];
        positions[prevRow][prevCol]=".";
        positions[pawnPassedRow][j]=".";
        player.setLastPieceMoved('P');

        return true;
    }

    public void checkEnPassant(int destRow,int destCol,Player opponent){

        int[] directions = {-1,1};
        char opponentColor=opponent.getColorChar();
        int attackRow=(opponentColor=='W')? -1 : 1;

        for(int direction:directions){
            int destPos=destCol+direction;
            if(destPos >= 0 & destPos < 8){
                if(positions[destRow][destPos].equals(opponentColor+"P")){
                    opponent.pawnsWithEnPassantPotential.put(destRow+String.valueOf(destPos),(destRow+attackRow)+String.valueOf(destCol));
                }
            }
        }
    }


    public void castleHelper(Player player, int rookRow, int rookCol) {

        if(rookCol==2) rookCol-=2;
        else if(rookCol==6)rookCol+=1;

        boolean isLeft = rookCol == 0;
        boolean isRight = rookCol == 7;

        int kingCol = player.kingPos.charAt(1) - '0';

        ImageIcon king = (ImageIcon) squares[rookRow][kingCol].getIcon();
        ImageIcon rook = (ImageIcon) squares[rookRow][rookCol].getIcon();

        if (isLeft || isRight) {
            int kingDestCol = isLeft ? kingCol - 2 : kingCol + 2; // Where the king indes in either castle
            int rookDestCol = isLeft ? rookCol + 3 : rookCol - 2; // Where the rook ends in left castle | then right castle

            movePiece(rookRow, kingCol, king, rookRow, kingDestCol);
            movePiece(rookRow, rookCol, rook, rookRow, rookDestCol);
        }

        player.kingMoved = true;
        clipPlayer(clips[0]);
    }

    private void movePiece(int rowFrom, int colFrom, ImageIcon pieceFrom, int rowTo, int colTo) {
        squares[rowTo][colTo].setIcon(pieceFrom);
        squares[rowFrom][colFrom].setIcon(null);
        positions[rowTo][colTo] = positions[rowFrom][colFrom];
        positions[rowFrom][colFrom] = ".";
    }


    public void clipPlayer(Clip clip){
        clip.start();
        clip.loop(0);
        clip.setMicrosecondPosition(0);
    }

    public void checkCheckMate(Player player,Player opponent){
        for(String str:player.possMovementsperPiece.keySet()){
            if(str.equals(player.kingPos)){
                for(String string:player.possMovementsperPiece.get(str)){
                    int row=string.charAt(0)-'0';
                    int col=string.charAt(1)-'0';
                    if(positions[row][col].charAt(0)!= player.getColorChar()){
                        return;
                    }
                }
            }else if(!player.possMovementsperPiece.get(str).isEmpty()){
                return;
            }
        }
        disableAllButtons();
        String winner=(opponent.getColorChar()=='W')? "White":"Black";
        JOptionPane.showMessageDialog(null,winner+" won the game!!");

    }


    public void disableAllButtons(){
            for (int i = 0; i < BOARD_ROWS; i++) {
                for (int j = 0; j < BOARD_COLUMNS; j++) {
                    squares[i][j].setEnabled(false);
                }
            }
    }

    //Fix checkmate king still has moves to friendly spaces
    private void handleKingMoves(ArrayList<String> possible, Player opponentPlayer) {
        possible.removeAll(opponentPlayer.kingPossibleMovesForKings);
        for(String str:opponentPlayer.possMovementsperPiece.keySet()){
            int row=str.charAt(0)-'0';
            int col=str.charAt(1)-'0';
            if(positions[row][col].charAt(0)!='.' && positions[row][col].charAt(1)=='P') {
                continue;
            }
            possible.removeAll(opponentPlayer.possMovementsperPiece.get(str));
        }
        possible.removeAll(opponentPlayer.pawnAttacksForKings);
    }

    public void colorPossibleMoves(boolean paint,char playerColor){
        for (String str : possible) {
            int row = str.charAt(0) - '0';
            int col = str.charAt(1) - '0';
            if(paint) {
                if(positions[row][col].charAt(0)!=playerColor && !positions[row][col].equals(".")) {
                    squares[row][col].setBackground(ATTACK_SQUARES);
                }else if(positions[row][col].charAt(0)!=playerColor ){
                    squares[row][col].setBackground((row + col) % 2 == 0 ? WHITE_POSSIBLE_SQUARES : DARK_POSSIBLE_SQUARES);
                }
            }else {
                squares[row][col].setBackground((row + col) % 2 == 0 ? WHITE_SQUARES : DARK_SQUARES);
            }
        }

    }

    public void checkPiece(ArrayList<String> possible, int i , int j, Player player,Player opponent){
        char piece=positions[i][j].charAt(1);

        switch (piece){
            case 'N'-> PossibleMovements.checkKnight(possible,i,j,player,opponent);
            case 'Q'->{
                PossibleMovements.checkVertical(possible,i,j,player,opponent);
                PossibleMovements.checkHorizontal(possible,i,j,player,opponent);
                PossibleMovements.checkDiagonals(possible,i,j,player,opponent);
            }
            case 'B'-> PossibleMovements.checkDiagonals(possible,i,j,player,opponent);
            case 'R'->{
                PossibleMovements.checkVertical(possible,i,j,player,opponent);
                PossibleMovements.checkHorizontal(possible,i,j,player,opponent);
            }
            case 'K'-> PossibleMovements.checkKing(possible,i,j,player,opponent);
            case 'P'-> PossibleMovements.pawnMovements(possible,i,j,player);
        }
    }

    public void checkPromotion(int row, int col, char playerColor) {
        String[] pieceCodes = (playerColor == 'W') ? PROMOTION_PIECES[0] : PROMOTION_PIECES[1];
        String[] pieceIcons = (playerColor == 'W') ? PROMOTION_ICONS[0] : PROMOTION_ICONS[1];

        String[] options = {"Knight", "Queen", "Rook", "Bishop"}; //

        JPanel panel = new JPanel();

        for (int i = 0; i < options.length; i++) {
            JButton button = new JButton();
            ImageIcon icon = new ImageIcon(Objects.requireNonNull(getClass().getResource(pieceIcons[i])));
            button.setIcon(icon);
            button.setFocusable(false);
            final int index = i;
            button.addActionListener(e -> {
                squares[row][col].setIcon(button.getIcon());
                positions[row][col] = pieceCodes[index];
                SwingUtilities.getWindowAncestor(panel).dispose();
            });
            panel.add(button);
        }

        JOptionPane.showOptionDialog(null, panel, "Promotion!", JOptionPane.YES_NO_OPTION,
                JOptionPane.PLAIN_MESSAGE, null, new Object[]{}, null);
    }



    public void updatePlayerMoves(Player player,Player opponent) { //Updating the player possible moves after a move
        player.possMovementsperPiece= new HashMap<>();
        player.pawnAttacksForKings= new ArrayList<>();
        player.kingPossibleMovesForKings= new ArrayList<>();
        opponent.pinnedPieces= new ArrayList<>();
        player.piecesPinning= new ArrayList<>();

        HashMap<String,ArrayList<String>> perPiece= player.possMovementsperPiece;
        for (int i = 0; i < BOARD_ROWS; i++) {
            for (int j = 0; j < BOARD_COLUMNS; j++) {
                if(positions[i][j].equals(".") || positions[i][j].charAt(0)!=player.getColorChar()) continue;

                ArrayList<String> addTo= new ArrayList<>();

                checkPiece(addTo, i, j, player,opponent);
                String position=i+String.valueOf(j);
                char pieceChar=positions[i][j].charAt(1);
                if(pieceChar!='K'){
                    if(pieceChar=='P'){
                        PossibleMovements.pawnSpecificsForKings(player.pawnAttacksForKings,i,j,player);
                        if(player.pawnsWithEnPassantPotential.containsKey(position)){
                            addTo.add(player.pawnsWithEnPassantPotential.get(position));
                        }
                    }
                    if(player.isInCheck){
                        if(opponent.enemyKingAttackers.size()>1){ // If theres 2 attackers then make it so only the king can move (if possible)
                            addTo.clear();
                        }else{
                            addTo.retainAll(opponent.enemyKingAttackers.get(0));
                        }
                    }
                }else{
                    player.kingPossibleMovesForKings.addAll(addTo);
                    handleKingMoves(addTo, opponent);
                    if(!player.kingMoved && !player.isInCheck &&(!player.rooksMoved[0] || !player.rooksMoved[1])){
                        checkCastling(addTo,player,opponent);
                    }
                }
                if(player.pinnedPieces.contains(position)){
                    addTo.retainAll(opponent.piecesPinning);
                }
                perPiece.put(position,addTo);
            }
        }
    }
    // Checking if the conditions for castling to be possible  are met
    public void checkCastling(ArrayList<String> kingMoves,Player player,Player opponent){

        // Index 0 of the array for leftRookCastle and 1 for rightRookCastle

        boolean[] rooksCastle={true,true};
        String[] rooksPositions=(player.getColorChar()=='W')? CASTLE_POS[0]:CASTLE_POS[1];

        int kRow=player.kingPos.charAt(0)-'0';
        int kCol=player.kingPos.charAt(1)-'0';

        for (int i = 1; i <= 3; i++) {
            if (!positions[kRow][kCol - i].equals(".")) {
                rooksCastle[0] = false; // Left castle
            }
            if (i <= 2 && !positions[kRow][kCol + i].equals(".")) {
                rooksCastle[1] = false; // Right castle
            }
        }

        for(int i=0;i<=1;i++){
            if(rooksCastle[i] && !opponent.sightEnemyCastling[i] && !player.rooksMoved[i]){
                kingMoves.add(rooksPositions[i]);
            }
        }

    }


}
