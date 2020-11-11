package problems.qbfpt.solvers;

import problems.qbf.solvers.TS_QBF;
import problems.qbfpt.triples.ForbiddenTriplesGenerator;
import solutions.Solution;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TS_QBFPT extends TS_QBF {

    private final ForbiddenTriplesGenerator forbiddenTriplesGenerator = new ForbiddenTriplesGenerator(ObjFunction.getDomainSize());
    public String localSearch;

    public TS_QBFPT(Integer tenure, Integer iterations, String filename) throws IOException{
        super(tenure, iterations, filename);
    }

    public TS_QBFPT(Integer tenure, Integer iterations, String filename, String localSearch) throws IOException{
        this(tenure, iterations, filename);
        this.localSearch = localSearch;
    }

    @Override
    public void updateCL(){
        if (!this.incumbentSol.isEmpty()){
            List<Integer> forbiddenValues = new ArrayList<>();
            Integer lastElement = this.incumbentSol.get(this.incumbentSol.size() - 1);
            for (int i = 0; i < this.incumbentSol.size() - 1; i++){
                forbiddenValues.addAll(forbiddenTriplesGenerator.getForbiddenValues(this.incumbentSol.get(i) + 1, lastElement + 1));
            }
            for (Integer forbiddenValue : forbiddenValues){
                int index = CL.indexOf(forbiddenValue - 1);
                if (index >= 0){
                    CL.remove(index);
                }
            }
        }
    }

    @Override
    public Solution<Integer> neighborhoodMove(){
        Double minDeltaCost = Double.POSITIVE_INFINITY;
        Integer bestCandIn = null;
        Integer bestCandOut = null;
        updateCL();

        for (Integer candIn : CL){
            Double deltaCost = ObjFunction.evaluateInsertionCost(candIn, currentSol);
            if (!TL.contains(candIn) || currentSol.cost + deltaCost < incumbentSol.cost) {
                if (deltaCost < minDeltaCost){
                    minDeltaCost = deltaCost;
                    bestCandIn = candIn;
                    bestCandOut = null;
                    if (localSearch.equals("best-improving")){
                        break;
                    }
                }
            }
        }

        for (Integer candOut : currentSol){
            Double deltaCost = ObjFunction.evaluateRemovalCost(candOut, currentSol);
            if (!TL.contains(candOut) || currentSol.cost + deltaCost < incumbentSol.cost){
                if (deltaCost < minDeltaCost){
                    minDeltaCost = deltaCost;
                    bestCandIn = null;
                    bestCandOut = candOut;
                    if (localSearch.equals("best-improving")){
                        break;
                    }
                }
            }
        }

        EXIT:
        for (Integer candIn : CL){
            for (Integer candOut : currentSol){
                Double deltaCost = ObjFunction.evaluateExchangeCost(candIn, candOut, currentSol);
                if ((!TL.contains(candIn) && !TL.contains(candOut)) || currentSol.cost + deltaCost < incumbentSol.cost){
                    if (deltaCost < minDeltaCost){
                        minDeltaCost = deltaCost;
                        bestCandIn = candIn;
                        bestCandOut = candOut;
                        if (localSearch.equals("best-improving")){
                            break EXIT;
                        }
                    }
                }
            }
        }

        TL.poll();
        if (bestCandOut != null){
            currentSol.remove(bestCandOut);
            CL.add(bestCandOut);
            TL.add(bestCandOut);
        } else{
            TL.add(fake);
        }

        TL.poll();
        if (bestCandIn != null){
            currentSol.add(bestCandIn);
            CL.remove(bestCandIn);
            TL.remove(bestCandIn);
        } else {
            TL.add(fake);
        }

        ObjFunction.evaluate(currentSol);

        return null;
    }

    public static void main(String[] args){
        try {
            long startTime = System.currentTimeMillis();
            TS_QBFPT tabuSearch = new TS_QBFPT(20, 1000, "instances/qbf020", "best-improving");
            Solution<Integer> bestSol = tabuSearch.solve();
            System.out.println(bestSol);
            System.out.println("Time = " + (double)(System.currentTimeMillis() - startTime) / (double) 1000 + " seconds");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
