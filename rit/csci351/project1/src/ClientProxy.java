import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * FTP proxy class for interacting with the specified FTP client.
 *
 * @author Stephen Allan (swa9846)
 * @version September 26, 2016
 */
public class ClientProxy {


    // Private constants
    private static final String WELCOME_MESSAGE = "220 This server was created for CSCI351 at RIT.";
    private static final String SPECIFY_PASS = "331 Please specify the password.";
    private static final String LOGIN_SUCCESS = "230 Login successful.";
    private static final String LOGIN_FAIL = "530 Please login with USER and PASS.";
    private static final String ANONYMOUS_SERVER = "530 This FTP server is anonymous only.";
    private static final String BINARY_MODE = "200 Switching to Binary mode.";
    private static final String ASCII_MODE = "200 Switching to ASCII mode.";
    private static final String UNKNOWN_TYPE = "500 Unrecognised TYPE command.";
    private static final String DIRECTORY_FAIL = "550 Failed to change directory.";
    private static final String DIRECTORY_SUCCESS = "250 Directory successfully changed.";
    private static final String PRINT_WORKING_DIRECTORY = "257";
    private static final String PASSIVE_MODE = "227 Entering Passive Mode";
    private static final String PORT_FAIL = "500 Illegal PORT command.";
    private static final String CONNECTION_FAIL = "425 Cannot open data connection.";
    private static final String PORT_SUCCESS = "200 PORT command successful. Consider using PASV.";
    private static final String NO_DATA_CONNECTION = "425 Use PORT or PASV first.";
    private static final String DIRECTORY_START = "150 Here comes the directory listing.";
    private static final String DIRECTORY_SEND_PASS = "226 Directory send OK.";
    private static final String DIRECTORY_SEND_FAIL = "226 Directory send failed.";
    private static final String FILE_OPEN_FAIL = "550 Failed to open file.";
    private static final String OPEN_DATA_CONNECTION = "150 Opening BINARY mode data connection for";
    private static final String TRANSFER_PASS = "226 Transfer complete.";
    private static final String TRANSFER_FAIL = "226 Transfer failed.";
    private static final String QUIT = "221 Goodbye.";
    private static final String COMMAND_NOT_SUPPORTED = "200 Command not supported";
    private static final String TIMEOUT = "421 Timeout.";
    private static final String WORKING_DIRECTORY = "user.dir";
    private static final int BUFFER = 9216;

    // Private global variables
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String ipAddress;
    private Closeable dataConnection;
    private boolean binaryTransfer = true;


    /* Public Functions */


    /**
     * Constructor for the ClientProxy class.
     * Initialize the proxy's global variables.
     *
     * @param client Command socket connected to the client
     * @throws IOException If failed to access client's input or output stream
     */
    public ClientProxy( Socket client ) throws IOException {
        // Setup streams
        bufferedReader = new BufferedReader( new InputStreamReader( client.getInputStream() ) );
        bufferedWriter = new BufferedWriter( new OutputStreamWriter( client.getOutputStream() ) );

        ipAddress = client.getLocalAddress().toString().substring( 1 ).replace( '.', ',' );
    }

    /**
     * Send the server's welcome message to the connected client.
     *
     * @throws IOException If failed to write to the client
     */
    public void welcome() throws IOException {
        sendResponse( WELCOME_MESSAGE );
    }

    /**
     * Attempts to log the connected client into the FTP server.
     * Prompts the client for a username and password.
     * Accepts any anonymous sessions.
     *
     * @return true if the client was successfully logged in, false otherwise
     * @throws IOException If failed to read from or write to the client
     */
    public boolean login() throws IOException {
        String[] cmd = readCommand().split( " " );
        if ( cmd[0].equalsIgnoreCase( "USER" ) ) {
            if ( cmd.length > 1 && (cmd[1].equalsIgnoreCase( "anonymous" ) || cmd[1].equalsIgnoreCase( "ftp" )) ) {
                sendResponse( SPECIFY_PASS );
                cmd = readCommand().split( " " );
                if ( cmd[0].equalsIgnoreCase( "PASS" ) ) {
                    sendResponse( LOGIN_SUCCESS );
                    return true;
                }
                else
                    sendResponse( LOGIN_FAIL );
            }
            else
                sendResponse( ANONYMOUS_SERVER );
        }
        else
            sendResponse( LOGIN_FAIL );

        return false;
    }

    /**
     * Sets the server's transfer mode.
     *
     * @param type Transfer type
     * @throws IOException If failed to write to the client
     */
    public void transferType( String type ) throws IOException {
        if ( type.equalsIgnoreCase( "I" ) ) {
            binaryTransfer = true;
            sendResponse( BINARY_MODE );
        }
        else if ( type.equalsIgnoreCase( "A" ) ) {
            binaryTransfer = false;
            sendResponse( ASCII_MODE );
        }
        else
            sendResponse( UNKNOWN_TYPE );
    }

    /**
     * Changes the current working directory to the parent directory.
     *
     * @throws IOException If failed to write to the client
     */
    public void directoryUp() throws IOException {
        String workingDirectory = System.getProperty( WORKING_DIRECTORY );
        String parent = new File( workingDirectory ).getParent();

        if ( parent == null )
            changeDirectory( workingDirectory );
        else
            changeDirectory( parent );
    }

    /**
     * Changes the current working directory to the given path.
     *
     * @param path Path of the directory to set as the working directory
     * @throws IOException If failed to write to the client
     */
    public void changeDirectory( String path ) throws IOException {
        if ( path.equals( "" ) ) {
            sendResponse( DIRECTORY_FAIL );
            return;
        }

        File file;
        if ( path.startsWith( "/" ) )
            file = new File( path );
        else
            file = new File( System.getProperty( WORKING_DIRECTORY ), path );

        if ( file.exists() && file.isDirectory() && file.canRead() ) {
            System.setProperty( WORKING_DIRECTORY, file.getCanonicalPath() );
            sendResponse( DIRECTORY_SUCCESS );
        }
        else
            sendResponse( DIRECTORY_FAIL );
    }

    /**
     * Sends the current working directory to the client.
     *
     * @throws IOException If failed to write to the client
     */
    public void printWorkingDirectory() throws IOException {
        sendResponse( PRINT_WORKING_DIRECTORY + " \"" + System.getProperty( WORKING_DIRECTORY ) + "\"" );
    }

    /**
     * Setup a data connection with the FTP client to transfer information.
     * Uses the address and port given by the client for data transfer.
     *
     * @throws IOException If failed to write to the client
     */
    public void passiveDataConnection() throws IOException {
        // Get random port
        ServerSocket data = new ServerSocket( 0 );

        // Convert port to PASV response format
        int port = data.getLocalPort();
        int second = port % 256;
        int first = (port - second) / 256;

        // Tell the client which port to connect to
        sendResponse( PASSIVE_MODE + " (" + ipAddress + "," + first + "," + second + ")." );
        dataConnection = data;
    }

    /**
     * Setup a data connection with the FTP client to transfer information.
     * Provides the client with an IP and port to connect on for data transfer.
     *
     * @param address Data connection address to connect on
     * @throws IOException If failed to write to the client
     */
    public void activeDataConnection( String address ) throws IOException {
        if ( address.equals( "" ) ) {
            sendResponse( PORT_FAIL );
            return;
        }

        // Converts the client's command into a usable format.
        String[] connectionInfo = address.split( "," );

        int first = Integer.parseInt( connectionInfo[connectionInfo.length - 2] );
        int second = Integer.parseInt( connectionInfo[connectionInfo.length - 1] );
        int port = (first * 256) + second;

        String ip = connectionInfo[0];
        for ( int i = 1; i < connectionInfo.length - 2; ++i )
            ip = ip + "." + connectionInfo[i];

        // Connect to the client
        Socket data = new Socket();
        try {
            data.connect( new InetSocketAddress( ip, port ), 3000 );
        } catch ( IOException e ) {
            sendResponse( CONNECTION_FAIL );
            return;
        }

        sendResponse( PORT_SUCCESS );
        dataConnection = data;
    }

    /**
     * Send directory listing as well as success codes to the client.
     *
     * @throws IOException If failed to write to the client
     */
    public void directoryListing() throws IOException {
        if ( dataConnection == null ) {
            sendResponse( NO_DATA_CONNECTION );
            return;
        }

        sendResponse( DIRECTORY_START );

        BufferedOutputStream outputStream;
        if ( dataConnection.getClass().getSimpleName().equalsIgnoreCase( "ServerSocket" ) )
            outputStream = new BufferedOutputStream( ((ServerSocket) dataConnection).accept().getOutputStream() );
        else
            outputStream = new BufferedOutputStream( ((Socket) dataConnection).getOutputStream() );

        try {
            File file = new File( System.getProperty( WORKING_DIRECTORY ) );

            if ( file.exists() && file.isDirectory() && file.canRead() ) {
                String filename;
                File[] files = file.listFiles();
                for ( int i = 0; i < files.length; ++i ) {
                    filename = files[i].getName();
                    if ( files[i].isDirectory() )
                        filename += System.getProperty( "file.separator" );

                    outputStream.write( (filename + "\r\n").getBytes() );
                }
            }
            else
                throw new FileNotFoundException();

            outputStream.flush();
            sendResponse( DIRECTORY_SEND_PASS );
        } catch ( Exception e ) {
            sendResponse( DIRECTORY_SEND_FAIL );
        }

        outputStream.close();
        dataConnection.close();
        dataConnection = null;
    }

    /**
     * Reads the requested file into an input stream.
     * Send the file's bytes as well as success codes to the client.
     *
     * @param filename Path of the file to send to the client
     * @throws IOException If failed to write to the client
     */
    public void fileTransfer( String filename ) throws IOException {
        if ( dataConnection == null ) {
            sendResponse( NO_DATA_CONNECTION );
            return;
        }
        else if ( filename.equals( "" ) ) {
            sendResponse( FILE_OPEN_FAIL );
            dataConnection = null;
            return;
        }

        File file;
        if ( filename.startsWith( "/" ) )
            file = new File( filename );
        else
            file = new File( System.getProperty( WORKING_DIRECTORY ), filename );

        if ( file.exists() && file.isFile() && file.canRead() )
            sendResponse( OPEN_DATA_CONNECTION + " " + filename + " (" + file.length() + " bytes)." );
        else {
            sendResponse( FILE_OPEN_FAIL );
            dataConnection = null;
            return;
        }

        BufferedOutputStream outputStream;
        if ( dataConnection.getClass().getSimpleName().equalsIgnoreCase( "ServerSocket" ) )
            outputStream = new BufferedOutputStream( ((ServerSocket) dataConnection).accept().getOutputStream() );
        else
            outputStream = new BufferedOutputStream( ((Socket) dataConnection).getOutputStream() );

        try {
            FileInputStream fileStream = new FileInputStream( file );
            byte[] buffer = new byte[BUFFER];

            int readBytes;
            while ( (readBytes = fileStream.read( buffer )) != -1 ) {
                outputStream.write( buffer, 0, readBytes );
            }

            outputStream.flush();
            sendResponse( TRANSFER_PASS );
        } catch ( Exception e ) {
            sendResponse( TRANSFER_FAIL );
        }

        outputStream.close();
        dataConnection.close();
        dataConnection = null;
    }

    /**
     * Sends a disconnection message to the client.
     *
     * @throws IOException If failed to write to the client
     */
    public void quit() throws IOException {
        sendResponse( QUIT );
    }

    /**
     * Notifies the client of an received command which is not supported.
     *
     * @throws IOException If failed to write to the client
     */
    public void unknownCommand() throws IOException {
        sendResponse( COMMAND_NOT_SUPPORTED );
    }

    /**
     * Sends a server timeout message to the client.
     *
     * @throws IOException If failed to write to the client
     */
    public void timeout() throws IOException {
        sendResponse( TIMEOUT );
    }

    /**
     * Read the global buffered input stream.
     *
     * @return Next line in the input stream
     * @throws IOException If failed to read from the client
     */
    public String readCommand() throws IOException {
        return bufferedReader.readLine();
    }


    /* Private Functions */


    /**
     * Writes a message to the global buffered output stream.
     * Sends that message to the connected FTP client.
     *
     * @param response Message to send to the client
     * @throws IOException If failed to write to the client
     */
    private void sendResponse( String response ) throws IOException {
        bufferedWriter.write( response + "\r\n" );
        bufferedWriter.flush();
    }

}
