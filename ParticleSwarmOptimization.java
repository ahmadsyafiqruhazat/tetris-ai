import java.util.ArrayList;

public class ParticleSwarmOptimization {

    private static final double[] MAX = {20.0, 20.0, 20.0, 20.0};
    private static final double[] MIN = {-20.0, -20.0, -20.0, -20.0};
    private static final double INERTIA_WEIGHT = 1.0;
    private static final double COGNITIVE_WEIGHT = 2.0;
    private static final double SOCIAL_WEIGHT = 2.5;
    private static final double[] gBest = {0.0, 0.0, 0.0, 0.0};
    private static final int gBestFitness = 0;

    private static final int NUM_ITERATIONS = 10;

    private Particle[] particles;

    public void printParticleData(int n) {
        double[] position = particles[n].getPosition();
        double[] velocity = particles[n].getVelocity();
        int fitness = particles[n].getFitness();
        double[] pBest = particles[n].getPBest();
        int pBestFitness = particles[n].getPBestFitness();

        for (int i = 0; i < position.length; i++) {
            System.out.print(position[i] + " ");
        }
        System.out.println();

        for (int i = 0; i < velocity.length; i++) {
            System.out.print(velocity[i] + " ");
        }
        System.out.println();

        System.out.println(fitness);

        for (int i = 0; i < pBest.length; i++) {
            System.out.print(pBest[i] + " ");
        }
        System.out.println();

        System.out.println(pBestFitness);
    }

    public void printSwarmData() {
        for (int i = 0; i < particles.length; i++) {
            printParticleData(i);
            System.out.println("---");
        }

        for (int i = 0; i < Particle.gBest.length; i++) {
            System.out.print(Particle.gBest[i] + " ");
        }
        System.out.println();
        System.out.println(Particle.gBestFitness);

        System.out.println("===");
    }

    public void runOneIteration() {
        for (int i = 0; i < particles.length; i++) {
            particles[i].update();
        }
    }

    public void runAndPrintIterations(int num) {
        for (int i = 0; i < num; i++) {
            runOneIteration();
            printSwarmData();
        }
    }

    public ParticleSwarmOptimization(Population p) {
        Particle.MAX = MAX;
        Particle.MIN = MIN;
        Particle.INERTIA_WEIGHT = INERTIA_WEIGHT;
        Particle.COGNITIVE_WEIGHT = COGNITIVE_WEIGHT;
        Particle.SOCIAL_WEIGHT = SOCIAL_WEIGHT;
        Particle.gBest = gBest;
        Particle.gBestFitness = gBestFitness;
        particles = new Particle[Constants.POPULATION_SIZE];
        ArrayList<Chromosome> chromosomes = p.getChromosomes();
        for (int i = 0; i < Constants.POPULATION_SIZE; i++)
            particles[i] = new Particle(chromosomes.get(i));
    }

    public Population run() {
        runAndPrintIterations(NUM_ITERATIONS);
        Population newPop = new Population(Constants.POPULATION_SIZE,false);
        ArrayList<Chromosome> chromosomes = newPop.getChromosomes();
        for (Particle par: particles) {
            chromosomes.add(new Chromosome(par.getPosition(),par.getFitness()));
        }
        return newPop;
    }
}
