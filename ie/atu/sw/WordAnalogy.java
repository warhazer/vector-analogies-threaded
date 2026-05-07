package ie.atu.sw;

import java.util.Map;

/**
 * Evaluates word analogy expressions against a loaded embeddings map.
 *
 * The supported operators are {@code +}, {@code -}, {@code *}, and {@code /}.
 * Tokens are evaluated strictly left to right.
 */
public class WordAnalogy {

    private final Map<String, double[]> embeddings;

    /**
     * Constructs a {@code WordAnalogy} instance bound to the given embeddings map.
     *
     * @param embeddings the loaded word-to-vector map used for lookups
     *
     * <p><b>Time Complexity: O(1)</b> — stores a reference; no data is copied.</p>
     */
    public WordAnalogy(Map<String, double[]> embeddings) {
        this.embeddings = embeddings;
    }

    /**
     * Parses and evaluates a word analogy expression, returning the result vector.
     *
     *
     * @param expression the analogy expression, e.g. {@code "king - man + woman"}
     * @return the computed result vector after applying all operations
     * @throws IllegalArgumentException if the expression is empty, malformed,
     *                                  contains an unknown operator, or references
     *                                  a word not present in the embeddings
     *
     * <p><b>Time Complexity: O(k * d)</b> — k operator steps, each requiring
     * O(d) work to apply the operation across d-dimensional vectors.</p>
     */
    public double[] evaluate(String expression) {
        if (expression == null || expression.isBlank()) {
            throw new IllegalArgumentException("Expression must not be empty.");
        }

        String[] tokens = expression.trim().split("\\s+");

        // A well-formed expression has an odd number of tokens: w (op w)*
        if (tokens.length % 2 == 0) {
            throw new IllegalArgumentException(
                "Malformed expression. Expected format: word [op word] ...");
        }

        double[] result = getVector(tokens[0]);

        for (int i = 1; i + 1 < tokens.length; i += 2) {
            String op   = tokens[i];
            double[] next = getVector(tokens[i + 1]);
            result = switch (op) {
                case "+" -> VectorUtils.add(result, next);
                case "-" -> VectorUtils.subtract(result, next);
                case "*" -> VectorUtils.multiply(result, next);
                case "/" -> VectorUtils.divide(result, next);
                default  -> throw new IllegalArgumentException("Unknown operator: " + op);
            };
        }
        return result;
    }

    /**
     * Retrieves the embedding vector for a word, using a case-insensitive lookup.
     *
     * @param word the word whose vector is required
     * @return the embedding vector stored for that word
     * @throws IllegalArgumentException if the word is not found in the embeddings map
     *
     * <p><b>Time Complexity: O(1)</b> average — single hash map lookup.</p>
     */
    private double[] getVector(String word) {
        double[] v = embeddings.get(word.toLowerCase());
        if (v == null) {
            throw new IllegalArgumentException(
                "Word not found in embeddings: \"" + word + "\"");
        }
        return v;
    }
}
