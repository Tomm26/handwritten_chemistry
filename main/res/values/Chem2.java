import java.util.ArrayList;

import org.ssclab.log.SscLogger;
import org.ssclab.pl.milp.ConsType;
import org.ssclab.pl.milp.Constraint;
import org.ssclab.pl.milp.GoalType;
import org.ssclab.pl.milp.LinearObjectiveFunction;
import org.ssclab.pl.milp.MILP;
import org.ssclab.pl.milp.Solution;
import org.ssclab.pl.milp.SolutionType;
import org.ssclab.pl.milp.Variable;

public class Chem2 {

    public static void main(String[] args) {
    // Reazione chimica
//    String reaction = "KNO3 + C = K2CO3 + CO + N2";
    
    double A[][] = {
        {1,0,-2,0,0},
        {1,0,0,0,-2},
        {3,0,-3,-1,0},
        {0,1,-1,-1,0},
        {1,1,1,1,1},
        {1,1,1,1,1},
    };
    double b[]  = {0,0,0,0,1,Double.NaN};
    double c[] = {1,1,1,1,1};

    ConsType[] rel = {ConsType.EQ, ConsType.EQ, ConsType.EQ, ConsType.EQ, ConsType.GE, ConsType.INT};

try {
    LinearObjectiveFunction f = new LinearObjectiveFunction(c, GoalType.MIN);

    ArrayList <Constraint> constraints = new ArrayList<>();
    for(int i = 0; i< A.length; i++){
        constraints.add(new Constraint(A[i], rel[i], b[i]));
    }

    MILP lp = new MILP(f,constraints);
    SolutionType solution_Type = lp.resolve();

    
    if(solution_Type == SolutionType.OPTIMUM){
        Solution solution = lp.getSolution();
        for(Variable var:solution.getVariables()) {
                SscLogger.log("Nome variabile :"+var.getName() + " valore:"+var.getValue());
            }
            SscLogger.log("Valore ottimo:"+solution.getOptimumValue());
        }// fine if

} catch (Exception e) {
    System.out.println(e.getMessage());
}

    }// fine main
}
