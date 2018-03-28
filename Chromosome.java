import java.util.Random;

public class Chromosome {

    private double[] genes = new double[Constants.defaultGeneLength];
    // Cache
    private int fitness = 0;
    private Random random = new Random();

    public Chromosome(){}

    public Chromosome(double[] genes, int fitness){
        this.genes = genes;
        this.fitness = fitness;
    }
    public void generateIndividual() {
        for (int i = 0; i < size(); i++) {
            double gene = random.nextDouble() * 1000.0f;
            genes[i] = gene;
        }
    }

    public double getGene(int index) {
        return genes[index];
    }

    public double[] getGenes() {
        return genes;
    }

    public void setGene(int index, double value) {
        genes[index] = value;
        fitness = 0;
    }

    public void mutateGene(int index, double value) {
        genes[index] += value;
        fitness = 0;
    }

    /* Public methods */
    public int size() {
        return genes.length;
    }

    public int getFitness() {
        if (fitness == 0) {
            int totalFitness = 0;
            for(int i =0 ; i<=Constants.NUM_RUNS; i++){
                totalFitness += PlayerSkeleton.run(genes);
            }
            fitness = totalFitness/Constants.NUM_RUNS;
        }

        return fitness;
    }

    @Override
    public String toString() {
        String geneString = "";
        for (int i = 0; i < size(); i++) {
            geneString += getGene(i);
        }
        return geneString;
    }
}