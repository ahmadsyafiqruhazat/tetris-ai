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
    public static void main(String[] args){
        HPSOGA optimize = new HPSOGA();
        optimize.run();
    }
}
