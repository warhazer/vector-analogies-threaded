package ie.atu.sw;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Provides the interactive command-line menu for the Word Analogies application.
 *
 * <p>The menu drives the full user workflow:
 * <ol>
 *   <li>Specify and load a word embeddings file.</li>
 *   <li>Enter a word analogy expression to evaluate.</li>
 *   <li>Configure the number of top results to display.</li>
 *   <li>Specify the output file path.</li>
 *   <li>Choose the similarity method (cosine, dot product, or Euclidean).</li>
 * </ol>
 *
 * <p>This class coordinates {@link EmbeddingLoader}, {@link WordAnalogy},
 * {@link SimilaritySearch}, and {@link ResultWriter} without depending on
 * their internal implementations, promoting loose coupling.</p>
 */
public class ApplicationMenu {

    private Map<String, double[]> embeddings  = null;
    private String                outputFile  = "./out.txt";
    private int                   topN        = 10;
    private String                method      = "cosine";

    /**
     * Starts the interactive menu loop. Continues until the user selects quit.
     *
     * @throws Exception if an I/O or threading error occurs in a sub-operation
     *
     * <p><b>Time Complexity: O(1)</b> per menu iteration; overall runtime is
     * driven entirely by user input and the cost of individual operations.</p>
     */
    public void start() throws Exception {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {  
            printMenu();
            System.out.print(ConsoleColour.BLACK_BOLD_BRIGHT + "Select Option [1-5]> " + ConsoleColour.RESET);
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> loadEmbeddings(scanner);
                case "2" -> enterOperation(scanner);
                case "3" -> configureTopN(scanner);
                case "4" -> setOutputFile(scanner);
                case "5" -> chooseSimilarityMethod(scanner);
                case "?" -> running = false;
                default  -> System.out.println(ConsoleColour.RED + "Invalid option." + ConsoleColour.RESET);
            }
        }
        System.out.println(ConsoleColour.GREEN + "Goodbye!" + ConsoleColour.RESET);
        scanner.close();
    }

    /**
     * Prints the main menu header and option list to standard output.
     *
     * <p><b>Time Complexity: O(1)</b> — a fixed number of print statements.</p>
     */
    private void printMenu() {
        System.out.println(ConsoleColour.WHITE_BOLD);
        System.out.println("************************************************************");
        System.out.println("*     ATU - Dept. of Computer Science & Applied Physics    *");
        System.out.println("*                                                          *");
        System.out.println("*  Word Analogies with Vector Arithmetic & Virtual Threads *");
        System.out.println("*                                                          *");
        System.out.println("************************************************************");
        System.out.println(ConsoleColour.CYAN
                + "(1) Specify Embeddings File"
                + ConsoleColour.RESET);
        System.out.println(ConsoleColour.CYAN
                + "(2) Enter Word Analogy Operation"
                + ConsoleColour.RESET);
        System.out.println(ConsoleColour.CYAN
                + "(3) Configure Top-N Results       [current: " + topN + "]"
                + ConsoleColour.RESET);
        System.out.println(ConsoleColour.CYAN
                + "(4) Specify Output File           [current: " + outputFile + "]"
                + ConsoleColour.RESET);
        System.out.println(ConsoleColour.CYAN
                + "(5) Choose Similarity Method      [current: " + method + "]"
                + ConsoleColour.RESET);
        System.out.println(ConsoleColour.RED
                + "(?) Quit"
                + ConsoleColour.RESET);
        System.out.println();
    }

    /**
     * Prompts for an embeddings file path, loads it with {@link EmbeddingLoader},
     * and reports the number of words loaded and the time taken.
     *
     * @param scanner the {@link Scanner} reading from standard input
     * @throws Exception if the file cannot be read or a virtual thread fails
     *
     * <p><b>Time Complexity: O(n * d)</b> — loading n words each of dimension d.
     * Parsing is parallelised across virtual threads inside EmbeddingLoader.</p>
     */
    private void loadEmbeddings(Scanner scanner) throws Exception {
        System.out.print(ConsoleColour.YELLOW + "Enter path to embeddings file> " + ConsoleColour.RESET);
        String path = scanner.nextLine().trim();

        if (path.isBlank()) {
            System.out.println(ConsoleColour.RED + "No path entered." + ConsoleColour.RESET);
            return;
        }

        System.out.println(ConsoleColour.YELLOW + "Loading embeddings, please wait..." + ConsoleColour.RESET);

        long start = System.currentTimeMillis();
        try {
            embeddings = new EmbeddingLoader().load(path);
        } catch (Exception e) {
            System.out.println(ConsoleColour.RED + "Failed to load file: " + e.getMessage() + ConsoleColour.RESET);
            return;
        }
        long elapsed = System.currentTimeMillis() - start;

        System.out.printf(ConsoleColour.GREEN + "Loaded %,d words in %,d ms%n" + ConsoleColour.RESET,
                embeddings.size(), elapsed);
    }

    /**
     * Prompts for a word analogy expression, evaluates it, searches the
     * embeddings for the top-N matches, prints the results, and writes
     * them to the configured output file.
     *
     * @param scanner the {@link Scanner} reading from standard input
     * @throws Exception if evaluation or the similarity search fails
     *
     * <p><b>Time Complexity: O(n * d)</b> — dominated by the similarity search
     * over n embeddings of dimension d, which uses concurrent virtual threads.</p>
     */
    private void enterOperation(Scanner scanner) throws Exception {
        if (embeddings == null) {
            System.out.println(ConsoleColour.RED
                    + "Please load an embeddings file first (option 1)."
                    + ConsoleColour.RESET);
            return;
        }

        System.out.print(ConsoleColour.YELLOW
                + "Enter operation (e.g. irish - whiskey + vodka)> "
                + ConsoleColour.RESET);
        String expression = scanner.nextLine().trim();

        if (expression.isBlank()) return;

        try {
            double[] resultVector = new WordAnalogy(embeddings).evaluate(expression);

            System.out.println(ConsoleColour.YELLOW + "Searching..." + ConsoleColour.RESET);

            List<Map.Entry<String, Double>> results =
                    new SimilaritySearch().findTopN(resultVector, embeddings, topN, method);

            System.out.printf(ConsoleColour.GREEN_BOLD
                    + "%nTop %d results for: %s%n"
                    + ConsoleColour.RESET, topN, expression);

            for (int i = 0; i < results.size(); i++) {
                Map.Entry<String, Double> entry = results.get(i);
                System.out.printf(ConsoleColour.CYAN + "%3d) %-20s => %.7f%n" + ConsoleColour.RESET,
                        i + 1, entry.getKey(), entry.getValue());
            }

            new ResultWriter().write(results, outputFile, expression);
            System.out.println(ConsoleColour.GREEN + "Results written to: " + outputFile + ConsoleColour.RESET);

        } catch (IllegalArgumentException e) {
            System.out.println(ConsoleColour.RED + "Error: " + e.getMessage() + ConsoleColour.RESET);
        }
    }

    /**
     * Prompts the user to change the number of top results returned and displayed.
     *
     * @param scanner the {@link Scanner} reading from standard input
     *
     * <p><b>Time Complexity: O(1)</b> — updates a single integer field.</p>
     */
    private void configureTopN(Scanner scanner) {
        System.out.print(ConsoleColour.YELLOW
                + "Enter number of top results to show [current: " + topN + "]> "
                + ConsoleColour.RESET);
        String input = scanner.nextLine().trim();
        try {
            int n = Integer.parseInt(input);
            if (n > 0) {
                topN = n;
                System.out.println(ConsoleColour.GREEN + "Top-N set to " + topN + ConsoleColour.RESET);
            } else {
                System.out.println(ConsoleColour.RED + "Value must be greater than zero." + ConsoleColour.RESET);
            }
        } catch (NumberFormatException e) {
            System.out.println(ConsoleColour.RED + "Invalid number." + ConsoleColour.RESET);
        }
    }

    /**
     * Prompts the user to specify the output file path for saving results.
     *
     * @param scanner the {@link Scanner} reading from standard input
     *
     * <p><b>Time Complexity: O(1)</b> — updates a single String field.</p>
     */
    private void setOutputFile(Scanner scanner) {
        System.out.print(ConsoleColour.YELLOW
                + "Enter output file path [current: " + outputFile + "]> "
                + ConsoleColour.RESET);
        String path = scanner.nextLine().trim();
        if (!path.isBlank()) {
            outputFile = path;
            System.out.println(ConsoleColour.GREEN + "Output file set to: " + outputFile + ConsoleColour.RESET);
        }
    }

    /**
     * Displays available similarity methods and updates the active selection.
     *
     * <p>Available choices:
     * <ul>
     *   <li>{@code 1} — cosine similarity (default)</li>
     *   <li>{@code 2} — Euclidean distance (negated so higher = more similar)</li>
     *   <li>{@code 3} — dot product</li>
     * </ul>
     * </p>
     *
     * @param scanner the {@link Scanner} reading from standard input
     *
     * <p><b>Time Complexity: O(1)</b> — updates a single String field.</p>
     */
    private void chooseSimilarityMethod(Scanner scanner) {
        System.out.println(ConsoleColour.CYAN + "Available similarity methods:");
        System.out.println("  (1) cosine     - Cosine similarity (default, range -1..1)");
        System.out.println("  (2) euclidean  - Euclidean distance (lower distance = more similar)");
        System.out.println("  (3) dot        - Dot product" + ConsoleColour.RESET);
        System.out.print(ConsoleColour.YELLOW + "Choose [1-3]> " + ConsoleColour.RESET);

        method = switch (scanner.nextLine().trim()) {
            case "2" -> "euclidean";
            case "3" -> "dot";
            default  -> "cosine";
        };
        System.out.println(ConsoleColour.GREEN + "Similarity method set to: " + method + ConsoleColour.RESET);
    }
}
