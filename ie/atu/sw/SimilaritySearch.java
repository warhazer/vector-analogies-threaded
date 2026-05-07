package ie.atu.sw;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * Searches a word embeddings map for the words whose vectors are most similar
 * to a given target vector.
 *
 */
public class SimilaritySearch {

    /** Number of words assigned to each virtual thread. */
    private static final int BATCH_SIZE = 1_000;

    /**
     * Finds the {@code topN} words most similar to {@code target} using the
     * specified similarity method.
     *
     * <p>The embeddings map is divided into batches of {@value #BATCH_SIZE} words.
     * Each batch is processed by a single virtual thread, keeping the number of
     * concurrent tasks manageable while still parallelising the work.</p>
     *
     * <p>Supported methods:
     * <ul>
     *   <li>{@code "cosine"} — cosine similarity (higher = more similar)</li>
     *   <li>{@code "dot"}    — dot product (higher = more similar)</li>
     *   <li>{@code "euclidean"} — Euclidean distance (lower = more similar;
     *       stored as a negative value so the same "highest score wins" sort
     *       can be used for all methods)</li>
     * </ul>
     * Any unrecognised method string defaults to cosine similarity.</p>
     *
     * @param target     the result vector from a word analogy operation
     * @param embeddings the full word-to-vector map to search through
     * @param topN       the number of top results to return
     * @param method     the similarity metric to use ({@code "cosine"},
     *                   {@code "dot"}, or {@code "euclidean"})
     * @return a list of (word, score) entries sorted descending by score,
     *         limited to {@code topN} entries
     * @throws Exception if a virtual thread is interrupted during execution
     *
     * <p><b>Time Complexity: O(n * d)</b> — every one of the n words requires
     * an O(d) similarity computation. Virtual threads allow batches to overlap,
     * reducing wall-clock time toward O(n * d / p) where p is the number of
     * available carrier threads.</p>
     */
    public List<Map.Entry<String, Double>> findTopN(
            double[] target,
            Map<String, double[]> embeddings,
            int topN,
            String method) throws Exception {

        Map<String, Double> scores = new ConcurrentHashMap<>(embeddings.size());

        // Convert to a list so we can slice it into fixed-size batches.
        List<Map.Entry<String, double[]>> entries = new ArrayList<>(embeddings.entrySet());

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<?>> futures = new ArrayList<>();

            for (int start = 0; start < entries.size(); start += BATCH_SIZE) {
                int end = Math.min(start + BATCH_SIZE, entries.size());
                // Capture immutable slice bounds for the lambda.
                final List<Map.Entry<String, double[]>> batch = entries.subList(start, end);

                futures.add(executor.submit(() -> {
                    for (Map.Entry<String, double[]> entry : batch) {
                        double score = computeScore(target, entry.getValue(), method);
                        scores.put(entry.getKey(), score);
                    }
                }));
            }

            for (Future<?> f : futures) f.get();
        }

        // Sort all scored words descending and keep only the top N.
        return scores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(topN)
                .collect(Collectors.toList());
    }

    /**
     * Computes a similarity score between {@code target} and {@code candidate}
     * using the chosen method. For Euclidean distance the value is negated so
     * that a higher score always means greater similarity, regardless of method.
     *
     * @param target    the query vector
     * @param candidate a candidate embedding vector
     * @param method    the metric name: {@code "cosine"}, {@code "dot"},
     *                  or {@code "euclidean"}
     * @return a score where higher values indicate greater similarity
     *
     * <p><b>Time Complexity: O(d)</b> — delegates to a single-pass O(d) method
     * in {@link VectorUtils}.</p>
     */
    private double computeScore(double[] target, double[] candidate, String method) {
        return switch (method) {
            case "dot"       -> VectorUtils.dotProduct(target, candidate);
            case "euclidean" -> -VectorUtils.euclideanDistance(target, candidate);
            default          -> VectorUtils.cosineSimilarity(target, candidate);
        };
    }
}
