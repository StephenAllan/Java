import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;


/* ====================
FIXME:

    I could not get the server to respond when sending an ACK packet.

    If the file is < 512 bytes, and comes with errors,
    I am able to send back a NACK to the server,
    and have it properly respond by resending that packet again.

    However, I could not figure out why server would not continue
    to send me packets after the first one for files > 512 bytes.

    Since I build the ACK and NACK packets in almost identical manors,
    I was unable to debug the underlying issue.

    If it was an issue with the server I was running locally,
    then please know that I was not able to test this functionality.

    If it is an issue with my code, then this program functions for files of size < 512 bytes only.

    As a result, plaintext files will be partially (first 512 bytes) written to a file.
    Other filetypes, such as images or PDFs, will not be viewable since they are an all-or-nothing kind of deal.

 ==================== */


/**
 * A proxy class for communicating with the given host using UDP datagrams.
 *
 * @author Stephen Allan (swa9846)
 * @version October 31, 2016
 */
public class ServerProxy {


    // Private constants
    private static final int HOST_PORT = 7000;
    private static final int TIMEOUT = 5000;
    private static final int BUFFER_LENGTH = 516;
    private static final int HEADER_LENGTH = 4;
    private static final int BYTE_LENGTH = 8;
    private static final int ERROR_CODE = 5;
    private static final String TRANSFER_MODE = "octet";
    private static final byte[] BYTE_ARRAY_TM = TRANSFER_MODE.getBytes();
    private static final byte[] BYTE_ARRAY_0 = {0};
    private static final byte[] OP_CODE_01 = {0, 1};
    private static final byte[] OP_CODE_02 = {0, 2};
    private static final byte[] OP_CODE_04 = {0, 4};
    private static final byte[] OP_CODE_06 = {0, 6};
    private static final int[] PARITY_BITS = {1, 2, 4, 8, 16, 32};

    // Private global variables
    private InetAddress address;
    private DatagramSocket socket;
    private ByteArrayOutputStream byteStream;
    private ArrayList<Integer> dataWithExtraBits = new ArrayList<>();


    /* Public Functions */


    /**
     * Constructor for the ServerProxy class.
     * Initialize the proxy's global variables.
     *
     * @param host Name of the server to connect to
     * @throws UnknownHostException If failed to locate the server
     * @throws SocketException      If failed to reserve a port for communication or timeout occurs
     */
    public ServerProxy( String host ) throws UnknownHostException, SocketException {
        // Locate host server
        address = InetAddress.getByName( host );
        socket = new DatagramSocket();
        socket.setSoTimeout( TIMEOUT );

        // Setup streams
        byteStream = new ByteArrayOutputStream();
    }

    /**
     * Requests a file from the server.
     * Receives and parses the response from the server.
     * If all parity checks pass, writes the data to a local file.
     * Sends a response packet back to the server after parsing the data.
     *
     * @param errors   True if the received packets may contain errors, false otherwise
     * @param filename Name of the file to retrieve from the server
     * @throws IOException           If failed to reach the host server
     * @throws NullPointerException  If error packet was received
     * @throws FileNotFoundException If cannot access file to write
     */
    public void requestFile( boolean errors, String filename ) throws IOException, NullPointerException, FileNotFoundException {
        // Send request packet to server
        sendPacket( createRequestPacket( errors, filename ) );

        // Open file for writing
        //File file = new File( filename );
        //ileOutputStream fileStream = new FileOutputStream( file );

        DatagramPacket packet;
        boolean wroteMessage = true;
        do {
            // Receive data packet from server
            packet = receivePacket();
            byte[] bytes = packet.getData();

            // Grab first 4 bytes of the packet
            byteStream.write( bytes[0] );
            byteStream.write( bytes[1] );
            byte[] responseCode = streamToArray();

            byteStream.write( bytes[2] );
            byteStream.write( bytes[3] );
            byte[] numberBytes = streamToArray();

            // Check if an error packet was received
            if ( (Byte.toUnsignedInt( responseCode[0] ) + Byte.toUnsignedInt( responseCode[1] )) == ERROR_CODE ) {
                String errorNum = Integer.toString( Byte.toUnsignedInt( numberBytes[0] ) + Byte.toUnsignedInt( numberBytes[1] ) );
                throw new NullPointerException( "Received error number: " + errorNum );
            }

            // Open file for writing
            FileOutputStream fileStream = new FileOutputStream( new File( filename ) );

            // Parse bytes in 32 bit increments
            wroteMessage = verifyReceivedBytes( bytes, fileStream );

            fileStream.flush();
            fileStream.close();

            sendPacket( createAcknowledgementPacket( wroteMessage, numberBytes ) );

        } while ( packet.getLength() >= BUFFER_LENGTH - HEADER_LENGTH || !wroteMessage );

        socket.close();
    }


    /* Private Functions */


    /**
     * Cuts the given byte array into arrays of 4 bytes (32 bits) a piece.
     * Checks for all zero bytes and verify whether or not the incoming bytes contained any errors.
     * Loops through all received bytes, processing them in blocks of 4.
     *
     * @param bytes      Bytes to cut into 32 bit blocks, test for errors, and write to file
     * @param fileStream Stream to write received data to the output file
     * @return False if the packet has a 2 or more bit errors and need transmitting, True otherwise
     * @throws IOException If failed to reach the host server
     */
    private boolean verifyReceivedBytes( byte[] bytes, FileOutputStream fileStream ) throws IOException {
        for ( int i = HEADER_LENGTH; i < BUFFER_LENGTH; ++i ) {

            if ( i % (32 / BYTE_LENGTH) == 0 && i != HEADER_LENGTH ) {

                byte[] parseBytes = streamToArray();
                if ( !isNullBytes( parseBytes ) && !parseReceivedBytes( parseBytes, fileStream ) )
                    return false;
            }

            byteStream.write( bytes[i] );
        }
        return true;
    }

    /**
     * Checks whether or not the given byte array contains only zero bytes.
     *
     * @param bytes Bytes to check for complete nullity.
     * @return False if any of the bytes are non-zero, True otherwise
     */
    private boolean isNullBytes( byte[] bytes ) {
        for ( int i = 0; i < bytes.length; ++i ) {
            if ( bytes[i] != (byte) 0 )
                return false;
        }
        return true;
    }

    /**
     * Checks the hamming parity of the given bytes.
     * Extracts the hamming bits and reverses the input bit into the correct order.
     * Writes the bytes to the local file.
     *
     * @param bytes      Bytes to parse, verify parity on, and write to file
     * @param fileStream Stream to write received data to the output file
     * @return False if the bytes failed the parity check, requiring retransmission, True otherwise
     * @throws IOException If failed to reach the host server
     */
    private boolean parseReceivedBytes( byte[] bytes, FileOutputStream fileStream ) throws IOException {
        ArrayList<Integer> result = getByteBits( bytes );

        if ( !verifyParity( result ) )
            return false;

        // Extract parity bits
        ArrayList<Integer> data = new ArrayList<>();
        int k = 0;
        for ( int i = 0; i < result.size(); ++i ) {
            if ( i == PARITY_BITS[k] - 1 )
                k += 1;
            else {
                data.add( result.get( i ) );
            }
        }
        Collections.reverse( data );

        dataWithExtraBits.addAll( data );

        writeBytesToFile( fileStream );

        return true;
    }

    /**
     * Converts bytes to a array of integers (bits).
     * Combines all bytes given into one integer array.
     *
     * @param bytes Bytes to convert to a consecutive array of bits
     * @return An array of bits representing the given bytes
     */
    private ArrayList<Integer> getByteBits( byte[] bytes ) {
        ArrayList<Integer> result = new ArrayList<>();

        for ( int i = 0; i < bytes.length; ++i ) {
            for ( int j = 0; j < BYTE_LENGTH; ++j ) {
                result.add( ((bytes[i] & (1 << j)) != 0) ? 1 : 0 );
            }
        }

        return result;
    }

    /**
     * Checks the individual parities of the input Integer list.
     * Attempts to correct any incorrect bits within the message.
     * Checks the overall hamming parity.
     * If the overall check fails, the packet needs to be retransmitted, otherwise it can be written to the file.
     *
     * @param bits The bits on which to verify individual and overall parity
     * @return False if the overall parity fails after attempting to correct the supposed incorrect bit, True otherwise
     */
    private boolean verifyParity( ArrayList<Integer> bits ) {
        // Check parity bits
        int incorrectBit = 0;
        for ( int i = 0; i < PARITY_BITS.length; ++i ) {
            boolean parityConfirmation = parityBits( bits, PARITY_BITS[i] );
            if ( !parityConfirmation )
                incorrectBit += PARITY_BITS[i];
        }

        // If a bit error was found, flip the incorrect bit
        if ( incorrectBit > 0 )
            bits.set( incorrectBit - 1, (bits.get( incorrectBit - 1 ) == 0) ? 1 : 0 );

        // Check overall parity
        int sum = 0;
        for ( int i = 0; i < bits.size(); ++i ) {
            sum += bits.get( i );
        }

        // If overall even parity fails, retransmit message
        if ( sum % 2 != 0 )
            return false;

        return true;
    }

    /**
     * Checks that the bits in the array sum to an even parity, based on the given parity bit.
     *
     * @param lst       Array of bits on which to confirm parity
     * @param parityBit Bit number on which to preform the parity check
     * @return True if the given parity for the given list matches the even parity scheme, False otherwise
     */
    private boolean parityBits( ArrayList<Integer> lst, int parityBit ) {
        int sum = 0;
        boolean takeBits = false;

        for ( int i = parityBit; i < lst.size(); ++i ) {
            if ( parityBit == 1 && i % 2 == 1 )
                sum += lst.get( i - 1 );
            else if ( i % parityBit == 0 )
                takeBits = !takeBits;

            if ( takeBits )
                sum += lst.get( i - 1 );
        }

        if (sum % 2 == 0)
            return true;

        return false;
    }

    /**
     * Converts my integer arraylist to list of size 8.
     * Converts those lists of size 8 into bytes.
     * Write those bytes to the given file stream.
     * Carry over extra bits for use in the next 32 bit block.
     *
     * @param fileStream Stream to write received data to the output file
     * @throws IOException If failed to reach the host server
     */
    private void writeBytesToFile( FileOutputStream fileStream ) throws IOException {
        ArrayList<ArrayList<Integer>> bytesToWrite = new ArrayList<>();
        ArrayList<Integer> arr = new ArrayList<>();

        // Split data into arrays of size 8
        for ( int i = 0; i < dataWithExtraBits.size(); ++i ) {
            if ( i % BYTE_LENGTH == 0 && i != 0 ) {
                bytesToWrite.add( arr );
                arr = new ArrayList<>();
            }
            arr.add( dataWithExtraBits.get( i ) );
        }
        bytesToWrite.add( arr );

        // Rebuild byte from Integer array and write to file
        int byteValue;
        for ( int i = 0; i < bytesToWrite.size(); ++i ) {
            ArrayList<Integer> lst = bytesToWrite.get( i );

            if ( lst.size() == BYTE_LENGTH ) {
                int power = 0;
                byteValue = 0;

                for ( int j = 0; j < lst.size(); ++j ) {
                    if ( lst.get( j ) == 1 )
                        byteValue += Math.pow( 2, power );
                    power += 1;
                }

                fileStream.write( byteValue );

                if ( i == 3 )
                    dataWithExtraBits.clear();
            } else {
                // Add extra bits to front of data list
                dataWithExtraBits.clear();
                for ( int j = 0; j < lst.size(); ++j ) {
                    dataWithExtraBits.add( lst.get( j ) );
                }
            }
        }
    }

    /**
     * Send data in given array to the server.
     *
     * @param bytes Data to send to the server
     * @throws IOException If failed to reach the host server
     */
    private void sendPacket( byte[] bytes ) throws IOException {
        socket.send( new DatagramPacket( bytes, bytes.length, address, HOST_PORT ) );
    }

    /**
     * Wait for a server response packet.
     * Blocks until a message is received from the server.
     *
     * @return Data received from the server
     * @throws IOException            If failed to reach the host server
     * @throws SocketTimeoutException If timeout was reached while waiting for server response
     */
    private DatagramPacket receivePacket() throws IOException, SocketTimeoutException {
        DatagramPacket packet = new DatagramPacket( new byte[BUFFER_LENGTH], BUFFER_LENGTH );
        socket.receive( packet );
        return packet;
    }

    /**
     * Builds the byte array to send to the server for requesting a packet.
     *
     * @param errors   True if the received packets may contain errors, false otherwise
     * @param filename Name of the file to retrieve from the server
     * @return The created byte array
     * @throws IOException If failed to reach the host server
     */
    private byte[] createRequestPacket( boolean errors, String filename ) throws IOException {
        byteStream.write( (errors) ? OP_CODE_02 : OP_CODE_01 );
        byteStream.write( filename.getBytes() );
        byteStream.write( BYTE_ARRAY_0 );
        byteStream.write( BYTE_ARRAY_TM );
        byteStream.write( BYTE_ARRAY_0 );

        return streamToArray();
    }

    /**
     * Builds the byte array to send to the server for acknowledging a received packet.
     *
     * @param ack         False is the packet has a 2 or more bit errors and need transmitting, True otherwise
     * @param blockNumber Number of the block the received packet was in
     * @return The created byte array
     * @throws IOException If failed to reach the host server
     */
    private byte[] createAcknowledgementPacket( boolean ack, byte[] blockNumber ) throws IOException {
        byteStream.write( (ack) ? OP_CODE_04 : OP_CODE_06 );
        //byteStream.write( BYTE_ARRAY_0 );
        byteStream.write( blockNumber );
        //byteStream.write( BYTE_ARRAY_0 );

        return streamToArray();
    }

    /**
     * Neatly converts the byte stream to a byte array, resetting the stream before the next use.
     *
     * @return Array of the bytes written to the byte stream
     * @throws IOException If failed to reach the host server
     */
    private byte[] streamToArray() throws IOException {
        byteStream.flush();
        byte[] bytes = byteStream.toByteArray();
        byteStream.reset();

        return bytes;
    }

}