public class Constants {
    public static int defaultGeneLength = 10;

    /* GA parameters */
    static double crossoverRate = 0.5;
    static  double mutationRate = 0.05;
    public static double NUM_OFFSPRING = 0.3;
    static double tournamentSize = 0.1;
    static int POPULATION_SIZE = 20;
    public static int MAX_LOST_GENERATION = 20;
    public static double maxInitialWeight = 2.0;

    // Number of runs averaged to get the fitness
    static int NUM_RUNS = 20;
    public static int MAX_ITERATIONS = 100;
    public static int PSO_ITERATIONS = 5;
    public static int MAX_MOVES = 5000;
    public static double[] defaultWeights = {1,1,1,1,1,1,1,1,1,1};
    public static double MAX_HEURISTICS = Double.MAX_VALUE;
    public static double MAX_INIT_WEIGHT = 2.0;
}
