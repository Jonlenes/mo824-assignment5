package problems.qbfpt.solvers;

import solutions.Solution;

import java.io.IOException;

public class TS_QBFPT_Diversification extends TS_QBFPT {

	private Integer diversificationStep;
	private Integer count = 0;
	
    public TS_QBFPT_Diversification(Integer tenure, Integer iterations, String filename, String localSearch, Integer diversificationStep) throws IOException {
        super(tenure, iterations, filename, localSearch);
        this.diversificationStep = diversificationStep; 
    }

    
	@Override
	public Solution<Integer> neighborhoodMove() {
		Double minDeltaCost;
		Integer bestCandIn = null, bestCandOut = null;

		minDeltaCost = Double.POSITIVE_INFINITY;
		count += 1;
		
		if (!this.currentSol.isEmpty() && count % diversificationStep == 0) {
			Integer candidate = null;
			Integer size = this.currentSol.size();
			Integer numCandidate = rng.nextInt(size);
			currentSol = createEmptySol();
			for (int i = 0; i < numCandidate; i++) {
				candidate = rng.nextInt(size);
				if (!TL.contains(candidate))
					currentSol.add(candidate);
			}
			ObjFunction.evaluate(currentSol);
		}
		
		updateCL();
		// Evaluate insertions
		for (Integer candIn : CL) {
			Double deltaCost = ObjFunction.evaluateInsertionCost(candIn, currentSol);
			if (!TL.contains(candIn) || currentSol.cost+deltaCost < incumbentSol.cost) {
				if (deltaCost < minDeltaCost) {
					minDeltaCost = deltaCost;
					bestCandIn = candIn;
					bestCandOut = null;
                    if (!localSearch.equals("best-improving")){
                        break;
                    }
				}
			}
		}
		// Evaluate removals
		for (Integer candOut : currentSol) {
			Double deltaCost = ObjFunction.evaluateRemovalCost(candOut, currentSol);
			if (!TL.contains(candOut) || currentSol.cost+deltaCost < incumbentSol.cost) {
				if (deltaCost < minDeltaCost) {
					minDeltaCost = deltaCost;
					bestCandIn = null;
					bestCandOut = candOut;
                    if (!localSearch.equals("best-improving")){
                        break;
                    }
				}
			}
		}
		// Evaluate exchanges
		Boolean stopSearch;
		for (Integer candIn : CL) {
			stopSearch = false;
			for (Integer candOut : currentSol) {
				Double deltaCost = ObjFunction.evaluateExchangeCost(candIn, candOut, currentSol);
				if ((!TL.contains(candIn) && !TL.contains(candOut)) || currentSol.cost+deltaCost < incumbentSol.cost) {
					if (deltaCost < minDeltaCost) {
						minDeltaCost = deltaCost;
						bestCandIn = candIn;
						bestCandOut = candOut;
                        if (!localSearch.equals("best-improving")){
                        	stopSearch = true;
                        	break;
                        }
					}
				}
			}
			if (stopSearch) break;
		}
		// Implement the best non-tabu move
		TL.poll();
		if (bestCandOut != null) {
			currentSol.remove(bestCandOut);
			CL.add(bestCandOut);
			TL.add(bestCandOut);
		} else {
			TL.add(fake);
		}
		TL.poll();
		if (bestCandIn != null) {
			currentSol.add(bestCandIn);
			CL.remove(bestCandIn);
			TL.add(bestCandIn);
		} else {
			TL.add(fake);
		}
		ObjFunction.evaluate(currentSol);
		
		return null;
	}
}