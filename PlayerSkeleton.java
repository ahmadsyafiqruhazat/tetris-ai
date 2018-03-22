import java.lang.*;
import java.util.Arrays;

public class PlayerSkeleton {

  public static final int COLS = 10;
  public static final int ROWS = 21;

  //indices for legalMoves
  public static final int ORIENT = 0;
  public static final int SLOT = 1;

  protected static int[] pOrients;
  protected static int[][] pWidth;
  private static int[][] pHeight;
  private static int[][][] pBottom;
  private static int[][][] pTop;
  private double[] weights;
  

  //implement this function to have a working system
  public int pickMove(State s, int[][] legalMoves) {
    int bestMove = 0;
    float maxHeuristic = -9999;
    int nextPiece = s.getNextPiece();
    WorkingState ws = new WorkingState(s);
    float[] weights = {1.0f, 1.0f, 1.0f, 1.0f};
    Heuristics h = new Heuristics(weights);

    WorkingState nextWs;

    for (int i = 0; i < legalMoves.length; i++){
      nextWs = new WorkingState(nextPiece, legalMoves[i][ORIENT], legalMoves[i][SLOT], ws);
      if (!nextWs.lost) {
        float heuristicMove = h.score(nextWs);
        if (heuristicMove > maxHeuristic){
          bestMove = i;
          maxHeuristic = heuristicMove;  
        }    
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
          System.out.println("h: " + h);
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

    public Heuristics(float[] w) {
      this.weights = w;
    }

    public float score(State s) {
      this.field = s.getField();
      this.top = s.getTop();
      float heuristic = 0;

      heuristic -= (float) (weights[0] * getMaxHeight());
      heuristic -= (float) (weights[1] * getTotalHeight());
      heuristic -= (float) (weights[2] * getBumpiness());
      heuristic -= (float) (weights[3] * getHoles());

      return heuristic;
    }

    public int getMaxHeight() {
      return Arrays.stream(top).max().getAsInt();
    }

    public int getTotalHeight() {
      return Arrays.stream(top).sum();
    }

    // also counts blocks above holes
    public int getHoles() {
      int holes = 0;
      int holeMultiplier = 1; // holes on top of holes are really bad
      int holeDepth = 1; // total number of blocks above holes

      for (int j = 0; j < field[0].length; j++) {
        for (int i = top[j] - 2; i >= 0; i--) {
          if (field[i][j] == 0) {
            holes += holeMultiplier;
            holeMultiplier++;
            continue;
          }
          holeDepth++;
        }
        holeMultiplier = 1;
      }
      // System.out.println(holes); 
      return holes + holeDepth;
    }

    public int getBumpiness() {
      int total = 0;
      for (int i = 0; i < top.length - 1; i++) {
        total += Math.abs(top[i] - top[i + 1]);
      }
      return total;
    }
  }

  private void setWeights(double[] weights){
    this.weights = weights;
  }

  public static int run(double[] weights){
    State s = new State();
    pOrients = s.getpOrients();
    pWidth = s.getpWidth();
    pBottom = s.getpBottom();
    pHeight = s.getpHeight();
    pTop = s.getpTop();

    PlayerSkeleton p = new PlayerSkeleton();
    p.setWeights(weights);
    while(!s.hasLost() && s.getTurnNumber()<501) {
      s.makeMove(p.pickMove(s,s.legalMoves()));
    }
    return s.getRowsCleared();
  }
  

  public static void main(String[] args) {
    State s = new State();

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
    System.out.println("You have completed "+s.getRowsCleared()+" rows.");
  }
}

