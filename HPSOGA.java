public class HPSOGA {

    private Population myPop;
    private GeneticAlgorithm ga;
    private ParticleSwarmOptimization pso;

    public void run(){
        myPop =  new Population(Constants.POPULATION_SIZE, true);
        ga = new GeneticAlgorithm();
        for(int i = 0 ; i< Constants.MAX_ITERATIONS; i++) {
            pso = new ParticleSwarmOptimization(myPop);
            myPop.setChromosomes(pso.run());
            myPop = ga.run(myPop);
        }
    }
    public static void main(String[] args){
        HPSOGA optimize = new HPSOGA();
        optimize.run();
    }
}
