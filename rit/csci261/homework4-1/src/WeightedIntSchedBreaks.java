import java.util.Scanner;

/**
 * Finds the longest sequence of intervals which do not overlap.
 *
 * @author Stephen Allan (swa9846)
 * @author Carl Twyman (clt6234)
 * @version 10/28/16
 */
public class WeightedIntSchedBreaks {


    /**
     * Reads and tokenizes user input.
     * Stores all 3 input arrays.
     * Calculate the longest sequence of non-overlapping intervals.
     *
     * @param args Command line arguments
     */
    public static void main( String[] args ) {
        WeightedIntSchedBreaks obj = new WeightedIntSchedBreaks();
        Scanner reader = new Scanner( System.in );

        // Read and parse input
        int numCount = reader.nextInt();

        int[] start = new int[numCount];
        int[] finish = new int[numCount];

        for ( int i = 0; i < numCount; ++i ) {
            start[i] = reader.nextInt();
            finish[i] = reader.nextInt();
        }

        int[][] between = new int[numCount][numCount];
        for ( int i = 0; i < numCount; ++i ) {
            for ( int j = 0; j < numCount; ++j ) {
                between[i][j] = reader.nextInt();
            }
        }

        // Display output
        System.out.println( obj.calculateLongestSchedule( start, finish, between ) );
    }

    /**
     * Creates an array of intervals for storing the original index before sorting.
     * Sorts the intervals by finish time.
     * Dynamically loops over the intervals, calculating the longest length from previous nodes to that node.
     * Calculates the longest sequence of non-overlapping intervals in a schedule.
     *
     * @param start   Starting times of all of the intervals
     * @param finish  Finishing times of all of the intervals
     * @param between Time it takes to get from one interval to every other interval
     * @return The number of intervals in the longest sequence of non-overlapping intervals
     */
    public int calculateLongestSchedule( int[] start, int[] finish, int[][] between ) {

        // Create intervals array
        Interval[] intervals = new Interval[finish.length];
        for ( int i = 0; i < finish.length; ++i ) {
            intervals[i] = new Interval( i );
        }

        // Sort intervals array
        for ( int i = 0; i < intervals.length; ++i ) {
            for ( int j = i + 1; j < intervals.length; ++j ) {
                if ( finish[intervals[i].inputIndex] > finish[intervals[j].inputIndex] ) {
                    Interval tmp = intervals[i];
                    intervals[i] = intervals[j];
                    intervals[j] = tmp;
                }
            }
        }

        // Calculate the length of the longest path until index I
        int[] len = new int[finish.length];
        for ( int i = 1; i < intervals.length; ++i ) {
            for ( int j = i - 1; j >= 0; --j ) {
                if ( ((finish[intervals[j].inputIndex] + between[intervals[j].inputIndex][intervals[i].inputIndex])
                        <= start[intervals[i].inputIndex]) && (len[j] + 1 > len[i]) ) {
                        len[i] = len[j] + 1;
                }
            }
        }

        // Determine the longest of the lengths
        int largestDistance = 0;
        for ( int i = 0; i < len.length; ++i ) {
            if ( len[i] > largestDistance ) {
                largestDistance = len[i];
            }
        }

        // Instead of looping over the len array and initializing all values to 1, just add one before returning
        return largestDistance + 1;
    }


    /**
     * Private class to hold information about an individual interval.
     */
    private class Interval {
        public int inputIndex;

        public Interval( int inputIndex ) {
            this.inputIndex = inputIndex;
        }
    }

}