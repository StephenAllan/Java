import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * FTP proxy class for interacting with the specified FTP server.
 *
 * @author Stephen Allan (swa9846)
 * @version September 26, 2016
 */
public class ServerProxy {


    // Private constants
    private static final int BUFFER = 9216;
    private static final String DEBUG_TEXT = "---> ";
    private static final String COMMANDS = "    ascii           Set ASCII transfer type\n"
            + "    binary          Set binary transfer type\n"
            + "    cd <path>       Change remote working directory\n"
            + "    cdup            Change remote working directory to parent directory\n"
            + "    debug           Toggle debugging mode\n"
            + "    dir             List the contents of the remote directory\n"
            + "    get <filename>  Retrieve a file from the remote system\n"
            + "    help            Display this command listing\n"
            + "    passive         Toggle passive/active transfer mode\n"
            + "    pwd             Print the working directory on the server\n"
            + "    quit            Close the connection to the server and terminate the program";

    // Private global variables
    private boolean passive = false;
    private boolean debug = false;
    private String ipAddress;
    private Socket socket = new Socket();
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;


    /* Public Functions */


    /**
     * Constructor for the ServerProxy class.
     * Initialize the proxy's global variables.
     *
     * @param server FTP server to connect to
     * @param port   Port number to connect to the server on
     * @throws IOException If failed to connect to the server
     */
    public ServerProxy( String server, int port ) throws IOException {
        // Connect
        socket.connect( new InetSocketAddress( server, port ) );
        ipAddress = socket.getLocalAddress().toString().substring( 1 ).replace( '.', ',' );

        // Setup streams
        bufferedReader = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
        bufferedWriter = new BufferedWriter( new OutputStreamWriter( socket.getOutputStream() ) );
    }

    /**
     * Attempts to log into the connected FTP server.
     * Prompts the user for their username and password.
     * Exits the current running program if login fails.
     *
     * @throws IOException If failed to read from or write to the server
     */
    public void login() throws IOException {
        // Get welcome message
        readStream();

        // Send Username
        FTP.displayMessage( "Enter Username: ", false );
        writeStream( "USER " + FTP.getUserInput() );

        String line = readStream();
        assertStatus( line, 331, true, "Login failed." );

        // Send password
        FTP.displayMessage( "Enter Password: ", false );
        writeStream( "PASS " + FTP.getUserInput() );

        line = readStream();
        assertStatus( line, 230, true, "Login failed." );
    }

    /**
     * Sets the server's transfer mode to ASCII.
     *
     * @throws IOException If failed to read from or write to the server
     */
    public void setAsciiMode() throws IOException {
        writeStream( "TYPE A" );
        readStream();
    }

    /**
     * Sets the server's transfer mode to binary.
     *
     * @throws IOException If failed to read from or write to the server
     */
    public void setBinaryMode() throws IOException {
        writeStream( "TYPE I" );
        readStream();
    }

    /**
     * Changes the current working directory to the parent directory.
     *
     * @throws IOException If failed to read from or write to the server
     */
    public void changeDirectory() throws IOException {
        writeStream( "CDUP" );
        readStream();
    }

    /**
     * Changes the current working directory to the given path.
     *
     * @param path Path of the directory to set as the working directory
     * @throws IOException If failed to read from or write to the server
     */
    public void changeDirectory( String path ) throws IOException {
        writeStream( "CWD " + path );
        readStream();
    }

    /**
     * If in debug mode, changes the proxy to quite mode.
     * If in quite mode, changes the proxy to debug mode.
     */
    public void changeDebugMode() {
        debug = !debug;
    }

    /**
     * Gets the proxy's current status of debug mode.
     *
     * @return True for debug mode, false otherwise
     */
    public boolean getDebugMode() {
        return debug;
    }

    /**
     * Setup a data connection with the FTP server to transfer the directory listing over.
     * Displays the received listing to standard out.
     *
     * @throws IOException If failed to read from or write to the server
     */
    public void getDirectoryListing() throws IOException {
        BufferedInputStream dataReader = getDataConnectionReader( "LIST" );
        if ( dataReader == null ) return;

        // Read the data stream
        byte[] buffer = new byte[BUFFER];
        while ( dataReader.read( buffer ) != -1 ) {
            FTP.displayMessage( new String( buffer ), false );
            buffer = new byte[BUFFER];
        }

        dataReader.close();
        readStream();
    }

    /**
     * Setup a data connection with the FTP server to transfer a file over.
     * Writes the data to a local file of the same name.
     *
     * @param path Path of the file to retrieve from the server
     * @throws IOException If failed to read from or write to the server
     */
    public void retrieveFile( String path ) throws IOException {
        BufferedInputStream dataReader = getDataConnectionReader( "RETR " + path );
        if ( dataReader == null ) return;

        FileOutputStream outputStream = new FileOutputStream( new File( path.substring( path.lastIndexOf( "/" ) + 1 ) ) );
        byte[] buffer = new byte[BUFFER];

        // Read the data stream
        int readBytes;
        while ( (readBytes = dataReader.read( buffer )) != -1 ) {
            outputStream.write( buffer, 0, readBytes );
        }
        outputStream.flush();

        outputStream.close();
        dataReader.close();
        readStream();
    }

    /**
     * Get a formatted string of the proxy's supported commands along with their descriptions.
     *
     * @return Formatted string of supported commands;
     */
    public String commandListing() {
        return COMMANDS;
    }

    /**
     * If in passive mode, changes the proxy to active mode.
     * If in active mode, changes the proxy to passive mode.
     */
    public void changePassiveMode() {
        passive = !passive;
    }

    /**
     * Gets the proxy's current status of passive mode.
     *
     * @return True for passive mode, false otherwise
     */
    public boolean getPassiveMode() {
        return passive;
    }

    /**
     * Gets the current working directory from the server.
     *
     * @throws IOException If failed to read from or write to the server
     */
    public void printWorkingDirectory() throws IOException {
        writeStream( "PWD" );
        readStream();
    }

    /**
     * Notifies the server of the disconnecting client.
     *
     * @throws IOException If failed to read from or write to the server
     */
    public void quit() throws IOException {
        writeStream( "QUIT" );
        readStream();
    }


    /* Private Functions */


    /**
     * Makes an active or passive connection between the proxy and server.
     * Sends the given command to the server to start sending data over the input stream.
     *
     * @param command Command to send to the server specifying what data to send
     * @return BufferedInputStream of the connection's input stream
     * @throws IOException If failed to read from, write to, or connect to the given address
     */
    private BufferedInputStream getDataConnectionReader( String command ) throws IOException {
        if ( passive ) {  // Passive Mode

            // Ask the server which port to connect to
            writeStream( "PASV" );
            String response = readStream();
            if ( !response.substring( 0, 3 ).equals( "227" ) ) return null;

            // Converts the server's response into a usable format.
            String[] address = response.substring( response.indexOf( '(' ) + 1, response.indexOf( ')' ) ).split( "," );

            int first = Integer.parseInt( address[address.length - 2] );
            int second = Integer.parseInt( address[address.length - 1] );
            int port = (first * 256) + second;

            String ip = address[0];
            for ( int i = 1; i < address.length - 2; ++i )
                ip = ip + "." + address[i];

            // Connect to the server
            Socket data = new Socket();
            try {
                data.connect( new InetSocketAddress( ip, port ), 3000 );
            } catch ( IOException e ) {
                FTP.displayMessage( "Could not connect to the server, try active mode transfers." );
                return null;
            }

            // Tell the server to send the data
            writeStream( command );
            if ( !readStream().substring( 0, 3 ).equals( "150" ) ) return null;

            return new BufferedInputStream( data.getInputStream() );
        }
        else {  // Active Mode

            // Get random port
            ServerSocket data = new ServerSocket( 0 );

            // Convert port to PORT command format
            int port = data.getLocalPort();
            int second = port % 256;
            int first = (port - second) / 256;

            // Tell the server which port to connect to
            writeStream( "PORT " + ipAddress + "," + first + "," + second );
            if ( !readStream().substring( 0, 3 ).equals( "200" ) ) {
                FTP.displayMessage( "Could not connect to the server, try passive mode transfers." );
                return null;
            }

            // Tell the server to send the data
            writeStream( command );
            if ( !readStream().substring( 0, 3 ).equals( "150" ) ) return null;

            return new BufferedInputStream( data.accept().getInputStream() );
        }
    }

    /**
     * Writes a message to the global buffered output stream.
     * Sends that message to the connected FTP server.
     *
     * @param message Message to send to the server
     * @throws IOException If failed to write to the server
     */
    private void writeStream( String message ) throws IOException {
        if ( debug )
            FTP.displayMessage( DEBUG_TEXT + message );

        // Write to output stream
        bufferedWriter.write( message + "\r\n" );
        bufferedWriter.flush();
    }

    /**
     * Read the global buffered input stream until a valid server response code is received.
     * Exits the current running program if a server timeout occurs.
     *
     * @return The server's last response in the input stream
     * @throws IOException If failed to read from input steam
     */
    private String readStream() throws IOException {
        String result;
        while ( true ) {

            // Read buffer
            String line = bufferedReader.readLine();
            FTP.displayMessage( line );
            assertStatus( line.substring( 0, 3 ), 421, false, "Server timeout. Terminating client." );

            try { // If line contains a response code, break
                Integer.parseInt( line.substring( 0, 4 ).trim() );
                result = line;
                break;
            } catch ( NumberFormatException e ) { // Otherwise loop and read again
            }
        }

        return result.trim();
    }

    /**
     * Asserts whether the given FTP server response is equal to the expected response code.
     * Failed assertions close all open global sockets and streams and exit the current running program.
     *
     * @param response     Server response message
     * @param expected     Expected server response code
     * @param status       True to assert that response == expect, false otherwise
     * @param errorMessage Message to display if assertion fails
     */
    private void assertStatus( String response, int expected, boolean status, String errorMessage ) {

        boolean assertionFailed = response.substring( 0, 3 ).equals( String.valueOf( expected ) );

        if ( (status && !assertionFailed) || (!status && assertionFailed) ) {
            FTP.displayMessage( errorMessage );

            try {
                bufferedWriter.close();
                bufferedReader.close();
                socket.close();
            } catch ( IOException e ) {
            }

            System.exit( 1 );
        }
    }

}
