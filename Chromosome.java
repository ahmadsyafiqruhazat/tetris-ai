import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;

public class Chromosome {

    private double[] genes = new double[Constants.defaultGeneLength];
    // Cache
    private int fitness = 0;
    private Random random = new Random();
    private ArrayList<Double[]> allGenes = new ArrayList<>();
    private PlayerSkeleton.ConcurrentExecutor concurrentExecutor = new PlayerSkeleton.ConcurrentExecutor(new
            ForkJoinPool());

    public Chromosome(){}

    public Chromosome(double[] genes, int fitness){
        this.genes = genes;
        this.fitness = fitness;
    }
    public void generateIndividual() {
        for (int i = 0; i < size(); i++) {
            double gene = random.nextDouble() * 10.0f;
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

    private static final PlayerSkeleton.Evaluator<Double[], Float> FITNESS_FUNC = new PlayerSkeleton.Evaluator<Double[],
            Float>() {
        @Override
        public Float evaluate(Double[] genes) {
            System.out.println("Evaluating fitness");
            double[] weights = new double[Constants.defaultGeneLength];
            for (int i = 0; i < Constants.defaultGeneLength; i++) {
                weights[i] = genes[i];
            }
            int fitness = PlayerSkeleton.run(weights);
            return (float) fitness;
        }
    };

    private static final PlayerSkeleton.Executor<Float, Float> AVG_SCORE = new PlayerSkeleton.Executor<Float,
            Float>() {

        @Override
        public Float execute(Iterable<Float> inputs) {
            System.out.println("Taking average");
            int count = 0;
            float sum = 0.0f;

            for(float num: inputs) {
                sum += num;
                ++count;
            }

            return sum / (float) count;
        }
    };

    /* Public methods */
    public int size() {
        return genes.length;
    }

    public int getFitness() {
        if (fitness == 0) {
            int totalFitness = 0;
            Double[] weights = new Double[Constants.defaultGeneLength];
            for (int i = 0; i < Constants.defaultGeneLength; i++) {
                weights[i] = genes[i];
            }

            for(int i =0 ; i<=Constants.NUM_RUNS; i++){
                allGenes.add(weights);
            }
            float result = concurrentExecutor.execute(FITNESS_FUNC, AVG_SCORE, allGenes);
            fitness = (int) result;
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