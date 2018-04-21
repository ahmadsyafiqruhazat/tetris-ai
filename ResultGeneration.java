import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.concurrent.ForkJoinPool;

public class ResultGeneration {
    private static PlayerSkeleton p =  new PlayerSkeleton(new ForkJoinPool());

    public static void main(String[] args){

        ArrayList<Double[]> allChromosomes = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            Double[] chromosomes = new Double[Constants.defaultWeights.length];
            for (int j = 0; j < Constants.defaultWeights.length; j ++ ) {
                chromosomes[j] = Constants.defaultWeights[j];
            }
            allChromosomes.add(chromosomes);
        }

        PlayerSkeleton.ConcurrentExecutor concurrentExecutor = new PlayerSkeleton.ConcurrentExecutor(new ForkJoinPool
                ());
        concurrentExecutor.map(RESULT_FUNC, allChromosomes, new ArrayList<>());
        return;
    }

    private static final PlayerSkeleton.Mapper<Double[], Float> RESULT_FUNC = new PlayerSkeleton.Mapper<Double[],
            Float>() {
        @Override
        public Float evaluate(Double[] genes) {
            double[] weights = new double[genes.length];
            for (int i = 0; i < genes.length; i++) {
                weights[i] = genes[i];
            }
            int result = PlayerSkeleton.run(weights);
            System.out.println("Result: " + result + " rows cleared!");
            return (float) result;
        }
    };

}
