package problems.qbft;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import models.Triple;
import problems.Evaluator;
import solutions.Solution;

public class QBFT implements Evaluator<Integer>{

	/**
	 * Dimension of the domain.
	 */
	public final Integer size;

	/**
	 * The array of numbers representing the domain.
	 */
	public final Double[] variables;

	/**
	 * The matrix A of coefficients for the QBF f(x) = x'.A.x
	 */
	public Double[][] A;
	
	public List<Triple<Integer, Integer, Integer>> triples;

	/**
	 * The constructor for QuadracticBinaryFunction class. The filename of the
	 * input for setting matrix of coefficients A of the QBF. The dimension of
	 * the array of variables x is returned from the {@link #readInput} method.
	 * 
	 * @param filename
	 *            Name of the file containing the input for setting the QBF.
	 * @throws IOException
	 *             Necessary for I/O operations.
	 */
	public QBFT(String filename) throws IOException {
		size = readInput(filename);
		variables = allocateVariables();
		this.triples = generateTriples(size);
	}

	/**
	 * Evaluates the value of a solution by transforming it into a vector. This
	 * is required to perform the matrix multiplication which defines a QBF.
	 * 
	 * @param sol
	 *            the solution which will be evaluated.
	 */
	public void setVariables(Solution<Integer> sol) {
		resetVariables();
		if (!sol.isEmpty()) {
			for (Integer elem : sol) {
				variables[elem] = 1.0;
			}
		}

	}
	
	/**
	 * Responsible for setting the QBF function parameters by reading the
	 * necessary input from an external file. This method reads the domain's
	 * dimension and matrix {@link #A}.
	 * 
	 * @param filename
	 *            Name of the file containing the input for setting the black
	 *            box function.
	 * @return The dimension of the domain.
	 * @throws IOException
	 *             Necessary for I/O operations.
	 */
	protected Integer readInput(String filename) throws IOException {

		Reader fileInst = new BufferedReader(new FileReader(filename));
		StreamTokenizer stok = new StreamTokenizer(fileInst);

		stok.nextToken();
		Integer _size = (int) stok.nval;
		A = new Double[_size][_size];

		for (int i = 0; i < _size; i++) {
			for (int j = i; j < _size; j++) {
				stok.nextToken();
				A[i][j] = stok.nval;
				if (j>i)
					A[j][i] = 0.0;
			}
		}

		return _size;

	}

	/**
	 * Reserving the required memory for storing the values of the domain
	 * variables.
	 * 
	 * @return a pointer to the array of domain variables.
	 */
	protected Double[] allocateVariables() {
		Double[] _variables = new Double[size];
		return _variables;
	}

	/**
	 * Reset the domain variables to their default values.
	 */
	public void resetVariables() {
		Arrays.fill(variables, 0.0);
	}
	
	@Override
	public Integer getDomainSize() {
		return size;
	}

	@Override
	public Double evaluate(Solution<Integer> sol) {
		setVariables(sol);
		return sol.cost = evaluateQBFT();
	}
	
	/*
	 * Generation prohibited triple
	 * 
	 * */
	public List<Triple<Integer,Integer,Integer>> generateTriples(Integer n) {
		List<Triple<Integer,Integer,Integer>> triples = new ArrayList<Triple<Integer,Integer,Integer>>();
		
		for(Integer u = 1; u <= n; u++) {
			Integer g = this.generateG(u, n);
			Integer h = this.generateH(u, n, g);
			triples.add(new Triple<Integer, Integer, Integer>(u, g, h));
		}
		
		return triples;
	}
	
	public Integer generateL(Integer u, Integer pi1, Integer pi2, Integer n) {	
		return 1 + ((pi1 * (u - 1) + pi2) % n);
	}
	
	public Integer generateG(Integer u, Integer n) {
		Integer pi1 = 131;
		Integer pi2 = 1031;
		Integer l = this.generateL(u, pi1, pi2, n);
		
		return (l != u)? l : (1 + (l % n));
	}
	
	public Integer generateH(Integer u, Integer n, Integer g) {	
		Integer pi1 = 193;
		Integer pi2 = 1093;
		Integer l = this.generateL(u, pi1, pi2, n);
		
		if(l != u && l != g) {
			return l;
		} else {
			Integer aux = 1 + (l % n);
			if(aux != u && aux != g) {
				return aux;
			} else {
				return 1 + ((l + 1) % n);
			}
		}
	}
	
	/**
	 * Evaluates a QBF by calculating the matrix multiplication that defines the
	 * QBF: f(x) = x'.A.x .
	 * 
	 * @return The value of the QBF.
	 */
	public Double evaluateQBFT() {

		Double aux = (double) 0, sum = (double) 0;
		Double vecAux[] = new Double[size];

		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				aux += variables[j] * A[i][j];
			}
			vecAux[i] = aux;
			sum += aux * variables[i];
			aux = (double) 0;
		}

		return sum;

	}

	@Override
	public Double evaluateInsertionCost(Integer elem, Solution<Integer> sol) {
		if (!validateInsertion(elem, sol))
			return Double.NEGATIVE_INFINITY;
		setVariables(sol);
		return evaluateInsertionQBFT(elem);
	}
	
	/**
	 * Determines the contribution to the QBF objective function from the
	 * insertion of an element.
	 * 
	 * @param i
	 *            Index of the element being inserted into the solution.
	 * @return The variation of the objective function resulting from the
	 *         insertion.
	 */
	public Double evaluateInsertionQBFT(int i) {

		if (variables[i] == 1)
			return 0.0;

		return evaluateContributionQBFT(i);
	}

	/**
	 * Determines the contribution to the QBF objective function from the
	 * insertion of an element. This method is faster than evaluating the whole
	 * solution, since it uses the fact that only one line and one column from
	 * matrix A needs to be evaluated when inserting a new element into the
	 * solution. This method is different from {@link #evaluateInsertionQBFT(int)},
	 * since it disregards the fact that the element might already be in the
	 * solution.
	 * 
	 * @param i
	 *            index of the element being inserted into the solution.
	 * @return the variation of the objective function resulting from the
	 *         insertion.
	 */
	private Double evaluateContributionQBFT(int i) {

		Double sum = 0.0;

		for (int j = 0; j < size; j++) {
			if (i != j)
				sum += variables[j] * (A[i][j] + A[j][i]);
		}
		sum += A[i][i];

		return sum;
	}

	@Override
	public Double evaluateRemovalCost(Integer elem, Solution<Integer> sol) {
		if (!validateRemoval(elem, sol))
			return Double.NEGATIVE_INFINITY;
		setVariables(sol);
		return evaluateRemovalQBFT(elem);
	}

	/**
	 * Determines the contribution to the QBF objective function from the
	 * removal of an element.
	 * 
	 * @param i
	 *            Index of the element being removed from the solution.
	 * @return The variation of the objective function resulting from the
	 *         removal.
	 */
	public Double evaluateRemovalQBFT(int i) {

		if (variables[i] == 0)
			return 0.0;

		return -evaluateContributionQBFT(i);

	}
	
	@Override
	public Double evaluateExchangeCost(Integer elemIn, Integer elemOut, Solution<Integer> sol) {
		if (!validateExchange(elemIn, elemOut, sol))
			return Double.NEGATIVE_INFINITY;
		setVariables(sol);
		return evaluateExchangeQBFT(elemIn, elemOut);
	}
	
	/**
	 * Determines the contribution to the QBF objective function from the
	 * exchange of two elements one belonging to the solution and the other not.
	 * 
	 * @param in
	 *            The index of the element that is considered entering the
	 *            solution.
	 * @param out
	 *            The index of the element that is considered exiting the
	 *            solution.
	 * @return The variation of the objective function resulting from the
	 *         exchange.
	 */
	public Double evaluateExchangeQBFT(int in, int out) {

		Double sum = 0.0;

		if (in == out)
			return 0.0;
		if (variables[in] == 1)
			return evaluateRemovalQBFT(out);
		if (variables[out] == 0)
			return evaluateInsertionQBFT(in);

		sum += evaluateContributionQBFT(in);
		sum -= evaluateContributionQBFT(out);
		sum -= (A[in][out] + A[out][in]);

		return sum;
	}
	
	public Boolean validateInsertion(Integer elem, Solution<Integer> sol) {
		Solution<Integer> newSol = new Solution<Integer>();
		newSol.addAll(sol);
		newSol.add(elem);
		
		return validateConstraints(newSol);
	}

	public Boolean validateRemoval(Integer elem, Solution<Integer> sol) {
		Solution<Integer> newSol = new Solution<Integer>();
		newSol.addAll(sol);
		newSol.remove(elem);
		
		return validateConstraints(newSol);
	}

	public Boolean validateExchange(Integer elemIn, Integer elemOut, Solution<Integer> sol) {
		Solution<Integer> newSol = new Solution<Integer>();
		newSol.addAll(sol);
		newSol.add(elemIn);
		newSol.remove(elemOut);
		
		return validateConstraints(newSol);
	}

	public Boolean validateConstraints(Solution<Integer> sol) {
		// Triplet Constraint
		for (int i = 0; i < this.triples.size(); ++i) {
			if(!this.validateTriple(this.triples.get(i), sol)) {
				return false;
			}
		}
		return true;
	}
	
	public Boolean validateTriple(Triple<Integer, Integer, Integer> triple, Solution<Integer> sol) {
		return ((sol.contains(triple.getFirst())? 1 : 0) + 
				(sol.contains(triple.getSecond())? 1 : 0) + 
				(sol.contains(triple.getThird())? 1 : 0) <= 2);
	}
	

}
