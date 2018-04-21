public class runner {
    public static void main(String[] args) {
        Particle p = new Particle();
        double[] toTest = {0.9025570162931712, 1.7711836079651513, 1.6093509791627627, 1.1234484044209512, 0.12088937383020945, 0.7099328905248904, 0.9634530179037607, 1.076436835990118, 0.23238051424642148, 1.5813799214516555};
        p.setPosition(toTest);
        System.out.println(p.getFitness());
    }
}
