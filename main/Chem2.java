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
  
ArrayList linea1 = new ArrayList<Integer>();
ArrayList linea2 = new ArrayList<Integer>();
ArrayList linea3 = new ArrayList<Integer>();
ArrayList linea4 = new ArrayList<Integer>();
ArrayList matrice = new ArrayList<ArrayList>();

Integer[] l1 = {1,0,-2,0,0};
Integer[] l2 = {1,0,0,0,-2};
Integer[] l3 = {3,0,-3,-1,0};
Integer[] l4 = {0,1,-1,-1,0};

for(int i=0; i<l1.length;i++){
    linea1.add(l1[i]);
    linea2.add(l2[i]);
    linea3.add(l3[i]);
    linea4.add(l4[i]);
}
matrice.add(linea1);
matrice.add(linea2);
matrice.add(linea3);
matrice.add(linea4);

System.out.println(matrice.toString());

double A[][] = new double[matrice.size()+2][((ArrayList)matrice.get(0)).size()];

for(int i=0;i<matrice.size();i++){
    for(int j=0; j<((ArrayList)matrice.get(0)).size();j++){
        A[i][j]= ((Integer)((ArrayList)matrice.get(i)).get(j)).doubleValue();
    }
}
for(int i=matrice.size();i<2+matrice.size();i++){
    for(int j=0; j<A[0].length;j++){
        A[i][j] = 1;
    }
}

/*
 * for(int i=0; i<A.length;i++){
    for(int j=0;j<A[0].length;j++){
        System.out.println(" " + A[i][j]);
    }
}
 */

 /*   double A[][] = {
        {1,0,-2,0,0},
        {1,0,0,0,-2},
        {3,0,-3,-1,0},
        {0,1,-1,-1,0},
        {1,1,1,1,1},
        {1,1,1,1,1},
    };
*/
    double b[] = new double[A.length];
    //double b[]  = {0,0,0,0,1,Double.NaN};
    
    ConsType[] rel = new ConsType[A.length];
    //ConsType[] rel = {ConsType.EQ, ConsType.EQ, ConsType.EQ, ConsType.EQ, ConsType.GE, ConsType.INT};

    for(int i = 0; i< A.length-2;i++){
        b[i] = 0;
        rel[i] = ConsType.EQ;
    }
    b[A.length-2] = 1;
    b[A.length-1] = Double.NaN;
    rel[A.length-2] = ConsType.GE;
    rel[A.length-1] = ConsType.INT;
    
    double c[] = new double[A[0].length];
    //double c[] = {1,1,1,1,1};

    for(int i=0;i<A[0].length;i++){
        c[i] = 1;
    }



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
