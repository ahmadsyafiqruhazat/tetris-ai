
public class Population {

    Chromosome[] chromosomes;

    public Population(int populationSize, boolean initialise) {
        chromosomes = new Chromosome[populationSize];
        // Initialise population
        if (initialise) {
            // Loop and create chromosomes
            for (int i = 0; i < size(); i++) {
                Chromosome newChromosome = new Chromosome();
                newChromosome.generateIndividual();
                saveIndividual(i, newChromosome);
            }
        }
    }

    /* Getters */
    public Chromosome getIndividual(int index) {
        return chromosomes[index];
    }

    public Chromosome getFittest() {
        Chromosome fittest = chromosomes[0];
        // Loop through chromosomes to find fittest
        for (int i = 0; i < size(); i++) {
            if (fittest.getFitness() <= getIndividual(i).getFitness()) {
                fittest = getIndividual(i);
            }
        }
        return fittest;
    }

    /* Public methods */
    // Get population size
    public int size() {
        return chromosomes.length;
    }

    // Save individual
    public void saveIndividual(int index, Chromosome indiv) {
        chromosomes[index] = indiv;
    }
}