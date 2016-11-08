import java.util.Scanner;

/**
 * Calculates the largest axis-parallel rectangle within the given coordinates.
 *
 * @author Stephen Allan (swa9846)
 * @author Carl Twyman (clt6234)
 * @version 9/21/16
 */
public class MaxRectangle {

    private static int[] stack;
    private static int   stackPointer = 0;
    private static int   maxRect = 0;

    /**
     * Reads and tokenizes user input.
     * Adds all of the input coordinates into a list.
     * Initializes the size of the stack and begins pushing elements onto it.
     * When a larger area is discovered, push the coordinate onto the stack.
     * When a smaller area is discovered pop from the stack
     * and calculate the rectangle of the largest found area.
     *
     * @param args Command line arguments
     */
    public static void main( String[] args ) {
        Scanner reader = new Scanner( System.in );

        int numCount = reader.nextInt() * 2;

        int[] coords = new int[numCount];
        for ( int i = 0; i < numCount; ++i ) {
            coords[i] = reader.nextInt();
        }

        stack = new int[numCount / 2];

        int i = 1;
        while ( i < coords.length ) {
            if ( isEmpty() || coords[i] >= coords[peek()] ) {
                push( i );
                i += 2;
            } else {
                calcRectangleArea( coords, i );
            }
        }

        i -= 2;
        while ( !isEmpty() ) {
            calcRectangleArea( coords, i );
        }

        System.out.println( maxRect );
    }


    /**
     * Uses the global stack to calculate the length and width of the rectangle.
     * Overwrites the maxRect variable if the newly calculated
     * rectangle is larger than the previously largest rectangle.
     *
     * @param coords Input integer coordinates
     * @param index  Index into the coords list
     */
    private static void calcRectangleArea( int[] coords, int index ) {
        int topIndex = pop();

        int rectLength = 0;
        if ( isEmpty() ) {
            rectLength = coords[index - 1];
        }
        else {
            rectLength = coords[index - 1] - coords[peek() - 1];
        }

        int area = coords[topIndex] * rectLength;
        if ( maxRect < area )
            maxRect = area;
    }

    /**
     * Pushes a value to the top of the global stack.
     *
     * @param value Element to push onto the stack.
     */
    private static void push( int value ) {
        stack[stackPointer] = value;
        stackPointer += 1;
    }

    /**
     * Returns the value of the top most element in the global stack.
     * Does not remove the element, nor adjust the stack pointer.
     *
     * @return  The top most element of the stack
     */
    private static int peek() {
        return stack[stackPointer - 1];
    }

    /**
     * Removes the top most element in the global stack.
     * decrements the stack pointer and deletes the element from the stack.
     *
     * @return  The top most element of the stack
     */
    private static int pop() {
        stackPointer -= 1;
        int result = stack[stackPointer];
        stack[stackPointer] = 0;
        return result;
    }

    /**
     * Determines if the global stack is empty using the stack pointer.
     *
     * @return  true if the stack is empty, false otherwise
     */
    private static boolean isEmpty() {
        return stackPointer <= 0;
    }

}