/**
 * Project 1: Java
 *
 * This class contains basic implementations of select Java math and array features.
 *
 * @author Stephen Allan (swa9846)
 * @version August 22, 2016
 */
public class Project1 {

    /**
     * Calculate the modulus of two numbers in the form of 'dividend mod divisor'.
     * A modulus is the remainder value which results from a division operation.
     *
     * @param dividend Number to be divided
     * @param divisor  Number to divide by
     * @return Remainder after the dividend is divided by the divisor
     */
    public static int mod( int dividend, int divisor ) {
        if ( dividend == divisor )
            return 0;

        if ( dividend < divisor ) {
            if ( dividend >= 0 ) {
                return dividend;
            }
        }

        int posResult = dividend - divisor * (dividend / divisor);
        if ( dividend >= 0 ) {
            if ( divisor > 0 )
                return posResult;
        }
        if ( dividend <= 0 ) {
            if ( divisor < 0 )
                return posResult;
        }

        int result = 1;
        if ( dividend < 0 ) {
            dividend *= -1;
        }
        if ( divisor < 0 ) {
            divisor *= -1;
            result = -1;
        }

        int val1 = dividend;
        int val2 = divisor;
        while ( val2 < val1 ) {
            val2 += divisor;
        }

        result *= (val2 - val1);
        return result;
    }

    /**
     * Calculates the square root of the input integer value.
     *
     * @param x Number to square root
     * @return Square root of the input integer value
     */
    public static int sqrt( int x ) {

        if ( x == 0 ) return 0;

        int n = x / 2;
        int squareRoot = 1;
        for ( int i = 1; i <= n; ++i ) {
            int square = i * i;

            if ( square <= x )
                squareRoot = i;
            else
                break;
        }

        return squareRoot;
    }

    /**
     * Calculates the cube root of the input integer value.
     *
     * @param x Number to cube root
     * @return Cube root of the input integer value
     */
    public static int cbrt( int x ) {
        if ( x == 0 ) return 0;

        int negative = 0;
        if ( x < 0 ) {
            negative = 1;
            x *= -1;
        }

        int n = x / 3;
        int cubeRoot = 1;
        for ( int i = 1; i <= n; ++i ) {
            int cube = i * i * i;

            if ( cube <= x )
                cubeRoot = i;
            else
                break;
        }

        if ( negative == 1 ) {
            cubeRoot *= -1;
        }
        return cubeRoot;
    }

    /**
     * Calculates the greatest common divisor between two integers.
     * The gcd is the largest number which can divide both input values.
     *
     * @param x First number to calculate gcd for
     * @param y Second number to calculate gcd for
     * @return Largest number which can divide both input values
     */
    public static int gcd( int x, int y ) {
        if ( x < 0 ) x *= -1;
        if ( y < 0 ) y *= -1;

        while ( true ) {
            if ( x == y )
                return x;

            if ( x > y )
                x -= y;
            else
                y -= x;
        }
    }

    /**
     * Calculates the least common multiple of the two given integer values.
     *
     * @param x First number to calculate lcm for
     * @param y Second number to calculate lcm for
     * @return Lowest number which is a multiple of both input values
     */
    public static int lcm( int x, int y ) {
        if ( y > x ) {
            int tmp = x;
            x = y;
            y = tmp;
        }

        if ( mod( x, y ) == 0 )
            return x;

        int val1 = x;
        int val2 = y;
        while ( true ) {
            if ( val1 == val2 )
                return val1;

            if ( val1 > val2 )
                val2 += y;
            else
                val1 += x;
        }
    }

    /**
     * Sorts the given interger list in ascending order.
     * Returns a copy of the given array, the original is never modified.
     *
     * @param lst Input list to sort
     * @return Sorted copy of the original list
     */
    public static int[] sort( int[] lst ) {

        int[] sorted = lst.clone();

        for ( int i = 0; i < sorted.length; ++i ) {

            int minIndex = -1;
            int minimum = sorted[i];
            for ( int j = i + 1; j < sorted.length; ++j ) {
                if ( sorted[j] < minimum ) {
                    minimum = sorted[j];
                    minIndex = j;
                }
            }

            if ( minIndex == -1 ) continue;

            int tmp = sorted[i];
            sorted[i] = minimum;
            sorted[minIndex] = tmp;
        }

        return sorted;
    }

    /**
     * Searches through the given integer list to find the largest value.
     *
     * @param lst Input list from which to select the largest value
     * @return Largest value within the input list
     */
    public static int max( int[] lst ) {
        int maximum = lst[0];
        for ( int i = 1; i < lst.length; ++i ) {
            if ( lst[i] > maximum ) {
                maximum = lst[i];
            }
        }
        return maximum;
    }

    /**
     * Searches through the given integer list to find the lowest value.
     *
     * @param lst Input list from which to select the lowest value
     * @return Lowest value within the input list
     */
    public static int min( int[] lst ) {
        int minimum = lst[0];
        for ( int i = 1; i < lst.length; ++i ) {
            if ( lst[i] < minimum ) {
                minimum = lst[i];
            }
        }
        return minimum;
    }

    /**
     * Search for a given integer value within a given integer input list.
     *
     * @param lst   Input list in which to search for the given value
     * @param value Number to find in the input list
     * @return 1 if the value is found in the input list, 0 otherwise
     */
    public static int inList( int[] lst, int value ) {
        for ( int ele : lst ) {
            if ( value == ele ) {
                return 1;
            }
        }
        return 0;
    }

    /**
     * Calculates the average of all of the values within an interger list.
     *
     * @param lst Input list from which to find the average
     * @return Average of the values within the input list
     */
    public static int avgList( int[] lst ) {
        int average = 0;
        for ( int ele : lst ) {
            average += ele;
        }
        return average / lst.length;
    }

    /**
     * Calculates the median number from a given list of integers.
     *
     * @param lst Input list from which to find the median
     * @return Median number of the input list
     */
    public static int medList( int[] lst ) {
        int[] sorted = sort( lst );

        if ( mod( sorted.length, 2 ) == 0 ) {
            int index = sorted.length / 2;
            int med = sorted[index];
            med += sorted[index - 1];
            return med / 2;
        } else
            return sorted[sorted.length / 2];
    }
}
