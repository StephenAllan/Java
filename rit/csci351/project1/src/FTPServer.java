import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * An RFC959 compliant FTP server.
 *
 * @author Stephen Allan (swa9846)
 * @version September 26, 2016
 */
public class FTPServer {


    // Private constants
    private static final String USAGE = "Usage: java FTPServer [<PortNumber>]";
    private static final int TIMEOUT = 60000;


    /**
     * Parse command line arguments for required input.
     * Being accepting connections on the server.
     * When a client connects, log the client into the server and being a timeout on its connection.
     * Begin reading commands from the client.
     * Close connection to the client on communication error, client quit, or timeout.
     *
     * @param args Command line arguments
     */
    public static void main( String[] args ) {

        ServerSocket socket = null;
        ClientProxy proxy = null;

        // Verify arguments and initialize
        try {
            if ( args.length == 0 ) {
                socket = new ServerSocket( 2121 );
            }
            else {
                try {
                    socket = new ServerSocket( Integer.parseInt( args[0] ) );
                } catch ( NumberFormatException e ) {
                    usage();
                }
            }
        } catch ( Exception e ) {
            usage();
        }

        // Begin accepting connections
        while ( true ) {
            try {
                // Accept connecting client
                Socket client = socket.accept();
                client.setSoTimeout( TIMEOUT );

                // Setup proxy object
                proxy = new ClientProxy( client );

                // Welcome and login client
                proxy.welcome();
                if ( !proxy.login() ) continue;

                // Receive and parse client's commands
                while ( true ) {
                    String[] tokens = proxy.readCommand().split( " " );

                    switch ( tokens[0].toUpperCase() ) {
                        case "TYPE":
                            if ( tokens.length < 2 )
                                proxy.transferType( "" );
                            else
                                proxy.transferType( tokens[1] );
                            break;

                        case "CDUP":
                            proxy.directoryUp();
                            break;

                        case "CWD":
                            if ( tokens.length < 2 )
                                proxy.changeDirectory( "" );
                            else
                                proxy.changeDirectory( tokens[1] );
                            break;

                        case "PWD":
                            proxy.printWorkingDirectory();
                            break;

                        case "PASV":
                            proxy.passiveDataConnection();
                            break;

                        case "PORT":
                            if ( tokens.length < 2 )
                                proxy.activeDataConnection( "" );
                            else
                                proxy.activeDataConnection( tokens[1] );
                            break;

                        case "LIST":
                            proxy.directoryListing();
                            break;

                        case "RETR":
                            if ( tokens.length < 2 )
                                proxy.fileTransfer( "" );
                            else
                                proxy.fileTransfer( tokens[1] );
                            break;

                        case "QUIT":
                            proxy.quit();
                            return;

                        default:
                            proxy.unknownCommand();
                            break;
                    }
                }

            } catch ( SocketTimeoutException ex ) {
                try {
                    proxy.timeout();
                } catch ( IOException e ) {
                }
            } catch ( Exception e ) {
            }
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