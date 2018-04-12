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
    public static double[] defaultWeights = {27.669933366449918, 14.00217351467533, 27.48799087578665, 33.76805261769937, 2.2681962577932335,
            -7.6594537527562325, 54.856399581119064, 58.405733825284806, 1.3348295886485575, 37.1849265591512};
}
