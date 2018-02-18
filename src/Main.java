public class Main {

    private static String FILE = "/Users/amounier/Documents/test.als";
    private static Integer ASSERT_INDEX = -1;
    private static Integer CE_MAX = 0;

    private static final String FILE_ARG = "-f";
    private static final String ASSERT_ARG = "-a";
    private static final String CE_MAX_ARG = "-ce_max";
    private static final String HELP_ARG = "-help";

    public static void main(String[] args) throws Exception {

        if (args.length == 0) {
            System.out.println("There were no commandline arguments passed!");
        } else {
            for (int i = 0; i < args.length; i++) {
                try {
                    if (args[i].equals(FILE_ARG)) {
                        FILE = args[++i];
                    }
                    if (args[i].equals(ASSERT_ARG)) {
                        ASSERT_INDEX = Integer.parseInt(args[++i]);
                    }
                    if (args[i].equals(CE_MAX_ARG)) {
                        CE_MAX = Integer.parseInt(args[++i]);
                    }
                    if (args[i].equals(HELP_ARG)) {
                        System.out.println("Help!");
                    }
                } catch (IndexOutOfBoundsException e) {
                    System.out.println("Invalid arguments.");
                    return;
                }
            }

            if (CE_MAX == 0) {
                System.out.println("Invalid arguments. No counterexamples maximum defined. Use -ce_max to set a maximum.");
                return;
            }
        }

        AlloyChecker checker = new AlloyChecker(FILE, ASSERT_INDEX, CE_MAX);
        checker.checkCmds();
    }
}
