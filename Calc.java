public class Calc {
    // NON-MUTATING!!!
    public static double[] scale(double[] v, double n) {
        double[] scaled = new double[v.length];
        for (int i = 0; i < v.length; i++) {
            scaled[i] = v[i] * n;
        }
        return scaled;
    }

    public static double[] add(double[] v1, double[] v2) {
        if (v1.length != v2.length)
            return null;
        double[] added = new double[v1.length];
        for (int i = 0; i < v1.length; i++) {
            added[i] = v1[i] + v2[i];
        }
        return added;
    }

    public static double[] subtract(double[] v1, double[] v2) {
        return add(v1, scale(v2, -1));
    }
}