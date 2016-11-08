import java.io.FileNotFoundException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * TFTP Client to receive packets and verify their integrity.
 *
 * @author Stephen Allan (swa9846)
 * @version October 31, 2016
 */
public class HammingTFTP {


    // Private constants
    private static final String USAGE = "Usage: java HammingTFTP [error|noerror] TFTP-Host Filename";


    /**
     * Parse command line arguments.
     * Request the given filename from the TFTP server.
     *
     * @param args Command line arguments
     */
    public static void main( String[] args ) {

        // Verify arguments
        if ( args.length < 3 ) {
            usage();
        }

        boolean errors = true;
        String host = "";
        String filename = "";
        try {
            // Initialize variables
            if ( args[0].toLowerCase().equals( "noerror" ) ) {
                errors = false;
            } else if ( !args[0].toLowerCase().equals( "error" ) ) {
                usage();
            }

            host = args[1];
            filename = args[2];
        } catch ( Exception e ) {
            usage();
        }

        try {
            ServerProxy proxy = new ServerProxy( host );
            proxy.requestFile( errors, filename );
        } catch ( NullPointerException e ) {
            System.out.println( "Error received from server while trying to download file:" + e.getMessage() );
            System.out.println( "Terminating program." );
        } catch ( SocketTimeoutException e ) {
            System.out.println( "Timeout while trying to reach server. Terminating program." );
        } catch ( UnknownHostException e ) {
            System.out.println( "Could not find host \"" + host + "\". Terminating program." );
        } catch ( FileNotFoundException e ) {
            System.out.println( "Could not open file for writing. Terminating program." );
        } catch ( Exception e ) {
            System.out.println( "Communication error. Terminating program." );
        }
    }

    /**
     * Display a usage message to the console and close the program.
     */
    private static void usage() {
        System.out.println( USAGE );
        System.exit( 1 );
    }
}