import java.util.Scanner;

/**
 * An RFC959 compliant FTP client.
 *
 * @author Stephen Allan (swa9846)
 * @version September 26, 2016
 */
public class FTP {


    // Private constants
    private static final String PROMPT = "ftp> ";
    private static final String UNKNOWN_COMMAND = "Unknown command. Type \"help\" for a list of supported commands.";
    private static final String EXCEPTION_MESSAGE = "Communication error. Terminating client.";
    private static final String USAGE = "Usage: java FTP <ServerName> [<PortNumber>]";

    // Private global variables
    private static Scanner scanner = new Scanner( System.in );


    /**
     * Parse command line arguments for required input.
     * Login into the specified server and begin reading commands from the user.
     * Close the program on manual disconnection or server read/write error.
     *
     * @param args Command line arguments
     */
    public static void main( String[] args ) {

        try {
            ServerProxy proxy = null;

            // Verify arguments and initialize
            if ( args.length < 1 ) {
                usage();
            }
            else if ( args.length == 1 ) {
                proxy = new ServerProxy( args[0], 21 );
            }
            else {
                try {
                    proxy = new ServerProxy( args[0], Integer.parseInt( args[1] ) );
                } catch ( NumberFormatException e ) {
                    usage();
                }
            }

            // Login
            proxy.login();

            // Start prompt loop
            while ( true ) {
                displayMessage( PROMPT, false );
                String[] token = getUserInput().split( " " );

                switch ( token[0].toLowerCase() ) {
                    case "ascii":
                        proxy.setAsciiMode();
                        break;

                    case "binary":
                        proxy.setBinaryMode();
                        break;

                    case "cd":
                        if ( token.length < 2 )
                            displayMessage( proxy.commandListing() );
                        else
                            proxy.changeDirectory( token[1] );
                        break;

                    case "cdup":
                        proxy.changeDirectory();
                        break;

                    case "debug":
                        proxy.changeDebugMode();
                        displayMessage( "Debug mode: " + proxy.getDebugMode() );
                        break;

                    case "dir":
                        proxy.getDirectoryListing();
                        break;

                    case "get":
                        if ( token.length < 2 )
                            displayMessage( proxy.commandListing() );
                        else
                            proxy.retrieveFile( token[1] );
                        break;

                    case "help":
                        displayMessage( proxy.commandListing() );
                        break;

                    case "passive":
                        proxy.changePassiveMode();
                        displayMessage( "Passive mode: " + proxy.getPassiveMode() );
                        break;

                    case "pwd":
                        proxy.printWorkingDirectory();
                        break;

                    case "quit":
                    case "exit":
                        proxy.quit();
                        return;

                    default:
                        displayMessage( UNKNOWN_COMMAND );
                        break;
                }
            }
        } catch ( Exception e ) {
            displayMessage( EXCEPTION_MESSAGE );
            System.exit( 1 );
        }
    }

    /**
     * Static function to read user input from the global scanner.
     *
     * @return String input from the user
     */
    public static String getUserInput() {
        return scanner.nextLine();
    }

    /**
     * Static function to write to standard out.
     * Default functionality prints a newline after the given message.
     *
     * @param message String to display to standard out
     */
    public static void displayMessage( String message ) {
        displayMessage( message, true );
    }

    /**
     * Static function to write to standard out.
     *
     * @param message String to display to standard out
     * @param newLine true to include a newline character, false otherwise
     */
    public static void displayMessage( String message, boolean newLine ) {
        if ( newLine )
            System.out.println( message );
        else
            System.out.print( message );
    }

    /**
     * Display a usage message to the console and close the program.
     */
    private static void usage() {
        displayMessage( USAGE );
        System.exit( 1 );
    }

}