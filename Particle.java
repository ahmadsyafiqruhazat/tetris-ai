import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ForkJoinPool;

public class Particle implements Comparable<Particle> {
    private static final int INIT_FITNESS = 0;

    public static final int DIMENSIONS = 10;

    public static double[] MAX;
    public static double[] MIN;
    public static double INERTIA_WEIGHT;
    public static double COGNITIVE_WEIGHT;
    public static double SOCIAL_WEIGHT;
    public static double[] gBest;
    public static int gBestFitness;

    // TODO: If anything hits NaN, randomise (or adjust on the basis of the best solutions)

    private double[] position;
    private double[] velocity;
    private int fitness;
    private Random random;

    private double[] pBest;
    private int pBestFitness;

    private boolean hasUpdated  = true;

    private CopyOnWriteArrayList<Double[]> allPositions = new CopyOnWriteArrayList<>();
    private PlayerSkeleton.ConcurrentExecutor concurrentExecutor = new PlayerSkeleton.ConcurrentExecutor(new
            ForkJoinPool());

    private double getRandomInRange(double min, double max) {
        return min + (random.nextDouble() * (max - min));
    }

    private boolean isEven(double dNum) {
        long lNum = (long) dNum;
        return (lNum % 2) == 0;
    }

    private void bounceVelocity() {
        double size;
        for (int i = 0; i < velocity.length; i++) {
            size = MAX[i] - MIN[i];
            velocity[i] = velocity[i] % size;
        }
    }

    private void bouncePosition() {
        double size, displacement;
        for (int i = 0; i < position.length; i++) {
            size = MAX[i] - MIN[i];
            // TODO: Simplify (I'm sure it CAN be simplified)
            if (position[i] < MIN[i]) {
                displacement = MAX[i] - position[i];
                if (isEven(displacement / size))
                    displacement = size - (displacement % size);
                else
                    displacement = displacement % size;
                position[i] = MIN[i] + displacement;
            } else if (position[i] > MAX[i]) {
                displacement = position[i] - MIN[i];
                if (isEven(displacement / size))
                    displacement = size - (displacement % size);
                else
                    displacement = displacement % size;
                position[i] = MAX[i] - displacement;
            }
        }
    }


    private static final PlayerSkeleton.Evaluator<Double[], Float> FITNESS_FUNC = new PlayerSkeleton.Evaluator<Double[],
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

    private static final PlayerSkeleton.Executor<Float, Float> AVG_SCORE = new PlayerSkeleton.Executor<Float,
            Float>() {

        @Override
        public Float execute(Iterable<Float> inputs) {
//            System.out.println("Taking average");
            int count = 0;
            float sum = 0.0f;

            for(float num: inputs) {
                sum += num;
                ++count;
            }

            return sum / (float) count;
        }
    };

//    private int getOneFitness() {
//        PlayerSkeleton p = new PlayerSkeleton(new ForkJoinPool());
//        return  p.run(position);
//    }

    // TODO: Parallelise
    // TODO: Something other than average?
    private void updateFitness() {
        hasUpdated = true;
        allPositions.clear();
        float result = 0;
        Double[] weights = new Double[Constants.defaultGeneLength];
        for (int i = 0; i < Constants.defaultGeneLength; i++) {
            weights[i] = position[i];
        }

        for (int i = 0; i < Constants.NUM_RUNS; i++) {
            allPositions.add(weights);
        }

//        System.out.println("Number of runs: " + allPositions.size());

        result = concurrentExecutor.execute(FITNESS_FUNC, AVG_SCORE, allPositions);
        fitness = (int) result;
    }

    private void updatePBest() {
        if (fitness > pBestFitness) {
            System.arraycopy(position, 0, pBest, 0, position.length);
            pBestFitness = fitness;
        }
    }

    private void updateGBest() {
        if (fitness > gBestFitness) {
            System.arraycopy(position, 0, gBest, 0, position.length);
            gBestFitness = fitness;
        }
    }

    private void updateVelocity() {
        double rand1 = random.nextDouble();
        double rand2 = random.nextDouble();
        double[] first = Calc.scale(velocity, INERTIA_WEIGHT);
        double[] second = Calc.scale(Calc.subtract(pBest, position), rand1 * COGNITIVE_WEIGHT);
        double[] third = Calc.scale(Calc.subtract(gBest, position), rand2 * SOCIAL_WEIGHT);
        velocity = Calc.add(Calc.add(first, second), third);
//        bounceVelocity();
    }

    private void updatePosition() {
        position = Calc.add(position, velocity);
//        bouncePosition();
    }

    private void initializePosition() {
        position = new double[DIMENSIONS];
        for (int i = 0; i < DIMENSIONS; i++) {
            position[i] = getRandomInRange(MIN[i], MAX[i]);
        }
    }

    private void initializePBest() {
        pBest = new double[DIMENSIONS];
        for (int i = 0; i < DIMENSIONS; i++) {
            pBest[i] = 0;
        }
    }

    private void initializePosition(double[] initPos) {
        position = new double[DIMENSIONS];
        System.arraycopy(initPos, 0, position, 0, initPos.length);
    }

    private void initializeVelocity() {
        velocity = new double[DIMENSIONS];
    }

    private void initializeFitness() {
        fitness = 0;
    }

    private void initializeFitness(int fitness) {
        this.fitness = fitness;
    }
    public Particle() {
        random = new Random();
        position = new double[DIMENSIONS];

//        initializePosition();
        initializeVelocity();
        initializeFitness();
        initializePBest();
    }

//    public Particle(Chromosome chromosome) {
//        double[] initPos = chromosome.getGenes();
//        random = new Random();
//        initializePosition(initPos);
//        initializeVelocity();
//        initializeFitness(chromosome.getFitness());
//        initializePBest();
//    }

    public void update() {
        updateFitness();
        updatePBest();
        updateGBest();
        updateVelocity();
        updatePosition();
    }

    public void generateIndividual() {
        for (int i = 0; i < size(); i++) {
            double gene = random.nextDouble() * 1000.0f;
            position[i] = gene;
        }
    }

    public double getGene(int index) {
        return position[index];
    }

    public double[] getGenes() {
        hasUpdated = false;
        return position;
    }

    public void setGene(int index, double value) {
        position[index] = value;
        fitness = 0;
    }

    public void mutateGene(int index, double value) {
        hasUpdated = false;
        position[index] *= value;
        fitness = 0;
    }

    @Override
    public String toString() {
        String geneString = "";
        for (int i = 0; i < size(); i++) {
            geneString += getGene(i);
        }
        return geneString;
    }

    public int size() {
        return position.length;
    }

    public double[] getPosition() {
        return position;
    }

    public double[] getVelocity() {
        return velocity;
    }

    public int getFitness() {
        if(!hasUpdated || fitness == 0){
            updateFitness();
        }
        return fitness;
    }

    public double[] getPBest() {
        return pBest;
    }

    public int getPBestFitness() {
        return pBestFitness;
    }

    @Override
    public int compareTo(Particle particle) {
        return this.getFitness() - particle.getFitness();
    }

}

