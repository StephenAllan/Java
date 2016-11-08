import java.util.Scanner;

/**
 * Find the longest convex subsequence in a given integer list.
 *
 * @author Stephen Allan (swa9846)
 * @author Carl Twyman (clt6234)
 * @version 10/28/16
 */
public class LongestConvexSubseq {


    /**
     * Reads and tokenizes user input.
     * Finds and displays the number of nodes in the
     * longest convex subsequence in the given input list.
     *
     * @param args Command line arguments
     */
    public static void main( String[] args ) {
        LongestConvexSubseq obj = new LongestConvexSubseq();
        Scanner reader = new Scanner( System.in );

        // Read and parse input
        int numCount = reader.nextInt();

        int[] nums = new int[numCount];

        for ( int i = 0; i < numCount; ++i ) {
            nums[i] = reader.nextInt();
        }

        // Display output
        System.out.println( obj.calculateLongestSubsequence( nums ) );
    }

    /**
     * Search for the longest convex subsequence in the given input list.
     *
     * @param input Integer list of value from which to find the longest convex subsequence
     * @return The number of nodes in the longest subsequence
     */
    public int calculateLongestSubsequence( int[] input ) {
        Node[] nodes = new Node[input.length];
        int longestPath = 0;

        for ( int i = 0; i < input.length; ++i ) {
            nodes[i] = new Node();

            for ( int j = i - 1; j >= 0; --j ) {
                Node tmpNode = new Node();
                int sum = 0;
                boolean addToCount = false;

                if ( nodes[j].previousIndex >= 0 ) {
                    sum = input[i] + input[nodes[j].previousIndex];
                } else {
                    addToCount = true;
                }


                if ( addToCount || sum >= (2 * input[j]) ) {
                    tmpNode.previousIndex = j;
                    tmpNode.pathLength += nodes[j].pathLength;
                }

                if ( tmpNode.pathLength > nodes[i].pathLength ) {
                    nodes[i] = tmpNode;
                }
            }

            if ( nodes[i].pathLength > longestPath ) {
                longestPath = nodes[i].pathLength;
            }
        }

        return longestPath;
    }

    /**
     * Private node class to hold information about the length of the path to
     * reach this node, and the index of the node before this one in the path.
     */
    private class Node {

        int previousIndex = -1;
        int pathLength = 1;
    }
}