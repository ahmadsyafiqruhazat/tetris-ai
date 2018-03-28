import java.lang.*;
import java.util.Arrays;
import java.util.ArrayList;

import java.util.stream.IntStream;

public class PlayerSkeleton {

  public static final int COLS = 10;
  public static final int ROWS = 21;
  public static final int N_PIECES = 7;

  //indices for legalMoves
  public static final int ORIENT = 0;
  public static final int SLOT = 1;

  protected static int[] pOrients;
  protected static int[][] pWidth;
  private static int[][] pHeight;
  private static int[][][] pBottom;
  private static int[][][] pTop;
  
  private static ArrayList<Integer> bumpinessArray = new ArrayList<Integer>();

  //implement this function to have a working system
  public int pickMove(State s, int[][] legalMoves) {
    int bestMove = 0;
    float maxHeuristic = -19998;
    int nextPiece = s.getNextPiece();
    WorkingState ws = new WorkingState(s);
    float[] weights = {1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f};
    float[] nextWeights = {1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f};
    Heuristics h = new Heuristics(weights);

    WorkingState nextWs;

    for (int i = 0; i < legalMoves.length; i++){
      float heuristicMove = -19998;
    
      nextWs = new WorkingState(nextPiece, legalMoves[i][ORIENT], legalMoves[i][SLOT], ws);
      if (!nextWs.lost) {
        heuristicMove = h.score(nextWs);
      }

      heuristicMove += getNextHeuristic(nextWs, nextWeights);

      if (heuristicMove > maxHeuristic){
        bestMove = i;
        maxHeuristic = heuristicMove;  
      }    
      // int[][] field = new int[ROWS][COLS];
      // int[] top = new int[COLS];
      // copyField(field, s.getField());
      // copyTop(top, s.getTop());
      
      // if (makeMove(field, top, turn, nextPiece, legalMoves[i][ORIENT], legalMoves[i][SLOT])) {
      //   heuristicMove = getHeuristic(field, top);
      // }
      
      // it would be great if we could make a copy of state
      // int[][] newState = s.testMove(legalMoves[i][ORIENT], legalMoves[i][SLOT]);
      // float heuristicMove = getHeuristic(newState);
        
      // System.out.println(i + ", " + heuristicMove);
    }
    
    // System.out.println(bestMove);
    
    return bestMove;
  }
  
  public float getNextHeuristic(WorkingState ws, float[] weights){    
    int[][][] legalMoves = new int[N_PIECES][][];
    
    // generate legal moves
    for(int i = 0; i < N_PIECES; i++) {
      //figure number of legal moves
      int n = 0;
      for(int j = 0; j < pOrients[i]; j++) {
        //number of locations in this orientation
        n += COLS+1-pWidth[i][j];
      }
      //allocate space
      legalMoves[i] = new int[n][2];
      //for each orientation
      n = 0;
      for(int j = 0; j < pOrients[i]; j++) {
        //for each slot
        for(int k = 0; k < COLS+1-pWidth[i][j];k++) {
          legalMoves[i][n][ORIENT] = j;
          legalMoves[i][n][SLOT] = k;
          n++;
        }
      }
    }                                         
    
    WorkingState nextWs;
    float[] nextHeuristic = new float[N_PIECES];
    Heuristics nextH = new Heuristics(weights);
    
    for (int i = 0; i < N_PIECES; i++){
      nextHeuristic[i] = -9999;  
    }
    
    // actual loop
    for (int n = 0; n < N_PIECES; n++){
      for (int i = 0; i < legalMoves[n].length; i++){
        nextWs = new WorkingState(n, legalMoves[n][i][ORIENT], legalMoves[n][i][SLOT], ws);
      
        float heuristicNextMove = -9999;
      
        if (!nextWs.lost) {
          heuristicNextMove = nextH.score(nextWs);
        }
      
        if (heuristicNextMove > nextHeuristic[n]){
          nextHeuristic[n] = heuristicNextMove;  
        }
      }  
    }
    
    float total = 0;
    for (float n: nextHeuristic) {
      total += n;  
      // System.out.println(total);
    } 
    
    float result = total / (float)N_PIECES;
    // System.out.println(result);
    return result;  
  }
  
  public static void main(String[] args) {
    State s = new State();
    pOrients = s.getpOrients();
    pWidth = s.getpWidth();
    pBottom = s.getpBottom();
    pHeight = s.getpHeight();
    pTop = s.getpTop();
    
    new TFrame(s);
    PlayerSkeleton p = new PlayerSkeleton();
    while(!s.hasLost()) {
      s.makeMove(p.pickMove(s,s.legalMoves()));
      s.draw();
      s.drawNext(0,0);
      try {
        Thread.sleep(1); 
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    int maxBumpiness = 0;
    int totalBumpiness = 0;
    
    for (int b: bumpinessArray){
      totalBumpiness += b;
      if (b > maxBumpiness){
        maxBumpiness = b;  
      }  
    }
    
    System.out.println("Max bumpiness: " + maxBumpiness);
    System.out.println("Mean bumpiness: " + totalBumpiness/bumpinessArray.size());
    
    System.out.println("You have completed "+s.getRowsCleared()+" rows.");
  }
  
  private class WorkingState extends State {
    public int[][] field = new int[ROWS][COLS];
    public int[] top = new int[COLS];
    public int turn, cleared;

    public WorkingState(State state) {
      field = cloneField(state.getField());
      top = cloneTop(state.getTop());
      cloneScores(state, true);
    }

    public WorkingState(int piece, int orient, int slot, State state) {
      this(state);
      makeSpecificMove(piece, orient, slot);
    }

    private void cloneScores(State s, boolean isStart) {
      turn = s.getTurnNumber();
      if (isStart) {
        // isStart means that we clone from a game state
        // total cleared is 0
        cleared = 0;
      } else {
        // else, it is from a workingState, so take existing cleared
        cleared = s.getRowsCleared();
      }
    }

    private int[][] cloneField(int[][] field) {
      int[][] clone = new int[ROWS][COLS];

      for (int i=0; i<ROWS; i++) {
        for (int j=0; j<COLS; j++) {
          clone[i][j] = field[i][j];
        }
      }

      return clone;
    }

    private int[] cloneTop(int[] top) {
      int[] clone = new int[COLS];

      for (int i=0; i<COLS; i++ ) {
        clone[i] = top[i];
      }

      return clone;
    }

    public void makeSpecificMove(int nextPiece, int orient, int slot) {
      turn++;
      //height if the first column makes contact
      // System.out.println(nextPiece + " " + orient + " " + slot);
      int height = top[slot]-pBottom[nextPiece][orient][0];
      //for each column beyond the first in the piece
      for(int c = 1; c < pWidth[nextPiece][orient];c++) {
        height = Math.max(height,top[slot+c]-pBottom[nextPiece][orient][c]);
      }
      
      //check if game ended
      if(height+pHeight[nextPiece][orient] >= ROWS) {
        lost = true;
        return;
      }

      
      //for each column in the piece - fill in the appropriate blocks
      for(int i = 0; i < pWidth[nextPiece][orient]; i++) {
        
        //from bottom to top of brick
        for(int h = height+pBottom[nextPiece][orient][i]; h < height+pTop[nextPiece][orient][i]; h++) {
          // System.out.println("h: " + h);
          field[h][i+slot] = turn;
        }
      }
      
      //adjust top
      for(int c = 0; c < pWidth[nextPiece][orient]; c++) {
        top[slot+c]=height+pTop[nextPiece][orient][c];
      }
      
      int rowsCleared = 0;
      
      //check for full rows - starting at the top
      for(int r = height+pHeight[nextPiece][orient]-1; r >= height; r--) {
        //check all columns in the row
        boolean full = true;
        for(int c = 0; c < COLS; c++) {
          if(field[r][c] == 0) {
            full = false;
            break;
          }
        }
        //if the row was full - remove it and slide above stuff down
        if(full) {
          rowsCleared++;
          cleared++;
          //for each column
          for(int c = 0; c < COLS; c++) {

            //slide down all bricks
            for(int i = r; i < top[c]; i++) {
              field[i][c] = field[i+1][c];
            }
            //lower the top
            top[c]--;
            while(top[c]>=1 && field[top[c]-1][c]==0) top[c]--;
          }
        }
      }
    }

    @Override
    public int[][] getField() {
      return field;
    }

    @Override
    public int[] getTop() {
      return top;
    }

    @Override
    public int getRowsCleared() {
      return cleared;
    }  

    @Override
    public int getTurnNumber() {
      return turn;
    }
  }

  private class Heuristics {
    public int[][] field;
    public int[] top;
    public float[] weights;
    public int rowsCleared;

    public Heuristics(float[] w) {
      this.weights = w;
    }

    public float score(State s) {
      this.field = s.getField();
      this.top = s.getTop();
      this.rowsCleared = s.getRowsCleared();
      float heuristic = 0;
      
      int maxHeight = getMaxHeight();
      int totalHeight =  getTotalHeight();
      int bumpiness = getBumpiness();
      int[] holesArray = getHoles();
      int numHoles = holesArray[0];
      int maxHoleHeight = holesArray[1];
      int holeDepth = holesArray[2];
      int numHoleRows = holesArray[3];
      int numHoleCols = holesArray[4];
      int concavity = getConcavity();
      
      heuristic -= (float)(weights[0]*maxHeight)/ROWS;  
      heuristic -= (float)(weights[1]*totalHeight)/(ROWS*COLS);
      heuristic -= (float)(weights[2]*bumpiness)/80;
      heuristic -= (float)(weights[3]*numHoles)/400;
      heuristic -= (float)(weights[4]*maxHoleHeight)/ROWS;
      heuristic -= (float)(weights[5]*holeDepth)/200;
      heuristic -= (float)(weights[6]*numHoleRows)/ROWS;
      heuristic -= (float)(weights[7]*numHoleCols)/COLS;
      heuristic -= (float)(weights[8]*concavity)/100;
      heuristic += (float)(weights[9]*(float)rowsCleared)/4;
      
      // System.out.println((weights[8]*concavity)/100);
      
      return heuristic;  
    }

    public int getMaxHeight() {
      return Arrays.stream(top).max().getAsInt();
    }

    public int getTotalHeight() {
      return Arrays.stream(top).sum();
    }

    public int getConcavity(){
      int concavity = 0;
      for (int j = 0; j < 4; j++){
        concavity += top[4] - top[j];  
      }
      for (int j = 6; j < COLS; j++){
        concavity += top[5] - top[j];   
      }
            
      return concavity;
    }
  
    public int[] getHoles(){
      int holes = 0;
      int maxHoleHeight = 0;
      int[] rowHoles = new int[ROWS];
      int[] colHoles = new int[COLS];
      int holeMultiplier = 1; // holes on top of holes are really bad
      int holeDepth = 1; // total number of blocks above holes
      int totalHoleDepth = 0;
      
      for (int j = 0; j < field[0].length; j++){
        holeMultiplier = 1; 
        holeDepth = 1;
        for (int i = top[j]-2; i >= 0; i--){
          if (field[i][j] == 0){
            totalHoleDepth += holeDepth;
            holeDepth = 0;
            holes += holeMultiplier;
            holeMultiplier++;
            if (top[j]>maxHoleHeight){
              maxHoleHeight = top[j];  
            }  
            rowHoles[i] = 1;
            colHoles[j] = 1;
            continue;
          } else {
            holeDepth++;
          }
            
        } 
      }

                  bumpinessArray.add(holes);
      int[] result = {holes, maxHoleHeight, totalHoleDepth, IntStream.of(rowHoles).sum(), IntStream.of(colHoles).sum()};
      return result;
    }

    public int getBumpiness() {
      int total = 0;
      for (int i = 0; i < top.length - 1; i++){
        total += Math.abs(top[i] - top[i+1]);
      }
        
      return total;
    }
  }

}