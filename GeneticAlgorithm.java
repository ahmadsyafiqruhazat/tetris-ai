import java.util.Collections;
import java.util.Random;

public class GeneticAlgorithm {

    public static final int POPULATION_SIZE = 100;
    /* GA parameters */
    private static final double crossoverRate = 0.6;
    private static final double mutationRate = 0.05;
    private static final int tournamentSize = 5;
    public static final int MAX_LOST_GENERATION = 5;
    public static final double NUM_OFFSPRING = 0.3;


    /* Public methods */

    // Evolve a population
    public static Population evolvePopulation(Population pop) {
        Population newPopulation = new Population(pop.size(), false);

        // Crossover population
        // Loop over the population size and create new chromosomes with
        // crossover

        for (int i = 0 ; i <(int)(NUM_OFFSPRING*pop.size()); i++) {
            Chromosome indiv1 = tournamentSelection(pop);
            Chromosome indiv2 = tournamentSelection(pop);
            Chromosome[] newIndiv = crossover(indiv1, indiv2);
            newPopulation.saveIndividual(newIndiv[0]);
            newPopulation.saveIndividual(newIndiv[1]);

        }

        for(int i = 0; i <pop.size()-(int)(NUM_OFFSPRING*pop.size()); i++){
            newPopulation.saveIndividual(pop.getIndividual(i));
        }

        // Mutate population
        for (int i = 0; i < newPopulation.size(); i++) {
            mutate(newPopulation.getIndividual(i));
        }

        return newPopulation;
    }

    // Crossover chromosomes
    private static Chromosome[] crossover(Chromosome indiv1, Chromosome indiv2) {
        Chromosome newSol1 = new Chromosome();
        Chromosome newSol2 = new Chromosome();

        double[] newGene1 = newSol1.getGenes();
        double[] newGene2 = newSol2.getGenes();
        int length = indiv1.size();

        Random random = new Random();
        if (Math.random() <= crossoverRate) {
            int crossoverPoint = random.nextInt(length);
            System.arraycopy(indiv1.getGenes(), 0, newGene1, 0, crossoverPoint);
            System.arraycopy(indiv2.getGenes(), 0, newGene2, 0, crossoverPoint);
            System.arraycopy(indiv1.getGenes(), crossoverPoint, newGene1, crossoverPoint, length-crossoverPoint);
            System.arraycopy(indiv1.getGenes(), crossoverPoint, newGene1, crossoverPoint, length-crossoverPoint);

        } else {
            return new Chromosome[] { indiv1,indiv2};
        }
    return new Chromosome[] { newSol1,newSol2};
    }

    // Mutate an individual
    private static void mutate(Chromosome indiv) {
        // Loop through genes
        for (int i = 0; i < indiv.size(); i++) {
            if (Math.random() <= mutationRate) {
                // Create random gene
                Random random = new Random();
                double gene = random.nextDouble() * 1000.0f;
                indiv.setGene(i, gene);
            }
        }
    }

    // Select chromosomes for crossover
    private static Chromosome tournamentSelection(Population pop) {
        // Create a tournament population
        Population tournament = new Population(tournamentSize, false);
        // For each place in the tournament get a random individual
        for (int i = 0; i < tournamentSize; i++) {
            int randomId = (int) (Math.random() * pop.size());
            tournament.saveIndividual(pop.getIndividual(randomId));
        }
        // Get the fittest
        Chromosome fittest = tournament.getFittest();
        return fittest;
    }

    public static void main(String[] args) {

        Population myPop = new Population(POPULATION_SIZE, true);

        int generationCount = 0;
        int lostGeneration = 0;
        int maxFitness = 0;


        while (true) {
//            System.out.println("getFitness: " +myPop.getFittest().getFitness());
            if(maxFitness > myPop.getFittest().getFitness()){
                lostGeneration++;

            } else {
                maxFitness = myPop.getFittest().getFitness();
                lostGeneration = 0;
            }
            if(lostGeneration> MAX_LOST_GENERATION){
                break;
            }
            generationCount++;
            System.out.println("Generation: " + generationCount + " Fittest: " + myPop.getFittest().getFitness() + " Max " + maxFitness);
            myPop = evolvePopulation(myPop);
//            System.out.println("Lost " + lostGeneration + " Max: "+  maxFitness);
        }
        System.out.println("Generation: " + generationCount);
        System.out.println("Genes:");
        System.out.println(myPop.getFittest());

    }
}