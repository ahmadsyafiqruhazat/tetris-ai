import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.CopyOnWriteArrayList;

public class Population {

    CopyOnWriteArrayList<Particle> chromosomes;

    public Population(int populationSize, boolean initialise) {
        // Initialise population
        chromosomes = new CopyOnWriteArrayList<>();
        if (initialise) {
            // Loop and create chromosomes
            for (int i = 0; i < populationSize; i++) {
                Particle newChromosome = new Particle();
                newChromosome.generateIndividual();
                chromosomes.add(newChromosome);
            }
        }
    }

    public void setChromosomes(ArrayList<Particle> chromosomes) {
        CopyOnWriteArrayList<Particle> copyOnWriteChromosomes = new CopyOnWriteArrayList<>();
        for (Particle chromosome: chromosomes) {
            copyOnWriteChromosomes.add(chromosome);
        }
        this.chromosomes = copyOnWriteChromosomes;
    }

    /* Getters */
    public Particle getIndividual(int index) {
        return chromosomes.get(index);
    }

    public Pair<Particle,Integer> getFittest() {
        Particle fittest = chromosomes.get(0);
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
    public void saveIndividual(Particle indiv) {
        chromosomes.add(indiv);
    }

    public void saveIndividual(Particle indiv,int index) {
        chromosomes.add(index,indiv);
    }

    public ArrayList<Particle> getChromosomes() {
        ArrayList<Particle> retChromosomes = new ArrayList<>();
        for (Particle chromosome: chromosomes) {
            retChromosomes.add(chromosome);
        }
        return retChromosomes;
    }

    public void sort(){
        System.out.println("Sort: population size - " + chromosomes.size());
        Collections.sort(chromosomes, Collections.reverseOrder());
    }

    public void reset(){
        for(Particle p : chromosomes) p.reset();
    }
}