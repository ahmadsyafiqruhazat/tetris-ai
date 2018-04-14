import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Population {

    ArrayList<Particle> chromosomes;

    public Population(int populationSize, boolean initialise) {
        // Initialise population
        chromosomes = new ArrayList<>();
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
        this.chromosomes = chromosomes;
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
        return chromosomes;
    }

    public void sort(){
        Collections.sort(chromosomes, Collections.reverseOrder());
    }

    public void reset(){
        for(Particle p : chromosomes) p.reset();
    }
}