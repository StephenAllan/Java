import java.util.Scanner;

/**
 * This class contains the DoubleKnapsack algorithm which determines
 * the largest total value of the items that can be placed in two knapsacks
 * with limited capacities.
 *
 * @author Carl Twyman
 * @author Stephen Allan
 * Homework 4 Problem 3
 * 10/28/16
 */

public class DoubleKnapsack {
    private static int[] indexes;

    /**
     * The knapsack method uses dynamic programming to calculate the subset of
     * items that has the largest combined value amongst it's items. This subset
     * must have a combined weight that is less than the total capacity of the
     * knapsack. The sums integer array is the matrix that contains the total values
     * for all of the subsets. Weights contains the total weights for each subset.
     *
     * @param n - int value representing the total number of items
     * @param w - int value representing the capacity of the first knapsack.
     * @param weight - int[] that contains the different weight values for each item.
     * @param cost - int[] that contains the different cost values associated with each item.
     * @return returns an integer representing the total value of the items contained in the knapsack.
     */
    public static int knapsack(int n, int w, int[] weight, int[] cost) {
        int[][] sums = new int[n+1][w+1];
        int[][] weights = new int[n+1][w+1];
        indexes = new int[n];
        int max;

        //Set the entire first row of the sums matrix to zero (no items added)
        for(int j = 0; j < w; j++) {
            sums[0][j] = 0;
            weights[0][j] = 0;
        }


        /** Calculate the total value of the items added to the knapsack.
         * Compares to the previously calculated values to determine whether
         * a new item has been added to the knapsack or if the value should just
         * remain the same. Values are stored at the location correlating to what
         * item they are in the list of items, and the weight associated with that item.
         */
        for(int x = 1; x <= n; x++) {
            for(int y = 0; y <= w; y++) {
                if(weight[x-1] > y) {
                    sums[x][y] = sums[x-1][y];
                    weights[x][y] = weights[x-1][y];
                }
                else {
                    if(sums[x-1][y] > sums[x-1][y-weight[x-1]] + cost[x-1]) {
                        sums[x][y] = sums[x-1][y];
                        weights[x][y] = weights[x-1][y];
                    }
                    else {
                        sums[x][y] = sums[x-1][y-weight[x-1]] + cost[x-1];
                        weights[x][y] = weights[x-1][y-weight[x-1]] + weight[x-1];
                    }
                }
            }
        }
        /** Determine what items were added to the knapsack. If the cost difference between
         * the current location and the location in the matrix pertaining to that item's weight
         * being subtracted is equal to the cost associated with the current item then that item
         * was in the list and store it's index in the indexes array to be removed from the second
         * knapsack's calculations.
         */
        int f = weights[n][w];
        int z = n;
        while(z > 0) {
           if (f - weight[z - 1] >= 0) {
                if (sums[z][f] - sums[z - 1][f - weight[z - 1]] == cost[z - 1]) {
                    indexes[z - 1] = z - 1;
                    f -= weight[z - 1];
                    z--;
                } else {
                    z--;
                }
            } else {
                z--;
            }
        }
        max = sums[n][w];
        return max;
    }

    /**
     * The main program reads in the inputs from System.in that pertain to the
     * total number of items in the items array, and the weights/values associated
     * with each item. Then call the knapsack method to calculate the items in the
     * first knapsack and then call the knapsack method a second time to calculate the
     * items in the second knapsack. Print out the total value of the two knapsacks.
     *
     * @param args
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int n = scanner.nextInt();
        int w1 = scanner.nextInt();
        int w2 = scanner.nextInt();
        int[] weight1 = new int[n];
        int[] cost1 = new int[n];
        int[] weight2 = new int[n];
        int[] cost2 = new int[n];


        for(int i = 0; i < n; ++i) {
            weight1[i] = scanner.nextInt();
            cost1[i] = scanner.nextInt();
        }
        int bag1 = knapsack(n, w1, weight1, cost1);

        //If an item's index is not in the indexes array, add it to the item array
        // that can be used for the second knapsack's calculations.

        for(int b = 0; b < n; b++) {
            if(indexes[b] == 0) {
                weight2[b] = weight1[b];
                cost2[b] = cost1[b];
            }
        }
        int bag2 = knapsack(n, w2, weight2, cost2);
        System.out.println(bag1+bag2);
    }
}
