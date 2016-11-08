import java.util.Scanner;

/**
 * Calculates the sum of all of the integer input's squares.
 *
 * @author Stephen Allan (swa9846)
 * @version 9/31/16
 */
public class SumSquares {

    /**
     * Reads n number of integers from standard in, squares them, and totals the results.
     * Displays the sum to standard out.
     *
     * @param args Command line arguments
     */
    public static void main ( String[] args ) {

        // Read input
        Scanner input = new Scanner( System.in );
        int n = input.nextInt();

        // Calculate sum of squares
        int squares = 0;
        for ( int i = 0; i < n; ++i ) {
            int num = input.nextInt();
            squares += num * num;
        }

        // Display result
        System.out.println( squares );
    }
}