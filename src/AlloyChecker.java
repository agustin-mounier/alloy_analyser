import edu.mit.csail.sdg.alloy4.A4Reporter;
import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4compiler.ast.Command;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprList;
import edu.mit.csail.sdg.alloy4compiler.parser.CompModule;
import edu.mit.csail.sdg.alloy4compiler.parser.CompUtil;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Options;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;
import edu.mit.csail.sdg.alloy4compiler.translator.TranslateAlloyToKodkod;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class AlloyChecker {

    private final String filePath;
    private final Integer assertIndex;
    private final Integer CE_MAX;


    public AlloyChecker(String filePath, Integer assertIndex, Integer ceMax) {
        this.filePath = filePath;
        this.assertIndex = assertIndex;
        this.CE_MAX = ceMax;
    }


    public void checkCmds() throws Err {
        A4Reporter rep = new A4Reporter();
        CompModule module;

        try {
            module = CompUtil.parseEverything_fromFile(rep, null, filePath);
        } catch (Err err) {
            System.out.println("Error reading .als file.");
            err.printStackTrace();
            return;
        }

        if (assertIndex == -1) {
            for (Command cmd : module.getAllCommands()) {
                Map<Integer, List<Pair<Command, Integer>>> cmdMap = checkCmdsForAssert(module, cmd);
                generateResponse(cmd, cmdMap);
            }
        } else {
            Command cmd;

            try {
                cmd = module.getAllCommands().get(assertIndex);
            } catch (IndexOutOfBoundsException e) {
                System.out.println("Invalid command index.");
                return;
            }

            Map<Integer, List<Pair<Command, Integer>>> cmdMap = checkCmdsForAssert(module, cmd);
            generateResponse(cmd, cmdMap);
        }
    }

    private Map<Integer, List<Pair<Command, Integer>>> checkCmdsForAssert(CompModule module, Command cmd) throws Err {
        A4Reporter rep = new A4Reporter();
        A4Options opt = new A4Options();
        opt.originalFilename = filePath;
        opt.solver = A4Options.SatSolver.SAT4J;
        opt.symmetry = 20;
        opt.skolemDepth = 1;


        Map<Integer, List<Pair<Command, Integer>>> map = new HashMap<>();

        List<Command> posibleCommands = AlloyFactCombinator.generatePowerCombinations(cmd);

        for (Command posibleCmd : posibleCommands) {
            A4Solution sol = TranslateAlloyToKodkod.execute_command(rep, module.getAllReachableSigs(), posibleCmd, opt);

            if (sol.satisfiable()) {
                int cantCe = 0;
                while (sol.satisfiable() && cantCe < CE_MAX) {
                    sol = sol.next();
                    cantCe++;
                }

                int facts = ((ExprList) posibleCmd.formula).args.size();
                if (map.containsKey(facts)) {
                    map.get(facts).add(new Pair(posibleCmd, cantCe));
                } else {
                    List<Pair<Command, Integer>> cmds = new ArrayList<>();
                    cmds.add(new Pair(posibleCmd, cantCe));
                    map.put(facts, cmds);
                }
            }
        }

        sortCmdsMap(map);

        return map;
    }

    private void sortCmdsMap(Map<Integer, List<Pair<Command, Integer>>> cmdMap) {

        for (Integer facts : cmdMap.keySet()) {
            List<Pair<Command, Integer>> cmds = cmdMap.get(facts);

            Collections.sort(cmds, new Comparator<Pair<Command, Integer>>() {
                @Override
                public int compare(Pair<Command, Integer> o1, Pair<Command, Integer> o2) {
                    return o1.getValue() - o2.getValue();
                }
            });
        }
    }

    private void generateResponse(Command cmd, Map<Integer, List<Pair<Command, Integer>>> cmdMap) {
        Scanner scan = new Scanner(System.in);

        System.out.println("<<< Result for command: " + cmd.toString() + " >>>");

        if (cmdMap.isEmpty()) {
            System.out.println("No counterexamples found even for the most unrestricted version of the model.");
            System.out.println("The assert may be trivially true.");
        } else if (checkCounterexamplesMax(cmdMap)) {
            System.out.println("All facts combinations are equally significant for the given maximum of counterexamples");
        } else {
            int order = 0;
            for (int i = 2; i <= ((ExprList) cmd.formula).args.size(); i++) {
                List<Pair<Command, Integer>> list = cmdMap.get(i);
                if (list != null) {
                    if (i == 2) {
                        System.out.println("The most significant fact is: ");
                    } else {
                        System.out.println("The most significant facts are: ");
                    }

                    do {
                        if (order >= list.size()) {
                            list = cmdMap.get(++i);
                            order = 0;
                            if (list == null) {
                                System.out.println("No more facts.");
                                return;
                            }
                        }

                        for (int j = 0; j < i - 1; j++) {
                            System.out.println(((ExprList) list.get(order).getKey().formula).args.get(j));
                        }

                        System.out.println("");
                        System.out.print("Show next most significant fact/s? [y/n]: ");
                        order++;
                    } while ("y".equals(scan.nextLine()));

                    break;
                }
            }
        }
    }

    private boolean checkCounterexamplesMax(Map<Integer, List<Pair<Command, Integer>>> cmdMap) {
        for (List<Pair<Command, Integer>> list : cmdMap.values()) {
            for(Pair<Command, Integer> p : list) {
                if(!CE_MAX.equals(p.getValue()))
                    return false;
            }
        }
        return true;
    }
}
