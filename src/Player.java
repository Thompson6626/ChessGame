
import java.util.ArrayList;
import java.util.HashMap;

public class Player {

    private final int[] prePos= new int[2];
    private  boolean pieceTouched=false;
    private final char colorChar;
    private char lastPieceMoved;
    protected String kingPos;
    protected char lastPieceTouched;
    protected HashMap<String,String> pawnsWithEnPassantPotential= new HashMap<>();
    protected ArrayList<ArrayList<String>> enemyKingAttackers= new ArrayList<>();
    protected HashMap<String,ArrayList<String>> possMovementsperPiece= new HashMap<>();
    protected ArrayList<String> pawnAttacksForKings= new ArrayList<>();
    protected ArrayList<String> kingPossibleMovesForKings= new ArrayList<>();
    protected ArrayList<String> pinnedPieces= new ArrayList<>();
    protected ArrayList<String> piecesPinning= new ArrayList<>();
    protected boolean isInCheck=false;
    protected boolean[] rooksMoved=new boolean[2];  //? [0] left rook , [1] right rook
    protected boolean kingMoved=false;
    protected boolean[] sightEnemyCastling= new boolean[2];


    Player( char colorChar,String kingPos){
        this.colorChar=colorChar;
        this.kingPos=kingPos;
    }

    public void setPrePos(int prePos, int prePos2) {
        this.prePos[0] = prePos;
        this.prePos[1] = prePos2;
    }
    public char getLastPieceMoved() {
        return lastPieceMoved;
    }

    public void setLastPieceMoved(char lastPieceMoved) {
        this.lastPieceMoved = lastPieceMoved;
    }

    public void setPieceTouched(boolean pieceTouched) {
        this.pieceTouched = pieceTouched;
    }

    public int[] getPrePos() {
        return prePos;
    }

    public boolean isPieceTouched() {
        return pieceTouched;
    }

    public char getColorChar() {
        return colorChar;
    }
}
