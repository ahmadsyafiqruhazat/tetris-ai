import java.util.Random;

public class Individual {

    static int defaultGeneLength = 64;
    private float[] genes = new float[defaultGeneLength];
    // Cache
    private int fitness = 0;
    private Random random = new Random();


    // Create a random individual
    public void generateIndividual() {
        for (int i = 0; i < size(); i++) {
            float gene = random.nextFloat() * 1000.0f;
            genes[i] = gene;
        }
    }

    /* Getters and setters */
    // Use this if you want to create individuals with different gene lengths
    public static void setDefaultGeneLength(int length) {
        defaultGeneLength = length;
    }

    public float getGene(int index) {
        return genes[index];
    }

    public void setGene(int index, float value) {
        genes[index] = value;
        fitness = 0;
    }

    /* Public methods */
    public int size() {
        return genes.length;
    }

    public int getFitness() {
        if (fitness == 0) {
            fitness = FitnessCalc.getFitness(this);
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