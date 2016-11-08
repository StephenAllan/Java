import java.util.Scanner;

/**
 * Determines if any element occurs half and a third of the time in the input list.
 *
 * @author Stephen Allan (swa9846)
 * @author Carl Twyman (clt6234)
 * @version 9/21/16
 */
public class Majority {

    /**
     * Reads and tokenizes user input.
     * Determines the largest number within the input list.
     * Creates an auxiliary array and stores the occurrences
     * of each of the input elements in that array.
     * Determines if the auxiliary array has any elements of values greater than
     * the side of the input array / 2, and the size of the input array / 3.
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

        // Find largest element
        int maximum = nums[0];
        for ( int i = 1; i < nums.length; ++i ) {
            if ( nums[i] > maximum )
                maximum = nums[i];
        }

        // Create an array of size of largest element + 1
        long[] occurrences = new long[maximum + 1];

        // loop over nums and use nums[i] as index into occurrences
        // num[i] will be the element/index
        // occurrences[j] will be the number of occurrences of element j
        // i.e. {1,2,4,3,2,3} will produce {0,1,2,2,1}
        for ( int i = 0; i < nums.length; ++i ) {
            occurrences[nums[i]] += 1;
        }

        // loop over occurrences to determine if any value is > numCount/2 [Part A]
        if ( determineMajority( occurrences, numCount / 2 ) == 1)
            System.out.println( "YES" );
        else
            System.out.println( "NO" );

        // loop over occurrences to determine if any value is > numCount/3 [Part B]
        if ( determineMajority( occurrences, numCount / 3 ) == 1)
            System.out.println( "YES" );
        else
            System.out.println( "NO" );
    }

    /**
     * Loops over the elements of lst to determine if any element holds a majority value.
     *
     * @param lst      Input list of integers
     * @param majority Value which determines if an occurrence is a majority
     * @return  1 if any element has a majority count, 0 otherwise
     */
    private static int determineMajority( long[] lst, int majority ) {
        for ( int i = 0; i < lst.length; ++i) {
            if ( lst[i] > majority )
                return 1;
        }
        return 0;
    }

}
