package bankersalgorithm;
/**
 *  This is a representation of the resources in a system
 *  it uses the banker's algorithm to distribute resources to different processes
 * it is thread safe
 *
 * it assumes a process will never ask for more than its agreed max need in resources
 * it does not deallocate resources when the thread/process ends
 * */

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

public class ResourceBank {
    private int[] available;
    private int[][] max;
    private int[][] allocation;
    private int[][] need;

    public Semaphore mutex = new Semaphore(1);



    public ResourceBank(int[] available, int[][] max) {
        /**
         *constructor
         * takes the max resource needs of all processes and the remaining available resources as parameters
         * */
        //avail
        this.available = available;

        //max
        this.max = max;

        //need
        this.need = new int[max.length][max[0].length];
        for (int i = 0; i < max.length; i++) {
            for (int j = 0; j < max[i].length; j++) {
                need[i][j] = max[i][j];
            }
        }

        //alloc
        this.allocation = new int[max.length][max[0].length];
        for (int i = 0; i < max.length; i++) {
            for (int j = 0; j < max[i].length; j++) {
                allocation[i][j] = 0;
            }
        }
    }

    public int getNumResourceTypes(){ return available.length;}

    public int[] getNeed(int workerIndex){return need[workerIndex];}


    public boolean requestResource(int[] request, int index) {
        /**
         * employs bankers algorithm to determine whether or not to grant a resource request
         * */

        //DEBUGGING:
        //printNeeds("as soon as bank gets request from" + index);


        //so the user can see the progress of the code
        //make a string to represent the request
        StringBuilder r = new StringBuilder("[");
        for (int i = 0; i < request.length; i++) {
            r.append(request[i]);
            if (i < request.length - 1) {
                r.append(",");
            } else {
                r.append("]");
            }
        }


        //if the system is in safe state
        if (isSafeState(available,allocation,need)) {

            //create copies of the vectors
            int[][] newAlloc = copyArray(allocation);
            int[] newAvail = copyArray(available);
            int[][] newNeed = copyArray(need);

            //update these copy/pretend vectors with the incoming request
            effectRequest(request,index,newAvail,newAlloc,newNeed);

            //DEBUGGING:
            //printNeeds("after pretending request to "+ index);

            //show the user the progress of the code
            System.out.println("Simulating request " + r + " for worker:" + index);

            //if the pretend scenario results in a safe state
            if (isSafeState(newAvail,newAlloc,newNeed)){

                //show the user the progress of the code
                System.out.println("Request " + r + " granted for worker:" + index);

                //grant the request in the actual system
                effectRequest(request,index,available,allocation,need);

                //free the mutex so another thread may request resources

                //DEBUGGING:
                //printNeeds("after granting request to "+ index);
                mutex.release();

                //notify the thread it's request was granted
                return true;
            }
            else {
                // not safe to grant request
                System.out.println("Cannot grant request " + r + " to worker:" + index);

                //free the mutex so another thread may request resources
                mutex.release();

                //notify the thread it's request was NOT granted
                return false;
            }
        }

        else {
            // not safe to start with
            //the program should not get to here

            System.out.println("System is not safe.");

            //free the mutex so another thread may request resources
            mutex.release();

            //notify the thread it's request was NOT granted
            return false;
        }
    }

    private void effectRequest(int[] request,int index, int[] avail, int[][] alloc, int[][] need) {
        /**
         *  'grants' the request
         *  it is reflected in the changes to the avail, need and alloc vector args
         *  it allows requests to be granted on a set of vectors that represents resource bank
         *  if these vectors are actually the fields of the resource bank it will create permanent changes to the state
         *  however it is only meant to be called on the fields of the bank if it is known that doing so will result in a safe state
         * */

        for (int j = 0; j < alloc[index].length; j++) {
            avail[j] -= request[j];
            alloc[index][j] += request[j];
            need[index][j] -= request[j];

        }
    }


    private static boolean isSafeState(int[]available, int[][]allocation, int need[][]){
        /**
         * the safe state check
         * returns whether or not the system has some course of action that will not cause a deadlock
         * */


        int[] work = available.clone();
        Queue<Integer>safeSequence = new LinkedList<>();
        while (true){

            //if there is some safe sequence that includes ALL threads
            if (safeSequence.size() == allocation.length){
                return true;
            }

            //else
            //keep track of the length of the safe sequence before looking for another process to add to it
            int prevSafeSequenceSize = safeSequence.size();

            for (int i = 0; i < allocation.length; i++) {

                //look only for processes not already accounted for in the safe sequence
                if (!safeSequence.contains(i)){

                    //check if every element in the need vector for the particular vector
                    // is less than the corresponding element in the work/available vector
                    boolean needLessThanWork = true;
                    for (int j = 0; j < need[i].length; j++) {
                        if (need[i][j] > work[j]) {
                            needLessThanWork = false;
                            break;
                        }
                    }
                    if (needLessThanWork){
                        work = vectorAdd(work,allocation[i]);
                        safeSequence.add(i);
                    }
                    //else continue
                }

            }

            //if no other processes were added to the safe sequence
            if(safeSequence.size() == prevSafeSequenceSize){
                /**
                *   if this was because all elements were already in the safeSequence, the if at the top of the while loop would have returned true
                *   therefore this means that there is a process that is in the system but not in the safeSequence
                *   thus the System is not in a safe state
                */
                return false;
            }

        }

    }

    private static int[] vectorAdd(int[] firstVector, int[] secondVector) {
        /**
         * simple 1D vector addition
         * to abstract the addition of work and allocation in the safe-state checks
         **/

        if (firstVector.length!=secondVector.length){
            //the program should not get here
            System.out.println("vectors of different length. Cannot be added");
            return null;
        }
        else {
            int[] answer = new int[firstVector.length];
            for (int i = 0; i < answer.length; i++) {
                answer[i] = firstVector[i]+secondVector[i];
            }
            return answer;
        }
    }


    public void deAllocateResources(int index) {
        /**
         * allows a thread to give up its allocated resources when it is done running
         * */

        try {
            mutex.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        available = vectorAdd(available,allocation[index]);

        for (int i = 0; i < need[index].length; i++) {
            need[index][i] = max[index][i];
            allocation[index][i] = 0;

        }

        mutex.release();
    }


    /**
     * the following methods allow an array ,1D and 2D, to be copied by value
     * both array.clone(), Arrays.copyOf() didn't work for this scenario so I just did it manually
     * */
    private int[][] copyArray( int[][] oldArray) {
        int[][] newArray = new int[oldArray.length][oldArray[0].length];
        for (int i = 0; i < oldArray.length; i++) {
            for (int j = 0; j < oldArray[0].length; j++) {
                newArray[i][j]=oldArray[i][j];
            }
        }
        return newArray;
    }
    private int[] copyArray( int[] oldArray) {
        int[] newArray = new int[oldArray.length];
        for (int i = 0; i < oldArray.length; i++) {
            newArray[i]=oldArray[i];
        }
        return newArray;
    }


}
