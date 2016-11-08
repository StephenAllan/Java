import java.util.Scanner;

/**
 * Calculates how many of the input integers can be added to achieve the target sum integer.
 *
 * @author Stephen Allan (swa9846)
 * @author Carl Twyman (clt6234)
 * @version 9/7/16
 */
public class SumT {

    private static int[] lst;
    private static int[] tmplst;
    private static long  sumCount = 0;

    /**
     * Reads and tokenizes user input.
     * Calculates how many input integers can be added together to achieve the target sum.
     * Displays the total number to standard out.
     *
     * @param args Command line arguments
     */
    public static void main( String[] args ) {
        Scanner reader = new Scanner( System.in );

        // Read and parse input
        int numCount = reader.nextInt();
        long target = reader.nextLong();

        int[] nums = new int[numCount];
        for( int i = 0; i < numCount; ++i ) {
            nums[i] = reader.nextInt();
        }

        // Sort the input list of integers
        sort( nums );

        // Determine the length of the two new indexing lists
        int lstLength = 0;
        int prevNumber, currNumber;
        for ( int i = 0; i < nums.length; ++i ) {
            try {
                prevNumber = nums[i-1];
            } catch ( Exception e ) {
                prevNumber = -1;
            }
            currNumber = nums[i];

            if ( prevNumber != currNumber ) {
                lstLength += 1;
            }
        }

        // Populate the two indexing lists
        int[] uniqueInputs = new int[lstLength];
        int[] occurrences = new int[lstLength];
        int index = -1;
        for ( int i = 0; i < nums.length; ++i) {
            try {
                prevNumber = nums[i-1];
            } catch ( Exception e ) {
                prevNumber = -1;
            }
            currNumber = nums[i];

            if ( prevNumber != currNumber ) {
                index += 1;
                uniqueInputs[index] = nums[i];
                occurrences[index] = 1;
            }
            else {
                occurrences[index] += 1;
            }
        }

        // Calculate total target sums
        for ( int i = 0; i < uniqueInputs.length; ++i ) {
            int foundIndex = search( uniqueInputs, target - uniqueInputs[i], i );

            if ( foundIndex >= 0 ) {

                // If we found multiple of ourselves
                if ( foundIndex == i ) {
                    int numOccurrences = occurrences[foundIndex];
                    if ( numOccurrences > 1 ) {

                        // Handle special case
                        if ( numOccurrences == 3 ) {
                            sumCount += numOccurrences;
                        }
                        // Handle even occurrences
                        else if ( numOccurrences % 2 == 0 ) {
                            long x = 1; // literals are treated as integers
                            long y = 2; // Java thinks long / int = int
                            sumCount += (numOccurrences / y) * (numOccurrences - x);
                        }
                        // Handle odd occurrences
                        else {
                            long x = 1;
                            long y = 2;
                            sumCount += (((numOccurrences - x) / y) * (numOccurrences - y)) + (numOccurrences - x) ;
                        }
                    }
                }

                // If we found a different number
                else {
                    sumCount += occurrences[foundIndex] * occurrences[i];
                }
            }
        }

        // Display output
        System.out.println( sumCount );
    }


    /**
     * Search for the given key in the given list.
     * Adds one to global sumCount if the value was found in the list.
     *
     * @param lst Input integer list
     * @param key Value to search for in the list
     */
    private static int search( int[] lst, long key, int start ) {
        return binarySearch( lst, key, start, lst.length );
    }


    /**
     * Recursive function to search through halves of the same list to find a value.
     * Called through the search function with appropriate setup and indices.
     *
     * @param lst Input integer list
     * @param key Value to search for in the list
     * @param start Starting index in the list
     * @param end Ending index in the list
     */
    private static int binarySearch( int[] lst, long key, int start, int end ) {
        if ( start >= end ) return -1;

        int mid = start + (end - start) / 2;

        if ( lst[mid] > key ) {
            return binarySearch( lst, key, start, mid );
        }
        else if ( lst[mid] < key ) {
            return binarySearch( lst, key, mid + 1, end );
        }
        else {
            return mid;
        }
    }

    /**
     * Sorts the input list in ascending order.
     * The input list will be modified to a sorted list.
     *
     * @param input List to be sorted
     */
    private static void sort( int[] input ) {
        lst = input;
        tmplst = new int[input.length];

        mergeSort( 0, input.length - 1 );
    }

    /**
     * Recursive function to handle the replacement of elements in lst with the sorted elements from the input list.
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
            }
            else {
                lst[k] = tmplst[j];
                j += 1;
            }
        }

        for ( ;i <= mid; ++k, ++i ) {
            lst[k] = tmplst[i];
        }
    }
}