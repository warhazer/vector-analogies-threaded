package ie.atu.sw;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

/**
 * Writes word analogy results to a plain-text output file.
 *
 * <p>Each result line is formatted as a ranked entry showing the word and
 * its similarity score. Existing files at the target path are overwritten.</p>
 */
public class ResultWriter {

    /**
     * Writes a ranked list of word-similarity results to the specified file.
     *
     * @param results    an ordered list of (word, score) entries to write,
     *                   typically sorted descending by similarity
     * @param outputPath the file path to write results to; created if absent,
     *                   overwritten if it already exists
     * @param expression the original analogy expression, included as a header
     * @throws IOException if the file cannot be created or written to
     *
     * <p><b>Time Complexity: O(n)</b> — iterates once over n result entries,
     * writing a constant amount of text per entry.</p>
     */
    public void write(
            List<Map.Entry<String, Double>> results,
            String outputPath,
            String expression) throws IOException {

        try (var writer = new PrintWriter(new FileWriter(outputPath))) {
            writer.println("Word Analogy Results");
            writer.println("====================");
            writer.println("Expression: " + expression);
            writer.println();
            for (int i = 0; i < results.size(); i++) {
                Map.Entry<String, Double> entry = results.get(i);
                writer.printf("%3d) %-20s => %.7f%n",
                        i + 1, entry.getKey(), entry.getValue());
            }
        }
    }
}
