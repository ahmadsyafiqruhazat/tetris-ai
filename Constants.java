public class Constants {
    public static int defaultGeneLength = 10;

    /* GA parameters */
    static final double crossoverRate = 0.5;
    static final double mutationRate = 0.05;
    public static final double NUM_OFFSPRING = 0.3;
    static final double tournamentSize = 0.1;
    static final int POPULATION_SIZE = 20;
    public static final int MAX_LOST_GENERATION = 20;

    // Number of runs averaged to get the fitness
    static final int NUM_RUNS = 5;
    public static int MAX_ITERATIONS = 100;
    public static int PSO_ITERATIONS = 10;
    public static int MAX_MOVES = 500;
    public static double[] defaultWeights = {11.774712736249061, 18.366360873896287, 3.9243449363538154, 16.159094029984907, 9.963566099083302,
            -16.670304858956435, 7.8849045920956815, 9.724702929910661, -13.75472152183216, 17.357597475341443};

}
