import java.util.Scanner;

/**
 * Calculates the smallest, most frequent values of a given list of integers.
 *
 * @author Stephen Allan (swa9846)
 * @author Carl Twyman (clt6234)
 * @version 9/7/16
 */
public class FindMaxPairs {

    private static long[] lst;
    private static long[] tmplst;

    /**
     * Reads and tokenizes user input.
     * Finds the number or sums from the input list.
     * Calculates the sums of the input list.
     * Sorts the list and determines the smallest, most frequent value.
     *
     * @param args Command line arguments
     */
    public static void main( String[] args ) {
        Scanner reader = new Scanner( System.in );

        // Read and parse input
        int numCount = reader.nextInt();

        long[] nums = new long[numCount];
        for( int i = 0; i < numCount; ++i ) {
            nums[i] = reader.nextLong();
        }

        // Calculate number of sums
        int tmpIndex = 0;
        for ( int i = 0; i < numCount ; ++i ) {
            for ( int j = i+1; j < numCount ; ++j ) {
                tmpIndex += 1;
            }
        }

        // Initialize sums list
        long[] sums = new long[tmpIndex];

        // Calculate sums
        int sumsIndex = 0;
        for ( int i = 0; i < numCount ; ++i ) {
            for ( int j = i+1; j < numCount ; ++j ) {
                sums[sumsIndex] = nums[i] + nums[j];
                sumsIndex += 1;
            }
        }

        // Sort sums list
        sort( sums );

        long result = 0;
        long resultCount = 0;
        long currCount = 0;
        long currNumber;
        long prevNumber;

        // Calculates the smallest, most frequent value
        for ( int i = 0; i < sums.length; ++i ) {
            try {
                prevNumber = sums[i - 1];
            } catch ( Exception e ) {
                prevNumber = sums[i];
            }
            currNumber = sums[i];

            if ( currNumber == prevNumber)
                currCount += 1;
            else
                currCount = 1;

            if ( currCount > resultCount ) {
                resultCount = currCount;
                result = currNumber;
            }
        }

        // Display output
        System.out.println( result );
    }

    /**
     * Sorts the input list in ascending order.
     * The input list will be modified to a sorted list.
     *
     * @param input List to be sorted
     */
    private static void sort( long[] input ) {
        lst = input;
        tmplst = new long[input.length];

        mergeSort( 0, input.length - 1 );
    }

    /**
     * Recursive function to handle the replacement of elements
     * in lst with the sorted elements from the input list.
     * Called through the sort function with appropriate setup and indices.
     *
     * @param start Starting index in the unsorted list
     * @param end Ending index in the unsorted list
     */
    private static void mergeSort( int start, int end ) {
        if ( start >= end ) return;

        int mid = start + (end - start) / 2;

        mergeSort( start, mid );
        mergeSort( mid + 1, end );

        for ( int i = start; i <= end; ++i ) {
            tmplst[i] = lst[i];
        }

        int i = start;
        int j = mid + 1;
        int k = start;

        for ( ;i <= mid && j <= end; ++k ) {
            if ( tmplst[i] <= tmplst[j] ) {
                lst[k] = tmplst[i];
                i += 1;
            } else {
                lst[k] = tmplst[j];
                j += 1;
            }
        }

        for ( ;i <= mid; ++k, ++i ) {
            lst[k] = tmplst[i];
        }
    }
}
