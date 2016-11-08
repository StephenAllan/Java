import java.util.Scanner;

/**
 * @author Carl Twyman
 * @author Stephen Allan
 *
 * File:    LongestIncreasingSubseqDP.java
 */
public class LongestIncreasingSubseqDP {

    /**
     * The incrSubseqDynamic function uses values it has previously calculated
     * to calculate the result for its current computation. It stores these
     * values in an array and once it completes returns the max number in this
     * array which represents the length of the longest subsequence in the array A.
     *
     * @param A
     * @return
     */
    public static int incrSubseqDynamic(int[] A) {
        int[] S = new int[A.length];
        int max = 0;
        for(int i = 0; i<A.length; i++) {
            S[i] = 1;
            for(int n = 0; n<i; n++) {
                if (A[n] < A[i] && S[i]<S[n]+1) {
                    S[i] = S[n]+1;
                }
            }
        }
        for(int x = 0; x<S.length; x++) {
            if(S[x] > max) {
                max = S[x];
            }
        }
        return max;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int j = scanner.nextInt();
        int[] A = new int[j];
        for(int i = 0; i<j; i++) {
            A[i] = scanner.nextInt();
        }
        System.out.println(incrSubseqDynamic(A));
    }
}
