package ie.atu.sw;

/**
 * Application entry point for the Word Analogies tool.
 *
 * <p>Instantiates and starts the interactive command-line menu.
 * All application logic is delegated to {@link ApplicationMenu}.</p>
 */
public class Runner {

    /**
     * Creates an {@link ApplicationMenu} and starts the interactive loop.
     *
     * @param args command-line arguments (not used)
     * @throws Exception if the menu encounters an unrecoverable error
     *
     * <p><b>Time Complexity: O(1)</b> — delegates immediately to ApplicationMenu.</p>
     */
    public static void main(String[] args) throws Exception {
        new ApplicationMenu().start();
    }

    /**
     * Displays an animated terminal progress bar.
     *
     * <p>Uses the carriage-return character {@code \r} to overwrite the current
     * line, creating an in-place animation. This does <em>not</em> work in the
     * Eclipse console; run from a terminal instead.</p>
     *
     * <p>Call this method inside a processing loop, passing the current step
     * index and the total number of steps. Do not call
     * {@code System.out.println()} between calls or the next update will appear
     * on a new line.</p>
     *
     * @param index the current step (1-based)
     * @param total the total number of steps; should equal 100 for a percentage bar
     *
     * <p><b>Time Complexity: O(size)</b> — iterates over the fixed bar width
     * (50 characters) to build the display string; independent of n.</p>
     */
    public static void printProgress(int index, int total) {
        if (index > total) return;
        int  size        = 50;
        char done        = '█';
        char todo        = '░';
        int  complete    = (100 * index) / total;
        int  completeLen = size * complete / 100;

        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < size; i++) {
            sb.append((i < completeLen) ? done : todo);
        }

        System.out.print("\r" + sb + "] " + complete + "%");
        if (index == total) System.out.println();
    }
}
