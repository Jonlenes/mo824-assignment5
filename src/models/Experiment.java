package models;

import java.io.IOException;

import problems.qbfpt.solvers.TS_QBFPT;
import problems.qbfpt.solvers.TS_QBFPT_Diversification;
import problems.qbfpt.solvers.TS_QBFPT_PROB;

public class Experiment {
	private String key;
	private String localSearch;
	private String strategy;
	private Double probability;
	private Integer diversificationStep;
	
	public Experiment(String key, String localSearch, String strategy) {
		super();
		this.key = key;
		this.localSearch = localSearch;
		this.strategy = strategy;
		this.probability = 0.0;
		this.diversificationStep = Integer.MAX_VALUE;
	}
	
	public Experiment(String key, String localSearch, String strategy, Double probability) {
		super();
		this.key = key;
		this.localSearch = localSearch;
		this.strategy = strategy;
		this.probability = probability;
		this.diversificationStep = Integer.MAX_VALUE;
	}
	
	public Experiment(String key, String localSearch, String strategy, Integer diversificationStep) {
		super();
		this.key = key;
		this.localSearch = localSearch;
		this.strategy = strategy;
		this.probability = 0.0;
		this.diversificationStep = diversificationStep;
	}
	
	public Experiment(String key, String localSearch, String strategy, Double probability,
			Integer diversificationStep) {
		super();
		this.key = key;
		this.localSearch = localSearch;
		this.strategy = strategy;
		this.probability = probability;
		this.diversificationStep = diversificationStep;
	}
	
	public TS_QBFPT getModel(Integer tenure, Integer iterations, String filename) throws IOException {
		if (strategy.equals("default")) {
			return new TS_QBFPT(tenure, iterations, filename, localSearch);
		}
		else if (strategy.equals("prob")) {
			return new TS_QBFPT_PROB(tenure, iterations, filename, localSearch, probability);
		}
		return new TS_QBFPT_Diversification(tenure, iterations, filename, localSearch, diversificationStep);
	}

	public String getKey() {
		
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
	
}
