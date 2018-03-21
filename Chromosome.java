import java.util.Random;

public class Chromosome {

    public static int defaultGeneLength = 4;
    private double[] genes = new double[defaultGeneLength];
    // Cache
    private int fitness = 0;
    private Random random = new Random();


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

    /* Public methods */
    public int size() {
        return genes.length;
    }

    public int getFitness() {
        if (fitness == 0) {
            int totalFitness = 0;
            for(int i =0 ; i<11; i++){
                totalFitness += PlayerSkeleton.run(genes);
            }
            fitness = totalFitness/10;
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