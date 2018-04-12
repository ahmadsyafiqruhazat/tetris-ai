public class Constants {
    public static int defaultGeneLength = 10;

    /* GA parameters */
    static final double crossoverRate = 0.5;
    static final double mutationRate = 0.05;
    public static final double NUM_OFFSPRING = 0.3;
    static final double tournamentSize = 0.1;
    static final int POPULATION_SIZE = 500;
    public static final int MAX_LOST_GENERATION = 20;

    // Number of runs averaged to get the fitness
    static final int NUM_RUNS = 5;
    public static int MAX_ITERATIONS = 1000;
    public static int PSO_ITERATIONS = 10;
    public static int MAX_MOVES = 500;
    public static double[] defaultWeights = {11.275150219061857, 0.0, -0.06004746567662789, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};

}
