import java.util.Random;

public class Particle {
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

    private int getOneFitness() {
        PlayerSkeleton p = new PlayerSkeleton();
        return  p.run(position);
    }

    // TODO: Parallelise
    // TODO: Something other than average?
    private void updateFitness() {
        int sum = 0;
        for (int i = 0; i < Constants.NUM_RUNS; i++) {
            sum += getOneFitness();
        }
        fitness = sum / Constants.NUM_RUNS;
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
        bounceVelocity();
    }

    private void updatePosition() {
        position = Calc.add(position, velocity);
        bouncePosition();
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
        initializePosition();
        initializeVelocity();
        initializeFitness();
        initializePBest();
    }

    public Particle(Chromosome chromosome) {
        double[] initPos = chromosome.getGenes();
        random = new Random();
        initializePosition(initPos);
        initializeVelocity();
        initializeFitness(chromosome.getFitness());
        initializePBest();
    }

    public void update() {
        updateFitness();
        updatePBest();
        updateGBest();
        updateVelocity();
        updatePosition();
    }

    public double[] getPosition() {
        return position;
    }

    public double[] getVelocity() {
        return velocity;
    }

    public int getFitness() {
        return fitness;
    }

    public double[] getPBest() {
        return pBest;
    }

    public int getPBestFitness() {
        return pBestFitness;
    }
}
