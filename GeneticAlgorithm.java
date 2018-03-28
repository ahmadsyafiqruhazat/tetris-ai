import javafx.util.Pair;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

public class GeneticAlgorithm {


    /* Public methods */

    // Evolve a population
    public static Population evolvePopulation(Population pop) {

        // Loop over the population size and create new chromosomes with
        // crossover

        for (int i = 0; i <(int)(Constants.NUM_OFFSPRING*Constants.POPULATION_SIZE); i++) {
            Pair<Particle,Integer> indiv1 = tournamentSelection(pop);
            Pair<Particle,Integer> indiv2 = tournamentSelection(pop);
            Particle[] newIndiv = crossover(indiv1.getKey(), indiv2.getKey());
            pop.saveIndividual(newIndiv[0],indiv1.getValue());
            pop.saveIndividual(newIndiv[1],indiv2.getValue());

        }

        // Mutate population
        for (int i = 0; i < pop.size(); i++) {
            mutate(pop.getIndividual(i));
        }

        return pop;
    }

    // Crossover chromosomes

    private static Particle[] crossover(Particle indiv1, Particle indiv2) {
        Particle newSol1 = new Particle();
        Particle newSol2 = new Particle();

        double[] newGene1 = newSol1.getGenes();
        double[] newGene2 = newSol2.getGenes();
        int length = indiv1.size();

        Random random = new Random();
        if (Math.random() <= Constants.crossoverRate) {
            int crossoverPoint = random.nextInt(length);
            System.arraycopy(indiv1.getGenes(), 0, newGene1, 0, crossoverPoint);
            System.arraycopy(indiv2.getGenes(), 0, newGene2, 0, crossoverPoint);
            System.arraycopy(indiv1.getGenes(), crossoverPoint, newGene1, crossoverPoint, length-crossoverPoint);
            System.arraycopy(indiv1.getGenes(), crossoverPoint, newGene1, crossoverPoint, length-crossoverPoint);

        } else {
            return new Particle[] { indiv1,indiv2};
        }

        return new Particle[] { newSol1,newSol2};
    }

    // Mutate an individual
    private static void mutate(Particle indiv) {
        // Loop through genes
        for (int i = 0; i < indiv.size(); i++) {
            if (Math.random() <= Constants.mutationRate) {
                // Create random gene
                Random random = new Random();
                double gene = random.nextDouble() * 0.4 - 0.2;
                indiv.mutateGene(i, gene);
            }
        }
    }

    // Select chromosomes for crossover
    private static Pair<Particle,Integer> tournamentSelection(Population pop) {
        // Create a tournament population
        Population tournament = new Population((int)(Constants.tournamentSize * Constants.POPULATION_SIZE), false);
        // For each place in the tournament get a random individual
        HashMap<Integer,Integer> pos = new HashMap<>();
        for (int i = 0; i < (int)(Constants.tournamentSize * Constants.POPULATION_SIZE); i++) {
            int randomId = (int) (Math.random() * pop.size());
            tournament.saveIndividual(pop.getIndividual(randomId));
            pos.put(i,randomId);
        }
        // Get the fittest
        Pair<Particle,Integer>fittest = tournament.getFittest();
        return new Pair<Particle,Integer>(fittest.getKey(), pos.get(fittest.getValue()));
    }

    public Population run( Population myPop) {

        int generationCount = 0;
        int lostGeneration = 0;
        int maxFitness = 0;


   //     while (true) {
//            System.out.println("getFitness: " +myPop.getFittest().getFitness());
            if(maxFitness > myPop.getFittest().getKey().getFitness()){
                lostGeneration++;

            } else {
                maxFitness = myPop.getFittest().getKey().getFitness();
                lostGeneration = 0;
            }
//            if(lostGeneration> Constants.MAX_LOST_GENERATION){
//                break;
//            }
            generationCount++;
            System.out.println("Generation: " + generationCount + " Fittest: " + myPop.getFittest().getKey().getFitness() + " Max " + maxFitness);
            myPop = evolvePopulation(myPop);
//            System.out.println("Lost " + lostGeneration + " Max: "+  maxFitness);
//        }
        System.out.println("Generation: " + generationCount);
        System.out.println("Genes:");
        System.out.println(Arrays.toString(myPop.getFittest().getKey().getGenes()));
        System.out.println("///////////////////");
        return myPop;

    }
}