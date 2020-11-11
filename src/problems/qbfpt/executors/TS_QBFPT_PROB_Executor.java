package problems.qbfpt.executors;

import models.Experiment;
import problems.qbf.solvers.TS_QBF;
import problems.qbfpt.solvers.TS_QBFPT_PROB;
import solutions.Solution;

import java.io.FileWriter;
import java.io.IOException;

public class TS_QBFPT_PROB_Executor {

    public static void main(String[] args) throws IOException {

        // Params
        String[] instances = {"qbf020", "qbf040", "qbf060", "qbf080", "qbf100", "qbf200", "qbf400"};
        Integer[] tenures = {20, 100};
        Integer iterations = 10000;

        // Experiments
        Experiment[] experiments = {
                new Experiment("FIRST_EXPERIMENT"),
        };


        for (String instance : instances) {
            FileWriter fileWriter = new FileWriter("results/" + instance + ".txt");

            for (Integer tenure : tenures) {
                for (Experiment experiment: experiments) {
                    try {
                        String expName = "TENURE=" + tenure + "_" + experiment.getKey();
                        System.out.println("\n\nINSTANCE:" + instance + "\tRUNNING EXPERIMENT: " + expName + "\n");

                        TS_QBF tabuSearch = new TS_QBFPT_PROB(tenure, iterations, "instances/" + instance, "best-improving", 0.25);
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