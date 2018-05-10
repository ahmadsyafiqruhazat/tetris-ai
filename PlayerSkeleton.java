import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.Collections;
import java.util.stream.IntStream;

public class PlayerSkeleton {

  public static final int COLS = State.COLS;
  public static final int ROWS = State.ROWS;
  public static final int N_PIECES = State.N_PIECES;

  //indices for legalMoves
  public static final int ORIENT = 0;
  public static final int SLOT = 1;

  protected static int[] pOrients;
  protected static int[][] pWidth;
  private static int[][] pHeight;
  private static int[][][] pBottom;
  private static int[][][] pTop;


  private double[] weights = Constants.defaultWeights;
  private double[] nextWeights = Constants.defaultWeights;
  private static double PRUNE_RATE_INITIAL = 0.7;
  private static double PRUNE_RATE_FINAL = 0.8;
  private static int[][][] legalMoves = new int[N_PIECES][][];

  // ForkJoinPool for concurrent execution
  private ForkJoinPool forkJoinExecutor;
  private ConcurrentExecutor concurrentExecutor;


  public PlayerSkeleton(ForkJoinPool forkJoinPool) {
    this.forkJoinExecutor = forkJoinPool;
    this.concurrentExecutor = new ConcurrentExecutor(forkJoinPool);
    double[] newWeights = Constants.defaultWeights;
    double[] newNextWeights = Constants.defaultWeights;
    this.weights = newWeights;
    this.nextWeights = newNextWeights;

  }

  public PlayerSkeleton(ForkJoinPool forkJoinPool, double[] weights, double[] nextWeights) {
    this.concurrentExecutor = new ConcurrentExecutor(forkJoinPool);

  }

  //implement this function to have a working system
  public int pickMove(State s, int[][] legalMoves) {
    int bestMove = 0;
    double maxHeuristic = -Constants.MAX_HEURISTICS;
    int nextPiece = s.getNextPiece();
    WorkingState ws = new WorkingState(s);
    
    Heuristics h = new Heuristics(weights);

    Possibility[] possibilities = new Possibility[legalMoves.length];

    for (int i = 0; i < legalMoves.length; i++) {
      // nextWs = new WorkingState(nextPiece, legalMoves[i][ORIENT], legalMoves[i][SLOT], ws);
      possibilities[i] = new Possibility(nextPiece, i, legalMoves);
      possibilities[i].defineWS(ws);

      if (!possibilities[i].state.lost) {
        possibilities[i].setScore(h.score(possibilities[i].state));
      }
    }

    Arrays.sort(possibilities, Collections.reverseOrder());

    // System.out.println("Unpruned: ");
    for (int i = 0; i < (int) ((1 - PRUNE_RATE_INITIAL) * (possibilities.length)); i++) {
      // we only want the score of the leaf, it does not matter what the middle nodes scores are right?
      possibilities[i].setScore(ldfsGetNextHeuristic(possibilities[i].state, nextWeights, 0));
      if (possibilities[i].score > maxHeuristic) {
        bestMove = possibilities[i].idx;
        maxHeuristic = possibilities[i].score;
      }
    }

    return bestMove;
  }


  public double ldfsGetNextHeuristic(WorkingState ws, double[] weights, int depthLimit) {
    double[] nextHeuristic = new double[N_PIECES];
    Possibility[] possibilities;
    Heuristics h = new Heuristics(nextWeights);
    double maxHeuristic;

    for (int i = 0; i < N_PIECES; i++) {
      nextHeuristic[i] = -Constants.MAX_HEURISTICS;
    }
    // actual loop
    // o(n*)
    for (int n = 0; n < N_PIECES; n++) {
      // new array of possibilities for piece n
      possibilities = new Possibility[legalMoves[n].length];
      maxHeuristic = -Constants.MAX_HEURISTICS;
      for (int i = 0; i < legalMoves[n].length; i++) {
        possibilities[i] = new Possibility(n, i, legalMoves[n]);
        possibilities[i].defineWS(ws);

        if (!possibilities[i].state.lost) {
          possibilities[i].setScore(h.score(possibilities[i].state));
        }
      }

      Arrays.sort(possibilities, Collections.reverseOrder());

      if (depthLimit == 0) {
        nextHeuristic[n] = possibilities[0].score;
      } else {
        for (int i = 0; i < (int) ((1 - PRUNE_RATE_FINAL) * (possibilities.length)); i++) {
          possibilities[i].setScore(ldfsGetNextHeuristic(possibilities[i].state, nextWeights, depthLimit - 1));
          if (possibilities[i].score > maxHeuristic) {
            maxHeuristic = possibilities[i].score;
          }
        }
        nextHeuristic[n] = maxHeuristic;
      }
    }

    return Arrays.stream(nextHeuristic).average().orElse(-Constants.MAX_HEURISTICS);
  }

  public double getNextHeuristic(WorkingState ws, double[] weights) {
    int[][][] legalMoves = new int[N_PIECES][][];

    // generate legal moves
    for (int i = 0; i < N_PIECES; i++) {
      //figure number of legal moves
      int n = 0;
      for (int j = 0; j < pOrients[i]; j++) {
        //number of locations in this orientation
        n += COLS + 1 - pWidth[i][j];
      }
      //allocate space
      legalMoves[i] = new int[n][2];
      //for each orientation
      n = 0;
      for (int j = 0; j < pOrients[i]; j++) {
        //for each slot
        for (int k = 0; k < COLS + 1 - pWidth[i][j]; k++) {
          legalMoves[i][n][ORIENT] = j;
          legalMoves[i][n][SLOT] = k;
          n++;
        }
      }
    }

    WorkingState nextWs;
    double[] nextHeuristic = new double[N_PIECES];
    Heuristics nextH = new Heuristics(weights);

    for (int i = 0; i < N_PIECES; i++) {
      nextHeuristic[i] = -Constants.MAX_HEURISTICS;
    }

    // actual loop
    // o(n*)
    for (int n = 0; n < N_PIECES; n++) {
      for (int i = 0; i < legalMoves[n].length; i++) {
        nextWs = new WorkingState(n, legalMoves[n][i][ORIENT], legalMoves[n][i][SLOT], ws);

        // default score for if next move causes lost
        double heuristicNextMove = -Constants.MAX_HEURISTICS;

        if (!nextWs.lost) {
          heuristicNextMove = nextH.score(nextWs);
        }

        if (heuristicNextMove > nextHeuristic[n]) {
          nextHeuristic[n] = heuristicNextMove;
        }
      }
    }

    double total = 0;
    for (double n : nextHeuristic) {
      // System.out.println("nextHeuristic: " + n);
      total += n;
      // System.out.println(total);
    }

    double result = total / (double) N_PIECES;
    // System.out.println(result);
    return result;
  }

  public static void main(String[] args) {
    State s = new State();
    pOrients = State.getpOrients();
    pWidth = State.getpWidth();
    pBottom = State.getpBottom();
    pHeight = State.getpHeight();
    pTop = State.getpTop();

    initializeLegalMoves();

    new TFrame(s);

    ForkJoinPool forkJoinPool = new ForkJoinPool();
    PlayerSkeleton p = new PlayerSkeleton(forkJoinPool);
    while (!s.hasLost()) {
      s.makeMove(p.pickMove(s, s.legalMoves()));
      s.draw();
      s.drawNext(0, 0);
      try {
        Thread.sleep(0);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

    }
    System.out.println("You have completed " + s.getRowsCleared() + " rows.");
  }




  static void initializeLegalMoves() {

    // generate legal moves - done globally for use in ldfs
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
  }

  /**
   * A working State for running the game
   */
  private class WorkingState {
    public int[][] field = new int[ROWS][COLS];
    public int[] top = new int[COLS];
    public int turn, cleared;
    
    public boolean lost = false;

    public WorkingState(State state) {
      field = cloneField(state.getField());
      top = cloneTop(state.getTop());
      cloneScores(state, true);
    }

    public WorkingState(WorkingState ws) {
      field = cloneField(ws.getField());
      top = cloneTop(ws.getTop());
      cloneScores(ws, true);
    }

    public WorkingState(int piece, int orient, int slot, State state) {
      this(state);
      makeSpecificMove(piece, orient, slot);
    }

    public WorkingState(int piece, int orient, int slot, WorkingState ws) {
      this(ws);
      makeSpecificMove(piece, orient, slot);
    }

    public WorkingState(int[][] field, int[] top, int turn, int cleared) {
      this.field = field;
      this.top = top;
      this.turn = turn;
      this.cleared = cleared;
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

    private void cloneScores(WorkingState ws, boolean isStart) {
      turn = ws.getTurnNumber();
      if (isStart) {
        // isStart means that we clone from a game state
        // total cleared is 0
        cleared = 0;
      } else {
        // else, it is from a workingState, so take existing cleared
        cleared = ws.getRowsCleared();
      }
    }

    private int[][] cloneField(int[][] field) {
      int[][] clone = new int[ROWS][COLS];

      for (int i = 0; i < ROWS; i++) {
        for (int j = 0; j < COLS; j++) {
          clone[i][j] = field[i][j];
        }
      }

      return clone;
    }

    private int[] cloneTop(int[] top) {
      int[] clone = new int[COLS];

      for (int i = 0; i < COLS; i++) {
        clone[i] = top[i];
      }

      return clone;
    }

    public MoveResult makeSpecificMove(int nextPiece, int orient, int slot) {
      //            // For concurrent implementation
      //            int[][] field = cloneField(this.field);
      //            int[] top = cloneTop(this.top);
      //            int turn = this.turn + 1;

      turn++;

      //height if the first column makes contact
      // System.out.println(nextPiece + " " + orient + " " + slot);
      int height = top[slot] - pBottom[nextPiece][orient][0];
      //for each column beyond the first in the piece
      for (int c = 1; c < pWidth[nextPiece][orient]; c++) {
        height = Math.max(height, top[slot + c] - pBottom[nextPiece][orient][c]);
      }

      //check if game ended
      if (height + pHeight[nextPiece][orient] >= ROWS) {
        lost = true;
        return new MoveResult(field, top, turn, true, 0);
      }

      //for each column in the piece - fill in the appropriate blocks
      for (int i = 0; i < pWidth[nextPiece][orient]; i++) {

        //from bottom to top of brick
        for (int h = height + pBottom[nextPiece][orient][i]; h < height + pTop[nextPiece][orient][i]; h++) {
          // System.out.println("h: " + h);
          field[h][i + slot] = turn;
        }
      }

      //adjust top
      for (int c = 0; c < pWidth[nextPiece][orient]; c++) {
        top[slot + c] = height + pTop[nextPiece][orient][c];
      }

      int rowsCleared = 0;

      //check for full rows - starting at the top
      for (int r = height + pHeight[nextPiece][orient] - 1; r >= height; r--) {
        //check all columns in the row
        boolean full = true;
        for (int c = 0; c < COLS; c++) {
          if (field[r][c] == 0) {
            full = false;
            break;
          }
        }
        //if the row was full - remove it and slide above stuff down
        if (full) {
          rowsCleared++;
          cleared++;
          //for each column
          for (int c = 0; c < COLS; c++) {

            //slide down all bricks
            for (int i = r; i < top[c]; i++) {
              field[i][c] = field[i + 1][c];
            }
            //lower the top
            top[c]--;
            while (top[c] >= 1 && field[top[c] - 1][c] == 0)
              top[c]--;
          }
        }
      }

      return new MoveResult(field, top, turn, false, rowsCleared);
    }

    public int[][] getField() {
      return field;
    }

    public int[] getTop() {
      return top;
    }

    public int getRowsCleared() {
      return cleared;
    }

    public int getTurnNumber() {
      return turn;
    }
  }

  /**
   * A class to get all the required heuristics
   */

  private class Heuristics {
    public int[][] field;
    public int[] top;
    public double[] weights;
    public int rowsCleared;

    public Heuristics(double[] w) {
      this.weights = w;
    }

    public double score(State s) {
      this.field = s.getField();
      this.top = s.getTop();
      this.rowsCleared = s.getRowsCleared();

      // score: the higher the better, but
      // it can be both positive and negative
      double heuristic = 0; 
      
      int maxHeight = getMaxHeight();
      int totalHeight = getTotalHeight();
      int bumpiness = getBumpiness();
      int[] holesArray = getHoles();
      int numHoles = holesArray[0];
      int maxHoleHeight = holesArray[1];
      int holeDepth = holesArray[2];
      int numHoleRows = holesArray[3];
      int numHoleCols = holesArray[4];
      int concavity = getConcavity();
      
      heuristic -= (double)(weights[0]*maxHeight);
      heuristic -= (double)(weights[1]*totalHeight);
      heuristic -= (double)(weights[2]*bumpiness);
      heuristic -= (double)(weights[3]*numHoles);
      heuristic -= (double)(weights[4]*maxHoleHeight);
      heuristic -= (double)(weights[5]*holeDepth);
      heuristic -= (double)(weights[6]*numHoleRows);
      heuristic -= (double)(weights[7]*numHoleCols);
      heuristic -= (double)(weights[8]*concavity);
      heuristic += (double)(weights[9]*(float)rowsCleared);
      
      return heuristic;  
    }
    
    public double score(WorkingState s) {
      this.field = s.getField();
      this.top = s.getTop();
      this.rowsCleared = s.getRowsCleared();

      // score: the higher the better, but
      // it can be both positive and negative
      double heuristic = 0; 
      
      int maxHeight = getMaxHeight();
      int totalHeight = getTotalHeight();
      int bumpiness = getBumpiness();
      int[] holesArray = getHoles();
      int numHoles = holesArray[0];
      int maxHoleHeight = holesArray[1];
      int holeDepth = holesArray[2];
      int numHoleRows = holesArray[3];
      int numHoleCols = holesArray[4];
      int concavity = getConcavity();
      
      heuristic -= (double)(weights[0]*maxHeight);
      heuristic -= (double)(weights[1]*totalHeight);
      heuristic -= (double)(weights[2]*bumpiness);
      heuristic -= (double)(weights[3]*numHoles);
      heuristic -= (double)(weights[4]*maxHoleHeight);
      heuristic -= (double)(weights[5]*holeDepth);
      heuristic -= (double)(weights[6]*numHoleRows);
      heuristic -= (double)(weights[7]*numHoleCols);
      heuristic -= (double)(weights[8]*concavity);
      heuristic += (double)(weights[9]*(float)rowsCleared);
      
      return heuristic;  
    }

    public int getMaxHeight() {
      return Arrays.stream(top).max().getAsInt();
    }

    public int getTotalHeight() {
      return Arrays.stream(top).sum();
    }

    public int getConcavity() {
      int concavity = 0;
      for (int j = 0; j < 4; j++) {
        concavity += top[4] - top[j];
      }
      for (int j = 6; j < COLS; j++) {
        concavity += top[5] - top[j];
      }
            
      return concavity;
    }

    public int[] getHoles() {
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
            if (top[j] > maxHoleHeight) {
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

      int[] result = {holes, maxHoleHeight, totalHoleDepth, IntStream.of(rowHoles).sum(), IntStream.of(colHoles).sum()};
      return result;
    }

    public int getBumpiness() {
      int total = 0;
      for (int i = 0; i < top.length - 1; i++) {
        total += Math.abs(top[i] - top[i + 1]);
      }

      return total;
    }
  }

  private void setWeights(double[] weights) {
    this.weights = weights;
  }

  public static int run(double[] weights) {
    State s = new State();
    pOrients = State.getpOrients();
    pWidth = State.getpWidth();
    pBottom = State.getpBottom();
    pHeight = State.getpHeight();
    pTop = State.getpTop();

    // generate legal moves - done globally for use in ldfs
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

    ForkJoinPool concurrentExecutor = new ForkJoinPool();
    PlayerSkeleton p = new PlayerSkeleton(concurrentExecutor);
    p.setWeights(weights);
    int moves = 0;

    int pickedMove=0;

    try {
        while (!s.hasLost()) {
          pickedMove = p.pickMove(s, s.legalMoves());
            s.makeMove(pickedMove);
            moves++;
        }
    } catch (ArrayIndexOutOfBoundsException e) {
        e.printStackTrace();
        System.out.print("Fail. Weights: " );
        for (int i = 0; i < weights.length; i++) {
            System.out.print(weights[i] + ", ");
        }
        System.out.println();
        System.out.println("Picked Move: " + pickedMove);
      try {
        serializeDataOut(s);
      } catch (IOException e1) {
        e1.printStackTrace();
      }
      return 0;
    }
    return s.getRowsCleared();
  }

  /**
   *
   *
   *
   * Concurrency Part
   *
   *
   *
   */


  /**
   * Functional Interfaces for concurrent evaluation
   */

  public static class ConcurrentExecutor {

    private final ForkJoinPool forkJoinPool;

    public ConcurrentExecutor(ForkJoinPool forkJoinPool) {
      this.forkJoinPool = forkJoinPool;
    }

    public <Src, Dst> void map(Mapper<Src, Dst> mapper, Iterable<Src> inputs, Collection<Dst> outputs) {
      forkJoinPool.invoke(new MapTask<Src, Dst>(mapper, inputs, outputs));
    }

    public <SrcT, IntT, DstT> DstT reduce(Mapper<SrcT, IntT> mapper, Reducer<IntT, DstT> reducer,
                                          Iterable<SrcT> inputs) {

      return forkJoinPool.invoke(new ReduceTask<SrcT, IntT, DstT>(mapper, reducer, inputs));
    }

  }

  public interface Mapper<SrcT, DstT> {
    public DstT evaluate(SrcT input);
  }

  public interface Reducer<SrcT, DstT> {
    public DstT execute(Iterable<SrcT> inputs);
  }

  public static class MapTask<SrcT, DstT> extends ForkJoinTask<Void> {
    public MapTask(Mapper<SrcT, DstT> mapper, Iterable<SrcT> inputs, Collection<DstT> outputs) {
      this.mapper = mapper;
      this.inputs = inputs;
      this.outputs = outputs;
    }

    @Override
    protected boolean exec() {
      ArrayList<ForkJoinTask<DstT>> applyTasks = new ArrayList<ForkJoinTask<DstT>>();

      for (SrcT input : inputs) {
        applyTasks.add(new EvaluateTask(input));
      }

      invokeAll(applyTasks);

      for (ForkJoinTask<DstT> applyTask : applyTasks) {
        outputs.add(applyTask.join());
      }

      return true;
    }

    @Override
    public Void getRawResult() {
      return null;
    }

    @Override
    protected void setRawResult(Void value) {
    }

    private final Mapper<SrcT, DstT> mapper;
    private final Iterable<SrcT> inputs;
    private final Collection<DstT> outputs;
    private static final long serialVersionUID = 1L;

    private class EvaluateTask extends ForkJoinTask<DstT> {
      public EvaluateTask(SrcT input) {
        this.input = input;
      }

      @Override
      protected boolean exec() {
        setRawResult(mapper.evaluate(input));
        return true;
      }

      @Override
      public DstT getRawResult() {
        return output;
      }

      @Override
      protected void setRawResult(DstT value) {
        output = value;
      }

      private final SrcT input;
      private DstT output;

      private static final long serialVersionUID = 1L;
    }
  }

  public static class ReduceTask<SrcT, IntT, DstT> extends ForkJoinTask<DstT> {
    public ReduceTask(Mapper<SrcT, IntT> mapper, Reducer<IntT, DstT> reducer, Iterable<SrcT> inputs) {
      this.inputs = inputs;
      this.mapper = mapper;
      this.reducer = reducer;
    }

    @Override
    protected boolean exec() {
      // map
      ArrayList<IntT> evaluateResults = new ArrayList<IntT>();
      MapTask<SrcT, IntT> mapTask = new MapTask<SrcT, IntT>(mapper, inputs, evaluateResults);
      mapTask.invoke();
      // reduce
      setRawResult(reducer.execute(evaluateResults));

      return true;
    }

    @Override
    public DstT getRawResult() {
      return output;
    }

    @Override
    protected void setRawResult(DstT value) {
      output = value;
    }

    private final Iterable<SrcT> inputs;
    private DstT output = null;
    private Mapper<SrcT, IntT> mapper;
    private Reducer<IntT, DstT> reducer;
    private static final long serialVersionUID = 1L;
  }


  /**
   * Result of a move, returned by WorkingState.move
   */
  public class MoveResult {
    public MoveResult(int field[][], int top[], int turn, boolean lost, int rowsCleared) {
      this.state = new WorkingState(field, top, turn, rowsCleared);
      this.rowsCleared = rowsCleared;
      this.lost = lost;
      this.holeStat = getHoles();
    }

    public WorkingState getState() {
      return state;
    }

    public int getRowsCleared() {
      return rowsCleared;
    }

    public boolean hasLost() {
      return lost;
    }

    public int[] getHoleStat() {
      return holeStat;
    }

    public int[] getHoles() {
      int[] top = this.state.getTop();
      int[][] field = this.state.getField();
      int holes = 0;
      int maxHoleHeight = 0;
      int[] rowHoles = new int[ROWS];
      int[] colHoles = new int[COLS];
      int holeMultiplier = 1; // holes on top of holes are really bad
      int holeDepth = 1; // total number of blocks above holes

      for (int j = 0; j < field[0].length; j++) {
        holeMultiplier = 1;
        for (int i = top[j] - 2; i >= 0; i--) {
          if (field[i][j] == 0) {
            holes += holeMultiplier;
            holeMultiplier++;
            if (top[j] > maxHoleHeight) {
              maxHoleHeight = top[j];
            }
            rowHoles[i] = 1;
            colHoles[j] = 1;
            continue;
          }
          holeDepth++;
        }
      }
      // System.out.println(holes);
      int[] result = { holes, maxHoleHeight, holeDepth, IntStream.of(rowHoles).sum(), IntStream.of(colHoles).sum() };
      return result;
    }

    private final int rowsCleared;
    private final boolean lost;
    private final WorkingState state;
    private final int[] holeStat;
  }

  class Possibility implements Comparable<Possibility> {
    public int idx, piece;
    public int[][] legalMoves;
    public double score;
    public WorkingState state;

    public Possibility(int piece, int idx, int[][] legalMoves) {
      this.piece = piece;
      this.idx = idx;
      this.legalMoves = legalMoves;
      score = -Constants.MAX_HEURISTICS;
    }

    public void defineWS(WorkingState original) {
      state = new WorkingState(piece, legalMoves[idx][ORIENT], legalMoves[idx][SLOT], original);
    }

    public void setScore(double score) {
      this.score = score;
    }

    @Override
    public int compareTo(Possibility pos) {
      // System.out.println("Comparing " + this.score + " to " + pos.score);
      if (this.score > pos.score) {
        return 1;
      } else if (this.score < pos.score) {
        return -1;  
      } else {
        return 0;  
      }
      // return (int) (this.score - pos.score);
    }
  }

  //save state for debug
  public static void serializeDataOut(State ish)throws IOException {
    String fileName= "state.txt";
    FileOutputStream fos = new FileOutputStream(fileName);
    ObjectOutputStream oos = new ObjectOutputStream(fos);
    oos.writeObject(ish);
    oos.close();
  }

  public static State serializeDataIn() throws IOException, ClassNotFoundException{
    String fileName= "Test.txt";
    FileInputStream fin = new FileInputStream(fileName);
    ObjectInputStream ois = new ObjectInputStream(fin);
    State state= (State) ois.readObject();
    ois.close();
    return state;
  }

  private static class Constants {
    public static int defaultGeneLength = 10;
    
    /* GA parameters */
    static double crossoverRate = 0.5;
    static  double mutationRate = 0.05;
    public static double NUM_OFFSPRING = 0.5;
    static double tournamentSize = 0.5;
    static int POPULATION_SIZE = 50;
    public static int MAX_LOST_GENERATION = 20;
    public static double maxInitialWeight = 2.0;
    
    // Number of runs averaged to get the fitness
    static int NUM_RUNS = 10;
    public static int MAX_ITERATIONS = 100;
    public static int PSO_ITERATIONS = 5;
    public static int MAX_MOVES = 500;
    public static double[] defaultWeights = {0.9252657084322371, 1.791060991167827, 1.6301289861264825, 1.221250538049939,
    0.1050588365382541, 0.765367780014832, 0.8630601768977213, 1.0040737814518448,
    0.08598196212509279, 1.7338714622965667};
    public static double MAX_HEURISTICS = Double.MAX_VALUE;
    public static double MAX_INIT_WEIGHT = 2.0;
    
    public static double CARRY_OVER_RATE = 0.5;
  }

}
