public class Constants {
    public static int defaultGeneLength = 10;

    /* GA parameters */
    static double crossoverRate = 0.5;
    static  double mutationRate = 0.05;
    public static double NUM_OFFSPRING = 0.5;
    static double tournamentSize = 0.5;
    static int POPULATION_SIZE = 50;
    public static int MAX_LOST_GENERATION = 20;
    public static double maxInitialWeight = 2.0;

    // Number of runs averaged to get the fitness
    static int NUM_RUNS = 10;
    public static int MAX_ITERATIONS = 100;
    public static int PSO_ITERATIONS = 5;
    public static int MAX_MOVES = 500;
    public static double[] defaultWeights = {0.9252657084322371, 1.791060991167827, 1.6301289861264825, 1.221250538049939, 0.1050588365382541, 0.765367780014832,
            0.8630601768977213, 1.0040737814518448, 0.08598196212509279, 1.7338714622965667};
    public static double MAX_HEURISTICS = Double.MAX_VALUE;
    public static double MAX_INIT_WEIGHT = 2.0;

    public static double CARRY_OVER_RATE = 0.5;
}
