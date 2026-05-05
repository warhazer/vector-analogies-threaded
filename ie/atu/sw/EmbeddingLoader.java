package ie.atu.sw;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Loads word embeddings from a GloVe-format text file into an in-memory map.
 */
public class EmbeddingLoader {

    /**
     * Reads the embeddings file into a {@code Map<String, double[]>}.
     *
     * <p>All lines are first collected from the file sequentially, 
     * then each line is dispatched to a virtual thread for parsing. 
     * The caller blocks until every line has been processed.</p>
     *
     * @param filePath the path to the embeddings text file
     * @return a map from lowercase word to its embedding vector
     * @throws Exception if the file cannot be opened or a thread is interrupted
     *
     * <p><b>Time Complexity: O(n * d)</b> — n lines, each requiring O(d) work
     * to split and parse d doubles. Virtual threads allow the O(d) steps to
     * overlap in time across lines, reducing wall-clock time.</p>
     */
    public Map<String, double[]> load(String filePath) throws Exception {
        Map<String, double[]> embeddings = new ConcurrentHashMap<>();

        // Read all lines sequentially first; I/O on a single file does not
        // benefit from parallelism and sequential reads have better buffering.
        List<String> lines = new ArrayList<>();
        try (var reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isBlank()) lines.add(line);
            }
        }

        // Dispatch each line to a virtual thread for concurrent parsing.
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<?>> futures = new ArrayList<>(lines.size());
            for (String line : lines) {
                futures.add(executor.submit(() -> parseLine(line, embeddings)));
            }
            // Wait for all parsing tasks to finish before returning.
            for (Future<?> f : futures) f.get();
        }

        return embeddings;
    }

    /**
     * Parses a single line from the embeddings file and inserts the result
     * into the shared map. The word key is stored in lower case.
     *
     * @param line      a non-blank line from the embeddings file
     * @param embeddings the shared map to write the parsed entry into
     *
     * <p><b>Time Complexity: O(d)</b> — splits and converts d double values.
     * Called concurrently from virtual threads; the ConcurrentHashMap ensures
     * thread-safe insertion without explicit locking.</p>
     */
    private void parseLine(String line, Map<String, double[]> embeddings) {
        String[] parts = line.split(",\\s*");
        if (parts.length < 2) return;

        String word = parts[0].trim().toLowerCase();
        double[] vector = new double[parts.length - 1];
        for (int i = 1; i < parts.length; i++) {
            try {
                vector[i - 1] = Double.parseDouble(parts[i].trim());
            } catch (NumberFormatException ignored) {
                vector[i - 1] = 0.0;
            }
        }
        embeddings.put(word, vector);
    }
}
