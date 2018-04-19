public class HPSOGA {

    private Population myPop;
    private GeneticAlgorithm ga;
    private ParticleSwarmOptimization pso;

    public void run(){
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
        System.out.println("POPULATION_SIZE: " + POPULATION_SIZE);
        System.out.println("tournamentSize: " + tournamentSize);
        System.out.println("maxInitialWeight: " + maxInitialWeight);
        System.out.println("PSO_ITERATIONS: " + PSO_ITERATIONS);
        System.out.println("NUM_RUNS: " + NUM_RUNS);
        System.out.println("MAX_MOVES: " + MAX_MOVES);
        System.out.println("MAX_INIT_WEIGHT: " + MAX_INIT_WEIGHT);
    }
    public static void main(String[] args){
        HPSOGA optimize = new HPSOGA();
        optimize.run();
    }
}
