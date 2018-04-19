public class HPSOGA {

    private Population myPop;
    private GeneticAlgorithm ga;
    private ParticleSwarmOptimization pso;

    public void run(){
        printCosntants();
        myPop =  new Population(Constants.POPULATION_SIZE, true);
        ga = new GeneticAlgorithm();
        for(int i = 0 ; i< Constants.MAX_ITERATIONS; i++) {
            System.out.println("**********************");
            System.out.println("Iteration: " + i);
            System.out.println("Running PSO");
            pso = new ParticleSwarmOptimization(myPop, i);
            myPop.setChromosomes(pso.run());
            System.out.println("Running GA");
            myPop = ga.run(myPop, i);
            myPop.reset();
        }
    }

    private void printCosntants() {

        System.out.println("Constants: ==================");
        System.out.println("POPULATION_SIZE: " + Constants.POPULATION_SIZE);
        System.out.println("tournamentSize: " + Constants.tournamentSize);
        System.out.println("maxInitialWeight: " + Constants.maxInitialWeight);
        System.out.println("PSO_ITERATIONS: " + Constants.PSO_ITERATIONS);
        System.out.println("NUM_RUNS: " + Constants.NUM_RUNS);
        System.out.println("MAX_MOVES: " + Constants.MAX_MOVES);
        System.out.println("MAX_INIT_WEIGHT: " + Constants.MAX_INIT_WEIGHT);
    }
    public static void main(String[] args){
        HPSOGA optimize = new HPSOGA();
        optimize.run();
    }
}
