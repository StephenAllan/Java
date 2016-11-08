import java.util.Scanner;

/**
 * Calculates all of the prime numbers below a given interger.
 * Displays the answer on standard out, one per line.
 *
 * @author Stephen Allan (swa9846)
 * @version 9/31/16
 */
public class Primes {

    /**
     * Calculates all of the prime numbers below a given input value.
     * Reads the input value from standard in.
     * Displays all of the prime numbers on standard out, one per line.
     *
     * @param args Command line arguments
     */
    public static void main( String[] args ) {

        // Read input
        Scanner input = new Scanner( System.in );
        int n = input.nextInt();

        if ( n < 2 ) return;

        // 2 is always prime
        System.out.println( "2" );

        // Find all other odd prime numbers
        for ( int i = 3; i <= n; i += 2) {
            for ( int j = 2; j <= i; ++j ) {
                if ( i % j == 0 ) {
                    if ( j == i ) {
                        System.out.println( j );
                    }
                    break;
                }
            }
        }
    }
}
