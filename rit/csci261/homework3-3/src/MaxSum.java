import java.util.Scanner;

/**
 * Calculates the largest possible sum of all increasing subsequences from a list of integers.
 *
 * @author Stephen Allan (swa9846)
 * @author Carl Twyman (clt6234)
 * @version 10/14/16
 */
public class MaxSum {

    /**
     * Reads and tokenizes user input.
     * Initializes an array which is a copy the input array.
     * Calculates the sum of the all of the increasing numbers up
     * until that index, and stores the results in the new array.
     * Determines the largest element in the new array.
     * Displays the result to standard out.
     *
     * @param args Command line arguments
     */
    public static void main( String[] args ) {
        Scanner reader = new Scanner( System.in );

        // Read and parse input
        int numCount = reader.nextInt();

        long[] nums = new long[numCount];
        for ( int i = 0; i < numCount; ++i ) {
            nums[i] = reader.nextLong();
        }

        // Initialize sums array
        long[] sums = nums.clone();

        // Calculate the sums
        for ( int i = 1; i < nums.length; ++i ) {
            for ( int j = 0; j < i; ++j ) {
                if ( nums[i] > nums[j] && sums[i] < (nums[i] + sums[j]) )
                    sums[i] = nums[i] + sums[j];
            }
        }

        // Determine the largest sum
        long largestSum = 0;
        for ( int i = 0; i < sums.length; ++i ) {
            if ( sums[i] > largestSum )
                largestSum = sums[i];
        }

        // Display output
        System.out.println( largestSum );
    }

}
