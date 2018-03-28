import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Population {

    ArrayList<Chromosome> chromosomes;

    public Population(int populationSize, boolean initialise) {
        // Initialise population
        chromosomes = new ArrayList<>();
        if (initialise) {
            // Loop and create chromosomes
            for (int i = 0; i < populationSize; i++) {
                Chromosome newChromosome = new Chromosome();
                newChromosome.generateIndividual();
                chromosomes.add(newChromosome);
            }
        }
    }

    public void sort(){
        Collections.sort(chromosomes, new Comparator<Chromosome>() {
            @Override
            public int compare(Chromosome o1, Chromosome o2) {
                if (o1.getFitness()>o2.getFitness()) return 0;
                else return 1;
            }
        });
    }

    /* Getters */
    public Chromosome getIndividual(int index) {
        return chromosomes.get(index);
    }

    public Pair<Chromosome,Integer> getFittest() {
        Chromosome fittest = chromosomes.get(0);
        int pos=0;
        // Loop through chromosomes to find fittest
        for (int i = 0; i < size(); i++) {
            if (fittest.getFitness() <= getIndividual(i).getFitness()) {
                fittest = getIndividual(i);
                pos = i;
            }
        }
        return new Pair<>(fittest,pos);
    }

    /* Public methods */
    // Get population size
    public int size() {
        return chromosomes.size();
    }

    // Save individual
    public void saveIndividual(Chromosome indiv) {
        chromosomes.add(indiv);
    }

    public void saveIndividual(Chromosome indiv,int index) {
        chromosomes.add(index,indiv);
    }

    public ArrayList<Chromosome> getChromosomes() {
        return chromosomes;
    }
}