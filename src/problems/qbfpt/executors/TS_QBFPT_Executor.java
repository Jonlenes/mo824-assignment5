package problems.qbfpt.executors;

import models.Experiment;
import problems.qbf.solvers.TS_QBF;
import problems.qbfpt.solvers.TS_QBFPT_PROB;
import solutions.Solution;

import java.io.FileWriter;
import java.io.IOException;

public class TS_QBFPT_Executor {

    public static void main(String[] args) throws IOException {

        // Params
        String[] instances = {"qbf020", "qbf040", "qbf060", "qbf080", "qbf100", "qbf200", "qbf400"};
        Integer[] tenures = {20, 100};
        Integer iterations = 10000;

        // Experiments
        Experiment[] experiments = {
                new Experiment("DEFAULT_FIRST", "first-improving", "default"),
        		new Experiment("DEFAULT_BEST", "best-improving", "default"),
        		new Experiment("PROB_0.25_FIRST", "first-improving", "prob", 0.25),
        		new Experiment("PROB_0.25_BEST", "best-improving", "prob", 0.25),
        		new Experiment("PROB_0.5_FIRST", "first-improving", "prob", 0.5),
        		new Experiment("PROB_0.5_BEST", "best-improving", "prob", 0.5),
        		new Experiment("PROB_0.75_FIRST", "first-improving", "prob", 0.75),
        		new Experiment("PROB_0.75_BEST", "best-improving", "prob", 0.75),
                new Experiment("DIV_100_FIRST", "first-improving", "diversification", 100),
                new Experiment("DIV_100_BEST", "best-improving", "diversification", 100),
                new Experiment("DIV_10_FIRST", "first-improving", "diversification", 10),
                new Experiment("DIV_10_BEST", "best-improving", "diversification", 10),
        };


        for (String instance : instances) {
            FileWriter fileWriter = new FileWriter("results/" + instance + ".txt");

            for (Integer tenure : tenures) {
                for (Experiment experiment: experiments) {
                    try {
                        String expName = "TENURE=" + tenure + "_" + experiment.getKey();
                        System.out.println("\n\nINSTANCE:" + instance + "\tRUNNING EXPERIMENT: " + expName + "\n");

                        TS_QBF tabuSearch = experiment.getModel(tenure, iterations, "instances/" + instance);
                        executeInstance(expName, tabuSearch, fileWriter);

                    } catch (IOException e) {
                        e.printStackTrace();
                        System.out.println("Error reading instance or writing in file: "+instance);
                    }
                }
            }
            fileWriter.close();
        }
    }

    public static void executeInstance(String title, TS_QBF ts, FileWriter fileWriter) {
        long startTime = System.currentTimeMillis();
        Solution<Integer> bestSol = ts.solve();
        long endTime   = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        double time = (double)totalTime/(double)1000;

        System.out.println("Best Val = " + bestSol);
        System.out.println("Time = "+ time + " seg");

        if(fileWriter != null) {
            try {
                fileWriter.append(title + "\n");
                fileWriter.append("Best solution: " + bestSol + "\n");
                fileWriter.append("Time: " + time + "seg \n\n");
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Error writing in file: "+title);
            }
        }
    }
}