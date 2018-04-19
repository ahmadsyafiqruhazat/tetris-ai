public class Constants {
    public static int defaultGeneLength = 10;

    /* GA parameters */
    static double crossoverRate = 0.5;
    static  double mutationRate = 0.05;
    public static double NUM_OFFSPRING = 0.5;
    static double tournamentSize = 0.5;
    static int POPULATION_SIZE = 100;
    public static int MAX_LOST_GENERATION = 20;
    public static double maxInitialWeight = 2.0;

    // Number of runs averaged to get the fitness
    static int NUM_RUNS = 50;
    public static int MAX_ITERATIONS = 100;
    public static int PSO_ITERATIONS = 100000000;
    public static int MAX_MOVES = 500;
    public static double[] defaultWeights = {807.9475181444632, 11.887473709241078, 687.3817025913254, 271.703308418665, -86.14077487670852, 856.5458745173605, 640.6716881259781, 125.28221937611306, 144.86750211592832, 684.1768557823555};
    public static double MAX_HEURISTICS = Double.MAX_VALUE;
    public static double MAX_INIT_WEIGHT = 2.0;
}
