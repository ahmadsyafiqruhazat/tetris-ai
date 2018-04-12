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
    public static double[] defaultWeights = {1209.4098328279626, 262.18896905458473, 2353.7774465963953, 1468.0480939912602, 2149.5993959051384,
            698.3970222970199, 2488.5131794007, 585.3727557995206, -12537.76701300828, -35018.99578747197};
}
