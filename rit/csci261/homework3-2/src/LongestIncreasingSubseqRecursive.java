import java.util.Scanner;

/**
 * @author Carl Twyman
 * @author Stephen Allan
 *
 * File: LongestIncreasingSubseqRecursive.java
 */
public class LongestIncreasingSubseqRecursive {

    /**
     * Helper function for the incrSubseqRecursive method. This method determines
     * the longest increasing subsequence in A recursively. It takes in the current
     * position in the list, the size of the list, the array, and the last number
     * added to the subsequence.
     *
     * Returns the max length of subsequences in the array.
     * @param i
     * @param j
     * @param A
     * @param x
     */
    public static int incrSubseqRecursiveHelp(int i, int j, int[] A, int x) {
        if(i == j) {
            return 0;
        }
        int count1 = 0;
        if(A[i] > x) {
            count1 = 1 + incrSubseqRecursiveHelp(i+1, j, A, A[i]);
        }
        int count2 = incrSubseqRecursiveHelp(i+1, j, A, x);
        if(count1 > count2) {
            return count1;
        } else {
            return count2;
        }
    }

    /**
     * The incrSubseqRecursive method returns the length of the longest
     * increasing subsequence in the array A. It takes in the int[] A, and
     * the length of the array.
     *
     * @param j
     * @param A
     */
    public static int incrSubseqRecursive(int j, int[] A) {
        int maxCount = 0;
        for(int i=0; i<j; i++) {
            int count = incrSubseqRecursiveHelp(i+1,j,A,A[i]);
            if(count > maxCount) {
                maxCount = count;
            }
        }
        return maxCount;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int j = scanner.nextInt();
        int[] A = new int[j];
        for(int i = 1; i<j; i++) {
            A[i] = scanner.nextInt();
        }
        System.out.println(incrSubseqRecursive(j,A));
    }
}
