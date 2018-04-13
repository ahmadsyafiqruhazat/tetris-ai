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
    public static double[] defaultWeights = {-0.9995664867211291, 2.9009306089442464, 3.5573142495905765, 6.820916023940407, 0.621789553654263,
            3.4564458404119422, 2.233928759382568, 2.068180502279361, 0.16984522585421719, 12.951450753267725};
}
