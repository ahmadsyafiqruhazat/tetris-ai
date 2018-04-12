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
    public static double[] defaultWeights = {174.08218067747248, 143.76296136778797, 16.955444455838972, 48.363970823774366, 185.8813779118484,
            1.0570908473816685, 129.5215259074787, 158.89669035100292, 110.8373898333615, 85.85409518337406};
}
