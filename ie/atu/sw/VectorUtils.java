package ie.atu.sw;

/**
 * Provides static utility methods for element-wise vector arithmetic and
 * vector similarity metrics used in word embedding computations.
 */
public class VectorUtils {

   /** <summary>
     Adds two vectors element-wise.
     * @return a new vector where {@code result[i] = a[i] + b[i]}
     */
    public static double[] add(double[] a, double[] b) {
        double[] result = new double[a.length];
        for (int i = 0; i < a.length; i++) result[i] = a[i] + b[i];
        return result;
    }

    /**
     * Subtracts vector {@code b} from vector {@code a} element-wise.
     * @return a new vector where {@code result[i] = a[i] - b[i]}
     */
    public static double[] subtract(double[] a, double[] b) {
        double[] result = new double[a.length];
        for (int i = 0; i < a.length; i++) result[i] = a[i] - b[i];
        return result;
    }

    /**
     * Multiplies two vectors element-wise (Hadamard product).
     * @return a new vector where {@code result[i] = a[i] * b[i]}
     */
    public static double[] multiply(double[] a, double[] b) {
        double[] result = new double[a.length];
        for (int i = 0; i < a.length; i++) result[i] = a[i] * b[i];
        return result;
    }

    /**
     * Divides vector {@code a} by vector {@code b} element-wise.
     * @return a new vector where {@code result[i] = a[i] / b[i]}
     */
    public static double[] divide(double[] a, double[] b) {
        double[] result = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = (b[i] != 0.0) ? a[i] / b[i] : 0.0;
        }
        return result;
    }

    /**
     * Computes the dot product of two vectors.
     * @return the dot product: {@code sum(a[i] * b[i])}
     */
    public static double dotProduct(double[] a, double[] b) {
        double sum = 0.0;
        for (int i = 0; i < a.length; i++) sum += a[i] * b[i];
        return sum;
    }

    /**
     * Computes the Euclidean distance between two vectors.
     * Calculated as the square root of the sum of squared differences.

     * @return the Euclidean distance {@code sqrt(sum((a[i] - b[i])^2))}
     */
    public static double euclideanDistance(double[] a, double[] b) {
        double sum = 0.0;
        for (int i = 0; i < a.length; i++) {
            double diff = a[i] - b[i];
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }

    /**
     * Computes the cosine similarity between two vectors.
     * Returns a value in [-1, 1] where 1 means the vectors point in exactly
     * the same direction (maximally similar) and 0 means orthogonal.

     * @return cosine similarity in the range [-1, 1]; returns 0.0 if either
     *         vector has zero magnitude
     */
    public static double cosineSimilarity(double[] a, double[] b) {
        double dot = 0.0, magA = 0.0, magB = 0.0;
        for (int i = 0; i < a.length; i++) {
            dot  += a[i] * b[i];
            magA += a[i] * a[i];
            magB += b[i] * b[i];
        }
        double denom = Math.sqrt(magA) * Math.sqrt(magB);
        return (denom == 0.0) ? 0.0 : dot / denom;
    }
}
