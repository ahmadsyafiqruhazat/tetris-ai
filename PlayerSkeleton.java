import java.lang.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

import java.util.stream.IntStream;

import com.sun.rowset.internal.Row;

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
  private double[] weights;
  private double[] nextWeights;

  // ForkJoinPool for concurrent execution
  private ForkJoinPool forkJoinExecutor;
  private ConcurrentExecutor concurrentExecutor;
  private MoveEvaluator evaluator;

  private CopyOnWriteArrayList<Move> possibleMoves = new CopyOnWriteArrayList<>();
//  private ArrayList<Move> possibleMoves = new ArrayList<>();

  public static void main(String[] args) {
      State s = new State();
      pOrients = s.getpOrients();
      pWidth = s.getpWidth();
      pBottom = s.getpBottom();
      pHeight = s.getpHeight();
      pTop = s.getpTop();

      new TFrame(s);

      ForkJoinPool forkJoinPool = new ForkJoinPool();
      PlayerSkeleton p = new PlayerSkeleton(forkJoinPool);
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
      System.out.println("You have completed " + s.getRowsCleared() + " rows.");

  }
    //implement this function to have a working system
    public int pickMove(State s, int[][] legalMoves) {
        int nextPiece = s.getNextPiece();
        WorkingState currentState = new WorkingState(s);
        return pickMove(currentState, nextPiece, legalMoves);
    }

  public int pickMove(WorkingState state, int nextPiece, int[][] legalMoves) {
      possibleMoves.clear();
      for (int i = 0; i < legalMoves.length; i++) {
          int orientation = legalMoves[i][ORIENT];
          int position = legalMoves[i][SLOT];
          possibleMoves.add(new Move(state, i, nextPiece, orientation, position));
      }
//      System.out.println("Num of possible moves: " + possibleMoves.size());
      return concurrentExecutor.execute(EVAL_MOVE_FUNC, PICK_MOVE_FUNC, possibleMoves);
  }

  public PlayerSkeleton(ForkJoinPool forkJoinPool) {
    this.forkJoinExecutor = forkJoinPool;
    this.concurrentExecutor = new ConcurrentExecutor(forkJoinPool);
    double[] newWeights = {1.0f, 1.0f, 1.0f, 2.0f, 1.0f, 1/3, 1.0f, 1.0f, 1/5, 1.0f};
    double[] newNextWeights = {1.0f, 1.0f, 1.0f, 2.0f, 1.0f, 1/3, 1.0f, 1.0f, 1/5, 1.0f};
    this.weights = newWeights;
    this.nextWeights = newNextWeights;
    this.evaluator = new WeightedSumEvaluator(EVALUATORS, weights, nextWeights);
  }

  public PlayerSkeleton(ForkJoinPool forkJoinPool, double[] weights, double[] nextWeights) {
      this.concurrentExecutor = new ConcurrentExecutor(forkJoinPool);
      this.evaluator = new WeightedSumEvaluator(EVALUATORS, weights, nextWeights);
  }

  public static final MoveEvaluator[] EVALUATORS;
  static {
        ArrayList<MoveEvaluator> evaluators = new ArrayList<MoveEvaluator>();
        evaluators.add(new MaxHeight());
        evaluators.add(new TotalHeight());
        evaluators.add(new Bumpiness());
        evaluators.add(new NumHoles());
        evaluators.add(new MaxHoleHeight());
        evaluators.add(new HoleDepth());
        evaluators.add(new NumHoleRows());
        evaluators.add(new NumHoleCols());
        evaluators.add(new Concavity());
        evaluators.add(new RowsCleared());

        EVALUATORS = evaluators.toArray(new MoveEvaluator[evaluators.size()]);
  }

  private final Evaluator<Move, EvaluationResult> EVAL_MOVE_FUNC = new Evaluator<Move, EvaluationResult>() {
      @Override
      public EvaluationResult evaluate(Move move) {
          WorkingState state = move.getState();
          long startTime = System.nanoTime();
          MoveResult moveResult = state.makeSpecificMove(move.getPiece(),
                  move.getOrientation(), move.getPosition());
          long midTime = System.nanoTime();
          System.out.println("Make specific move: " + (midTime - startTime));
//          float nextScore = getNextHeuristic(moveResult.getState(), nextWeights);
          float score = evaluator.evaluate(moveResult);
          long intTime = System.nanoTime();

          System.out.println("Evaluate: " + (intTime - midTime));
          int[][] legalMoves = state.legalMoves();
          int nextPiece = state.getNextPiece();
          for (int moveIndex = 0; moveIndex < legalMoves.length; ++moveIndex) {
              int orientation = legalMoves[moveIndex][ORIENT];
              int position = legalMoves[moveIndex][SLOT];
              possibleMoves.add(new Move(state, moveIndex, nextPiece,
                      orientation, position));
          }
          long intTime2 = System.nanoTime();

          System.out.println("Find possible moves: " + (intTime2 - intTime));
//          ConcurrentExecutor newExecutor = new ConcurrentExecutor(ForkJoinPool.commonPool());
          score += concurrentExecutor.execute(EVAL_FURTHER_MOVE_FUNC, PROBE_MOVE_FUNC, possibleMoves);
          long endTime = System.nanoTime();

          System.out.println("Next move: " + (endTime - intTime2));
//          System.out.println("evaluating move: Piece - " + move.getPiece() + " Position - " + move.getPosition() + " " +
//                  "Orientation " +
//                  "- " + move.getOrientation() );
          return new EvaluationResult(move.getIndex(), score);
      }
  };


    private final Evaluator<Move, EvaluationResult> EVAL_FURTHER_MOVE_FUNC = new Evaluator<Move, EvaluationResult>() {
        @Override
        public EvaluationResult evaluate(Move move) {
            WorkingState state = move.getState();
            MoveResult moveResult = state.makeSpecificMove(move.getPiece(),
                    move.getOrientation(), move.getPosition());
            float score = evaluator.evaluate(moveResult);
            return new EvaluationResult(move.getIndex(), score);
        }
    };


  private static final Executor<EvaluationResult, Integer> PICK_MOVE_FUNC = new Executor<EvaluationResult,
            Integer>() {
      @Override
      public Integer execute(Iterable<EvaluationResult> results) {
          float maxScore = -Float.MAX_VALUE;
          int move = -1;

          for (EvaluationResult result : results) {
              float score = result.getScore();
//              System.out.println("new score: " + score + " maxScore: " + maxScore);
              if (score > maxScore) {
                  maxScore = score;
                  move = result.getMove();
              }
          }
//          System.out.println("new maxScore: " + maxScore + " move: " + move);

          return move;
      }

  };


    private static final Executor<EvaluationResult, Float> PROBE_MOVE_FUNC = new Executor<EvaluationResult,
            Float>() {
        @Override
        public Float execute(Iterable<EvaluationResult> results) {
            float maxScore = -Float.MAX_VALUE;

            for (EvaluationResult result : results) {
                float score = result.getScore();
                if (score > maxScore) {
                    maxScore = score;
                }
            }

            return maxScore;
        }
    };

  
  public float getNextHeuristic(WorkingState ws, double[] weights){
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
  



  /**
   * A working State for running the game
   */
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

    public MoveResult makeSpecificMove(int nextPiece, int orient, int slot) {

      int[][] field = cloneField(this.field);
      int[] top = cloneTop(this.top);
      int turn = this.turn + 1;

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
        return new MoveResult(field, top, turn, true, 0);
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

      return new MoveResult(field, top, turn, false, rowsCleared);
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


    /**
     *
     *
     * Evaluators for different heuristics
     *
     */


    public static class MaxHeight implements MoveEvaluator {
        @Override
        public Float evaluate(MoveResult result) {
            int[] top = result.getState().getTop();

            return (float) Arrays.stream(top).max().getAsInt();
        }
    }

    public static class TotalHeight implements MoveEvaluator {
        @Override
        public Float evaluate(MoveResult result) {
            int[] top = result.getState().getTop();

            return (float) Arrays.stream(top).sum();
        }
    }

    public static class Bumpiness implements MoveEvaluator {
        @Override
        public Float evaluate(MoveResult result) {
            int[] top = result.getState().getTop();
            int total = 0;
            for (int i = 0; i < top.length - 1; i++) {
                total += Math.abs(top[i] - top[i + 1]);
            }
            return (float) total;
        }
    }

    public static class NumHoles implements MoveEvaluator {
        @Override
        public Float evaluate(MoveResult result) {
            int[] holeStat = result.getHoleStat();
            return (float) holeStat[0];
        }
    }

    public static class MaxHoleHeight implements MoveEvaluator {
        @Override
        public Float evaluate(MoveResult result) {
            int[] holeStat = result.getHoleStat();
            return (float) holeStat[1];
        }
    }

    public static class HoleDepth implements MoveEvaluator {
        @Override
        public Float evaluate(MoveResult result) {
            int[] holeStat = result.getHoleStat();
            return (float) holeStat[2];
        }
    }

    public static class NumHoleRows implements MoveEvaluator {
        @Override
        public Float evaluate(MoveResult result) {
            int[] holeStat = result.getHoleStat();
            return (float) holeStat[3];
        }
    }

    public static class NumHoleCols implements MoveEvaluator {
        @Override
        public Float evaluate(MoveResult result) {
            int[] holeStat = result.getHoleStat();
            return (float) holeStat[4];
        }
    }

    public static class Concavity implements MoveEvaluator {
        @Override
        public Float evaluate(MoveResult result) {
            int[] top = result.getState().getTop();
            int concavity = 0;
            for (int j = 0; j < 4; j++){
                concavity += top[4] - top[j];
            }
            for (int j = 6; j < COLS; j++){
                concavity += top[5] - top[j];
            }
            return (float) concavity;
        }
    }

    public static class RowsCleared implements MoveEvaluator {
        @Override
        public Float evaluate(MoveResult result) {
            return (float) result.getRowsCleared();
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

      heuristic -= (float)(weights[0]*maxHeight);
      heuristic -= (float)(weights[1]*totalHeight);
      heuristic -= (float)(weights[2]*bumpiness);
      heuristic -= (float)(weights[3]*numHoles);
      heuristic -= (float)(weights[4]*maxHoleHeight);
      heuristic -= (float)(weights[5]*holeDepth);
      heuristic -= (float)(weights[6]*numHoleRows);
      heuristic -= (float)(weights[7]*numHoleCols);
      heuristic -= (float)(weights[8]*concavity);
      heuristic += (float)(weights[9]*(float)rowsCleared);

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

      for (int j = 0; j < field[0].length; j++){
        holeMultiplier = 1;
        for (int i = top[j]-2; i >= 0; i--){
          if (field[i][j] == 0){
            holes += holeMultiplier;
            holeMultiplier++;
            if (top[j]>maxHoleHeight){
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
      int[] result = {holes, maxHoleHeight, holeDepth, IntStream.of(rowHoles).sum(), IntStream.of(colHoles).sum()};
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

    ForkJoinPool concurrentExecutor = new ForkJoinPool();
    PlayerSkeleton p = new PlayerSkeleton(concurrentExecutor);
    p.setWeights(weights);
    while(!s.hasLost() && s.getTurnNumber()<501) {
      s.makeMove(p.pickMove(s,s.legalMoves()));
    }
    return s.getRowsCleared();
  }

  public static class ConcurrentExecutor {

    private final ForkJoinPool forkJoinPool;

    public ConcurrentExecutor(ForkJoinPool forkJoinPool) {
      this.forkJoinPool = forkJoinPool;
    }

    public <Src, Dst> void evaluate(Evaluator<Src, Dst> evaluator,
                                    Iterable<Src> inputs, Collection<Dst> outputs) {
      forkJoinPool
              .invoke(new EvaluateTask<Src, Dst>(evaluator, inputs, outputs));
    }

    public <SrcT, IntT, DstT> DstT execute(Evaluator<SrcT, IntT> evaluator,
                                           Executor<IntT, DstT> executor, Iterable<SrcT> inputs) {

      return forkJoinPool.invoke(new ExecuteTask<SrcT, IntT, DstT>(
              evaluator, executor, inputs));
    }

  }

  public interface Evaluator<SrcT, DstT> {
    public DstT evaluate(SrcT input);
  }

  public interface Executor<SrcT, DstT> {
    public DstT execute(Iterable<SrcT> inputs);
  }


  public static class EvaluateTask<SrcT, DstT> extends ForkJoinTask<Void> {
    public EvaluateTask(Evaluator<SrcT, DstT> evaluator, Iterable<SrcT> inputs,
                        Collection<DstT> outputs) {
      this.evaluator = evaluator;
      this.inputs = inputs;
      this.outputs = outputs;
    }

    @Override
    protected boolean exec() {
      ArrayList<ForkJoinTask<DstT>> applyTasks = new ArrayList<ForkJoinTask<DstT>>();

//      Iterator<SrcT> iter = inputs.iterator();
//
//        while (iter.hasNext()) {
//            SrcT input = iter.next();
//
//            applyTasks.add(new ApplyTask(input));
//        }

      for (SrcT input : inputs) {
        applyTasks.add(new ApplyTask(input));
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

    private final Evaluator<SrcT, DstT> evaluator;
    private final Iterable<SrcT> inputs;
    private final Collection<DstT> outputs;
    private static final long serialVersionUID = 1L;

    private class ApplyTask extends ForkJoinTask<DstT> {
      public ApplyTask(SrcT input) {
        this.input = input;
      }

      @Override
      protected boolean exec() {
        setRawResult(evaluator.evaluate(input));
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

  public static class ExecuteTask<SrcT, IntT, DstT> extends
          ForkJoinTask<DstT> {
    public ExecuteTask(Evaluator<SrcT, IntT> evaluator,
                       Executor<IntT, DstT> executor, Iterable<SrcT> inputs) {
      this.inputs = inputs;
      this.evaluator = evaluator;
      this.executor = executor;
    }

    @Override
    protected boolean exec() {
      // evaluate
      ArrayList<IntT> evaluateResults = new ArrayList<IntT>();
      EvaluateTask<SrcT, IntT> evaluateTask = new EvaluateTask<SrcT, IntT>(evaluator,
              inputs, evaluateResults);
      evaluateTask.invoke();
      // execute
      setRawResult(executor.execute(evaluateResults));

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
    private Evaluator<SrcT, IntT> evaluator;
    private Executor<IntT, DstT> executor;
    private static final long serialVersionUID = 1L;
  }

    /**
     * An evaluator which uses a weighted sum of features as score
     */
    public static class WeightedSumEvaluator implements MoveEvaluator {

        private final MoveEvaluator[] evaluators;
        private final double[] weights;
        private final double[] nextWeights;

        public WeightedSumEvaluator(MoveEvaluator[] evaluators, double[] weights, double[] nextWeights) {
            this.evaluators = evaluators;
            this.weights = weights;
            this.nextWeights = nextWeights;
        }

        @Override
        public Float evaluate(MoveResult moveResult) {
            float sum = 0.0f;

            for (int i = 0; i < evaluators.length - 1; ++i) {
                float score = evaluators[i].evaluate(moveResult);
                if (evaluators[i] instanceof RowsCleared) {
                    sum += score * weights[i];
                } else {
                    sum -= score * weights[i];
                }
            }

            return sum;
        }

        public Float evaluateNext(MoveResult moveResult) {
            float sum = 0.0f;

            for (int i = 0; i < evaluators.length; ++i) {
                float score = evaluators[i].evaluate(moveResult);
                sum += score * nextWeights[i];
            }

            return sum;
        }

    }

  /**
   * A common interface for different kind of evaluator
   */
  public interface MoveEvaluator extends Evaluator<MoveResult, Float> {
  }

  private static class Move {
    public Move(WorkingState state, int index, int piece,
                int orientation, int position) {
      this.state = state;
      this.index = index;
      this.piece = piece;
      this.orientation = orientation;
      this.position = position;
    }

    public WorkingState getState() {
      return state;
    }

    public int getIndex() {
      return index;
    }

    public int getPiece() {
      return piece;
    }

    public int getOrientation() {
      return orientation;
    }

    public int getPosition() {
      return position;
    }

    private final WorkingState state;
    private final int index;
    private final int piece;
    private final int orientation;
    private final int position;
  }

  /**
   * A simple class to hold the evaluation result of a move
   */
  public static class EvaluationResult {
    public EvaluationResult(int move, float score) {
      this.move = move;
      this.score = score;
    }

    public int getMove() {
      return move;
    }

    public float getScore() {
      return score;
    }

    private final int move;
    private final float score;
  }

  /**
   * Result of a move, returned by WorkingState.move
   */
  public class MoveResult {
    public MoveResult(int field[][], int top[], int turn, boolean lost,
                      int rowsCleared) {
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

    public int[] getHoleStat() { return holeStat; }

    public int[] getHoles(){
        int[] top = this.state.getTop();
        int[][] field = this.state.getField();
        int holes = 0;
        int maxHoleHeight = 0;
        int[] rowHoles = new int[ROWS];
        int[] colHoles = new int[COLS];
        int holeMultiplier = 1; // holes on top of holes are really bad
        int holeDepth = 1; // total number of blocks above holes

        for (int j = 0; j < field[0].length; j++){
            holeMultiplier = 1;
            for (int i = top[j]-2; i >= 0; i--){
                if (field[i][j] == 0){
                    holes += holeMultiplier;
                    holeMultiplier++;
                    if (top[j]>maxHoleHeight){
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
        int[] result = {holes, maxHoleHeight, holeDepth, IntStream.of(rowHoles).sum(), IntStream.of(colHoles).sum()};
        return result;
    }

    private final int rowsCleared;
    private final boolean lost;
    private final WorkingState state;
    private final int[] holeStat;
  }


}

