import edu.mit.csail.sdg.alloy4.ConstList;
import edu.mit.csail.sdg.alloy4.ErrorSyntax;
import edu.mit.csail.sdg.alloy4compiler.ast.Command;
import edu.mit.csail.sdg.alloy4compiler.ast.Expr;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class AlloyFactCombinator {

    public static List<Command> generatePowerCombinations(Command cmd) throws ErrorSyntax {
        List<Command> cmds = new ArrayList<>();

        List<List<Expr>> powerSet = new LinkedList<>();
        powerSet.add(new LinkedList<Expr>());

        ConstList<Expr> args = ((ExprList) cmd.formula).args;
        int cantArgs = args.size() - 1;

        List<Expr> combinableArgs = new ArrayList<>();
        for (int i = 0; i < cantArgs; i++) {
            combinableArgs.add(args.get(i));
        }

        for (int i = 1; i <= cantArgs; i++)
            powerSet.addAll(combination(combinableArgs, i));


        for (List<Expr> combination : powerSet) {
            combination.add(args.get(cantArgs));
            ExprList formula = ExprList.make(cmd.formula.pos, cmd.formula.closingBracket, ((ExprList) cmd.formula).op,
                    ConstList.make(combination));

            Command cmdCombination = new Command(cmd.check, cmd.overall, cmd.bitwidth, cmd.maxseq, formula);
            cmds.add(cmdCombination);
        }

        return cmds;
    }

    private static <T> List<List<T>> combination(List<T> values, int size) {

        if (0 == size) {
            return Collections.singletonList(Collections.<T>emptyList());
        }

        if (values.isEmpty()) {
            return Collections.emptyList();
        }

        List<List<T>> combination = new LinkedList<List<T>>();

        T actual = values.iterator().next();

        List<T> subSet = new LinkedList<T>(values);
        subSet.remove(actual);

        List<List<T>> subSetCombination = combination(subSet, size - 1);

        for (List<T> set : subSetCombination) {
            List<T> newSet = new LinkedList<T>(set);
            newSet.add(0, actual);
            combination.add(newSet);
        }

        combination.addAll(combination(subSet, size));

        return combination;
    }

}
