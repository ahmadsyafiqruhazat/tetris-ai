import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ForkJoinTask;

public class GeneticAlgorithm {


    /* Public methods */

    // Evolve a population
    public static Population evolvePopulation(Population pop) {
        Population newPopulation = new Population(pop.size(), false);

        // Crossover population
        // Loop over the population size and create new chromosomes with
        // crossover

        Collection<ForkJoinTask<Void>> allTasks = new ArrayList<>();
        for (int i = 0 ; i <(int)(Constants.NUM_OFFSPRING * pop.size()); i++) {
            ForkJoinTask task = new SelectionTask(pop, newPopulation);
            allTasks.add(task);
        }
        ForkJoinTask.invokeAll(allTasks);

        for (ForkJoinTask<Void> task: allTasks) {
            task.join();
        }

        pop.sort();

        for(int i = 0; i < pop.size() - (int)(Constants.NUM_OFFSPRING * pop.size()); i++){
            System.out.println("particle " + i + "'s fitness: " + pop.getIndividual(i).getFitness());
            newPopulation.saveIndividual(pop.getIndividual(i));
        }

        // Mutate population
        for (int i = 0; i < newPopulation.size(); i++) {
            mutate(newPopulation.getIndividual(i));
        }

        return newPopulation;
    }


    static class SelectionTask extends ForkJoinTask<Void> {
        public SelectionTask(Population oldPopulation, Population newPopulation) {
            this.oldPopulation = oldPopulation;
            this.newPopulation = newPopulation;
        }

        @Override
        protected boolean exec() {
            fillWithFittest(oldPopulation, newPopulation);
            return true;
        }

        @Override
        public Void getRawResult() {
            return null;
        }

        @Override
        protected void setRawResult(Void value) {
        }

        private Population oldPopulation;
        private Population newPopulation;

        private static final long serialVersionUID = 1L;
    }

    static Void fillWithFittest(Population oldPopulation, Population newPopulation) {
        Particle indiv1 = tournamentSelection(oldPopulation);
        Particle indiv2 = tournamentSelection(oldPopulation);
        Particle newIndiv = crossover(indiv1, indiv2);
        System.out.println("crossover's fitness: " + newIndiv.getFitness());
//        System.out.println("crossover: " + Arrays.toString(newIndiv.getGenes()));
        newPopulation.saveIndividual(newIndiv);
        return null;
    }

    private static final PlayerSkeleton.Mapper<Double[], Float> EVOLVE_FUNC = new PlayerSkeleton.Mapper<Double[],
                Float>() {
        @Override
        public Float evaluate(Double[] genes) {
//            System.out.println("Evaluating fitness");
            double[] weights = new double[Constants.defaultGeneLength];
            for (int i = 0; i < Constants.defaultGeneLength; i++) {
                weights[i] = genes[i];
            }
            int fitness = PlayerSkeleton.run(weights);
            return (float) fitness;
        }
    };

    // Crossover chromosomes

    private static Particle crossover(Particle indiv1, Particle indiv2) {
        Particle newSol = new Particle();
        double c1,c2;
        double[] newGene1 = newSol.getGenes();
        int length = indiv1.size();

        double[] gene1 = indiv1.getGenes();
        double[] gene2 = indiv2.getGenes();

        int totalFitness = indiv1.getFitness() + indiv2.getFitness();
        
        if (totalFitness==0) {
            c1 = 0.5;
            c2 = 0.5;
        } else {
            c1 = (double) indiv1.getFitness() / (double) totalFitness;
            c2 = (double) indiv2.getFitness() / (double) totalFitness;
//            System.out.println("c1: " + c1 + "c2: " + c2);
        }

//        System.out.println("new gene1:");
        for(int i=0;i<Constants.defaultGeneLength; i++){
            newGene1[i] = c1*gene1[i] +c2*gene2[i];
//            System.out.println(newGene1[i]);
        }

        return newSol;
    }

    // Mutate an individual
    static void mutate(Particle indiv) {
        // Loop through genes

        Random random = new Random();
        if (Math.random() <= Constants.mutationRate) {
            int position = random.nextInt(10);
            double gene = (random.nextDouble() * 0.4 - 0.2) + 1;
            indiv.mutateGene(position, gene);
        }
    }

    static Particle getMutated(Particle indiv) {
        // Loop through genes

        Particle mutated = new Particle();
        Random random = new Random();
        for (int i = 0; i < indiv.size(); i++) {
            if (Math.random() <= Constants.mutationRate) {
                double mutateScale = (random.nextDouble() * 0.4 - 0.2) + 1;
                mutated.setGene(i, indiv.getGene(i) * mutateScale);
            } else {
                mutated.setGene(i, indiv.getGene(i));
            }
        }
        return mutated;
    }

    // Select chromosomes for crossover
    private static Particle tournamentSelection(Population pop) {
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
        return fittest.getKey();
    }

    public Population run( Population myPop, int i) {

        int generationCount = 0;
        int lostGeneration = 0;
        int maxFitness = 0;


   //     while (true) {
//            System.out.println("getFitness: " +myPop.getFittest().getFitness());
//            if(maxFitness > myPop.getFittest().getKey().getFitness()){
//                lostGeneration++;
//
//            } else {
//                maxFitness = myPop.getFittest().getKey().getFitness();
//                lostGeneration = 0;
//            }
////            if(lostGeneration> Constants.MAX_LOST_GENERATION){
////                break;
////            }
//            generationCount++;
//            System.out.println("Generation: " + i + " Fittest: " + myPop.getFittest().getKey().getFitness() + " Max " + maxFitness);
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