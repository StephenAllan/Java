import java.util.Scanner;

/**
 * Calculates the largest sum of the longest increasing subsequences from a list of integers.
 *
 * @author Stephen Allan (swa9846)
 * @author Carl Twyman (clt6234)
 * @version 10/14/16
 */
public class MaxLongestSum {

    /**
     * Reads and tokenizes user input.
     * Initializes two arrays, 'len' to hold the length of the subsequence up to that index, and
     * 'trace' to hold the index in the nums array of the element before the current element in the subsequence.
     * Using a dynamic programming approach, loop through the input list,
     * calculating the largest increase subsequence up until that index.
     * Loop through the len array to find the longest sequence.
     * Trace that longest sequence back through the nums array, summing the elements along the way.
     * Display the resulting sum to standard out.
     *
     * @param args Command line arguments
     */
    public static void main( String[] args ) {
        Scanner reader = new Scanner( System.in );

        // Read and parse input
        int numCount = reader.nextInt();

        int[] nums = new int[numCount];
        for ( int i = 0; i < numCount; ++i ) {
            nums[i] = reader.nextInt();
        }

        // Initialize the length and trace arrays
        int[] len = new int[nums.length];
        int[] trace = new int[nums.length];

        // Set default values for both arrays
        for ( int i = 0; i < nums.length; ++i ) {
            len[i] = 1;
            trace[i] = i;
        }

        // Calculate the increasing subsequences
        for ( int i = 1; i < nums.length; ++i ) {
            for ( int j = 0; j < i; ++j ) {
                // If the current value at I is greater than the value at J,
                // and the length of the subsequence at I is less
                // than the length of the subsequence at J + 1,
                // Say the length of the subsequence at I is now the length of the subsequence
                // at J + 1, and that the number before I in the subsequence is J.
                if ( nums[i] > nums[j] && len[i] < (len[j] + 1) ) {
                    len[i] = len[j] + 1;
                    trace[i] = j;
                }
            }
        }

        // Determine the longest length
        int longestLengthIndex = 0;
        for ( int i = 0; i < len.length; ++i ) {
            if ( len[i] > len[longestLengthIndex] )
                longestLengthIndex = i;
        }

        // Sum all values of the longest length
        int prev = 0;
        int curr = longestLengthIndex;
        long largestSum = 0;
        while ( prev != curr ) {
            largestSum += nums[curr];
            prev = curr;
            curr = trace[prev];
        }

        // Display output
        System.out.println( largestSum );
    }

}
