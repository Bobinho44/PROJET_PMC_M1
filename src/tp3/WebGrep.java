package tp3;

import dictionary.ThreadSafeDictionary;
import threadpool.ThreadPool;

public class WebGrep {

    private final static ThreadSafeDictionary explored = new ThreadSafeDictionary();
    private final static ThreadPool threadPool = new ThreadPool(Tools.numberThreads());
    private final static ThreadPool printer = new ThreadPool(1);

    private static void explore(String address) {

        // Submit the new page exploration to the thread pool
        threadPool.submitPriorityTask(() -> {

            if (explored.add(address)) {
                // Parse the page to find matches and hypertext links
                ParsedPage page = Tools.parsePage(address);
                if (!page.matches().isEmpty()) {

                    // Give print task to the printer
                    printer.submitPriorityTask(() -> {
                        Tools.print(page);
                        return true;
                    });

                    // Recursively explore other pages
                    for (String href : page.hrefs()) explore(href);
                }
            }

            return true;
        });
    }


    public static void main(String[] args) {
        // Initialize the program using the options given in argument
        if (args.length == 0) Tools.initialize("-cet --threads=1000 Nantes https://fr.wikipedia.org/wiki/Nantes");
        else Tools.initialize(args);

        // Get the starting URL given in argument
        for (String address : Tools.startingURL())
            explore(address);
    }

}