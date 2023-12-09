
import java.util.ArrayList;


public class PossibleMovements {

    protected static String[][] positions= ChessGame.positions;
    private static final String[][] LEFT_CASTLING={{"01", "02", "03"},{"71", "72", "73"}};
    private static final String[][] RIGHT_CASTLING={{"05", "06"},{"75", "76"}};

    public static void checkVertical(ArrayList<String> possible, int row, int col, Player player, Player opponent) {
        int[][] directions = {{-1, 0}, {1, 0}};
        checkDirection(possible, row, col, player,opponent, directions,true);
    }

    public static void checkHorizontal(ArrayList<String> possible, int row, int col, Player player,Player opponent) {
        int[][] directions = {{0, -1}, {0, 1}};
        checkDirection(possible, row, col, player,opponent, directions,true);
    }

    public static void checkDiagonals(ArrayList<String> possible, int row, int col, Player player,Player opponent) {
        int[][] directions = {{-1, 1}, {-1, -1}, {1, -1}, {1, 1}};
        checkDirection(possible, row, col, player,opponent, directions,true);
    }
    public static void checkKnight(ArrayList<String> possible, int row, int col, Player player,Player opponent){
        int[][] directions={{-2,-1},{-2,1},{2,-1},{2,1},{-1,-2},{1,-2},{-1,2},{1,2}};
        checkDirection(possible,row,col,player,opponent,directions,false);
    }

    public static void checkKing(ArrayList<String> possible, int row, int col, Player player,Player opponent){
        int[][] directions={{-1,0},{1,0},{0,-1},{0,1},{-1,-1},{-1,1},{1,-1},{1,1}};
        checkDirection(possible,row,col,player,opponent,directions,false);
    }

    private static void checkDirection(ArrayList<String> possible, int row, int col, Player player,Player opponent, int[][] directions, boolean continuous) {
        char playerColor = player.getColorChar();
        char opponentColor = (playerColor == 'W') ? 'B' : 'W';
        boolean ifFound = false;

        player.sightEnemyCastling[0] = false; // Left castle
        player.sightEnemyCastling[1] = false; // Right castle

        for (int[] direction : directions) {
            int i = row + direction[0];
            int j = col + direction[1];
            ArrayList<String> possibleAttackers = new ArrayList<>();
            ArrayList<String> forward= new ArrayList<>();
            while (i >= 0 && i < 8 && j >= 0 && j < 8) {
                String add = i + String.valueOf(j);
                if (!ifFound && (positions[i][j].equals(opponentColor + "K") || positions[i][j].equals("."))) {

                    if (!opponent.kingMoved && !opponent.rooksMoved[0]&&!opponent.rooksMoved[1]) { // Check if the king moved or not to see if they can still castle
                        checkLineOfSightOfCastling(add, playerColor, player);
                    }
                    possible.add(add);
                    forward.add(add);

                    possibleAttackers.add(add);
                    if (positions[i][j].equals(opponentColor + "K")) {
                        String lastPos = row + String.valueOf(col);
                        possibleAttackers.add(lastPos);
                        player.enemyKingAttackers.add(possibleAttackers);
                        ifFound = true;
                    }
                } else {
                    if (!add.equals(player.kingPos)){ // King cannot defend pieces from king
                        possible.add(add);
                    }
                    if(continuous && positions[i][j].charAt(0)!=playerColor){
                        ArrayList<String> trajectory= new ArrayList<>();// Checking if trough the piece theres the enemy king
                        while(true){
                            i += direction[0];
                            j += direction[1];
                            if(i < 0 || i >= 8 || j < 0 || j >= 8) break;
                            if(positions[i][j].equals(opponentColor+"K")){
                                player.piecesPinning.addAll(forward);
                                player.piecesPinning.addAll(trajectory);
                                player.piecesPinning.add(row+String.valueOf(col));
                                opponent.pinnedPieces.add(add);
                                break;
                            }
                            if(positions[i][j].charAt(0)==playerColor){
                                break;
                            }
                            if(positions[i][j].charAt(0)==opponentColor && positions[i][j].charAt(1)!='K'){
                                break;
                            }
                            trajectory.add(i+String.valueOf(j));
                        }
                    }
                    break;
                }
                i += direction[0];
                j += direction[1];
                if (!continuous) break; // If pieces can just move 1 square (knight or king) then break
            }
        }
    }

    private static void checkLineOfSightOfCastling(String add, char playerColor, Player player) {
        String[] opponentLeftCastling = (playerColor == 'W') ? LEFT_CASTLING[0] : LEFT_CASTLING[1];
        String[] opponentRightCastling = (playerColor == 'W') ? RIGHT_CASTLING[0] : RIGHT_CASTLING[1];

        String[][] castlingSide={opponentLeftCastling,opponentRightCastling};

        for(int i=0;i<=1;i++){
            for(String str:castlingSide[i]){
                if(add.equals(str)){
                    player.sightEnemyCastling[i]=true;
                    break;
                }
            }
        }

    }




    public static void pawnSpecificsForKings(ArrayList<String> possible, int row, int col, Player player){  // Spots to avoid just for the king
        char playerColor=player.getColorChar();
        int forwardDirection = (playerColor == 'W') ? -1 : 1;

        if (row + forwardDirection >= 0 && row + forwardDirection < 8) {
            if (col + 1 < 8 ) {
                possible.add((row + forwardDirection) + String.valueOf(col + 1));
            }
            if (col - 1 >= 0) {
                possible.add((row + forwardDirection) + String.valueOf(col - 1));
            }
        }
    }

    public static void pawnMovements(ArrayList<String> possible, int row, int col, Player player) {
        char playerColor=player.getColorChar();
        int forwardDirection = (playerColor == 'W') ? -1 : 1;
        int startingRow = (playerColor == 'W') ? 6 : 1;
        char opponentColor = (playerColor == 'W') ? 'B' : 'W';

        if (row == startingRow && positions[row + 2 * forwardDirection][col] .equals(".") && positions[row+forwardDirection][col].equals(".")) { //?? If pawn didnt move it can still move 2 blocks
            possible.add((row + 2 * forwardDirection) + String.valueOf(col));
        }

        int[] colDirections={-1,1};

        if (row + forwardDirection >= 0 && row + forwardDirection < 8) {
            if (positions[row + forwardDirection][col] .equals(".")) {
                possible.add((row + forwardDirection) + String.valueOf(col));
            }
            for(int i:colDirections){ // Checking if the pawn has enemies in the diagonal to attack
                pawnAttacking(row,col,forwardDirection,i,opponentColor,possible,player);
            }
        }
    }
    public static void pawnAttacking(int row,int col,int forwardDirection,int sideDirection,char opponentColor,ArrayList<String> possible,Player player){
        if((col+sideDirection<8&&col+sideDirection>=0) && positions[row+forwardDirection][col+sideDirection].charAt(0)==opponentColor){
            String toAdd=(row+forwardDirection) + String.valueOf(col+sideDirection);
            possible.add(toAdd);
            if(positions[row+forwardDirection][col+sideDirection].equals(opponentColor+"K")){
                ArrayList<String> attackers= new ArrayList<>();
                attackers.add(toAdd);
                attackers.add(row +String.valueOf(col));
                player.enemyKingAttackers.add(attackers);
            }
        }
    }

}
