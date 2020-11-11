package problems.qbfpt.solvers;

import solutions.Solution;

import java.io.IOException;
import java.util.ArrayList;

public class TS_QBFPT_PROB extends TS_QBFPT {

    private final Double probability;

    public TS_QBFPT_PROB(Integer tenure, Integer iterations, String filename, String localSearch, Double probability) throws IOException {
        super(tenure, iterations, filename, localSearch);
        this.probability = probability;
    }

    @Override
    public Solution<Integer> neighborhoodMove(){
        Double minCostDelta = Double.POSITIVE_INFINITY;
        Integer bestCandIn = null;
        Integer bestCandOut = null;
        ArrayList<Double> pIn = new ArrayList<>();
        ArrayList<Double> pOut = new ArrayList<>();
        double x;
        updateCL();

        for (Integer candIn : CL){
            x = rng.nextFloat();
            pIn.add(x);
            if (x < probability){
                Double deltaCost = ObjFunction.evaluateInsertionCost(candIn, currentSol);
                if (!TL.contains(candIn) || currentSol.cost + deltaCost < incumbentSol.cost){
                    if (deltaCost < minCostDelta){
                        minCostDelta = deltaCost;
                        bestCandIn = candIn;
                        if (!localSearch.equals("best-improving")){
                            break;
                        }
                    }
                }
            }
        }

        for (Integer candOut : currentSol){
            x = rng.nextFloat();
            pOut.add(x);
            if (x < probability){
                Double deltaCost = ObjFunction.evaluateRemovalCost(candOut, currentSol);
                if (!TL.contains(candOut) || currentSol.cost + deltaCost < incumbentSol.cost){
                    minCostDelta = deltaCost;
                    bestCandIn = null;
                    bestCandOut = candOut;
                    if (!localSearch.equals("best-improving")){
                        break;
                    }
                }
            }
        }

        EXIT:
        for (int i = 0; i < CL.size(); i++){
            if (i >= pIn.size()){
                x = rng.nextFloat();
                pIn.add(x);
            }
            if (pIn.get(i) < probability){
                Integer candIn = CL.get(i);
                for (int j = 0; j < currentSol.size(); j++){
                    if (j >= pOut.size()){
                        x = rng.nextFloat();
                        pOut.add(x);
                    }
                    if (pOut.get(j) < probability){
                        Integer candOut = currentSol.get(j);
                        Double deltaCost = ObjFunction.evaluateExchangeCost(candIn, candOut, currentSol);
                        if ((!TL.contains(candIn) && !TL.contains(candOut)) || currentSol.cost + deltaCost < incumbentSol.cost){
                            if (deltaCost < minCostDelta){
                                minCostDelta = deltaCost;
                                bestCandIn = candIn;
                                bestCandOut = candOut;
                                if (!localSearch.equals("best-improving")){
                                    break EXIT;
                                }
                            }
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
            TS_QBFPT tabuSearch = new TS_QBFPT_PROB(100, 1000, "instances/qbf020", "best-improving", 0.25);
            Solution<Integer> bestSol = tabuSearch.solve();
            System.out.println(bestSol);
            System.out.println("Time = " + (double)(System.currentTimeMillis() - startTime) / (double) 1000 + " seconds");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}