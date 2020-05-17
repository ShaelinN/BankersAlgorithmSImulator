package bankersalgorithm;

public class Simulation {

    public static void main(String[] args) throws InterruptedException {

        int[] avail = { 3, 14, 11, 12 };

        int[][] max = {
                { 0, 0, 1, 2 },
                { 1, 7, 5, 0 },
                { 2, 3, 5, 6 },
                { 0, 6, 5, 2 },
                { 0, 6, 5, 6 }
        };

        ResourceBank rBank = new ResourceBank(avail, max);

        for (int i = 0; i < max.length; i++) {
            Worker worker = new Worker(rBank, i);
            worker.start();
            if (i == max.length - 1) {
                worker.join();
            }
        }
    }
}
