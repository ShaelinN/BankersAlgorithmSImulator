package bankersalgorithm;

import java.util.Random;

public class Worker extends Thread {

    // Thread number
    private int index;
    // for generating request vectors
    private Random random = new Random();
    // the Bank
    private ResourceBank rBank;

    int[] myNeed;


    public Worker(ResourceBank rBank, int index) {
        this.rBank = rBank;
        this.index = index;
        this.myNeed = rBank.getNeed(index);
    }

    public void run() {
        System.out.println("Worker " + index + " starting.");

        while (true) {
            // Sleep for random time (max 5s)
            sleepUpTo(5000);







            myNeed = rBank.getNeed(index); // update myneed with each loop

            if (zeroVector(myNeed)) {
                //if all needs are 0
                break; // exit loop
            }

            else {//request fraction of needs
                int[] request = new int[rBank.getNumResourceTypes()];

                for (int i = 0; i < request.length; i++) {
                    if (myNeed[i] != 0) {

                        try {
                            //request up to the full needed amount
                            //prevents asking for more than the max resources allowed for the process
                            request[i] = Math.max(1,random.nextInt(myNeed[i])); //ensures if the random bound i is 1, request[i] doesn't always become 0 and cause an infinite loop
                        }
                        catch (IllegalArgumentException e){
                            e.printStackTrace();

                        }

                    }
                    else {
                        request[i] = 0;
                    }
                }

                if (zeroVector(request)){
                    System.out.println(index+" requested zero resources");
                    continue;
                }





                //else
                try {
                    rBank.mutex.acquire();
                    if (!rBank.requestResource(request, index)) { // if bank cannot fulfill the portion of the request
                        break; // exit loop
                    }


                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


            }

        }

        System.out.println("Worker " + index + " exiting.");
        rBank.deAllocateResources(index);
    }






    private void sleepUpTo(int timeInMillis) {
        try {
            sleep(random.nextInt(5000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    private boolean zeroVector(int[] vector) {
        for (int i = 0; i < vector.length; i++) {
            if (vector[i] > 0){
                return false;
            }
        }
        return true;
    }

}
