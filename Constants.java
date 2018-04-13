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
    public static int MAX_MOVES = 5000;
    public static double[] defaultWeights = {-0.8689324756939085, 2.642891949338086, 1.5715951649364008, 0.8412177342456232, 2.2777232167146035,
            1.3757830453846938, 2.983452152207513, 2.6082342391276456, 0.10293581884694611, 0.24836896652013385};
}
