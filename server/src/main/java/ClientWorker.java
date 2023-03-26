import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;


/**
 * This class represents a Client Worker thread in a chat server.
 * Each Client Worker thread handles the communication for a client.
 */
public class ClientWorker implements Runnable{

    private final Socket request;
    private final DataInputStream in;
    private final PrintWriter out;
    public static ArrayList<ClientWorker> ClientWorkers = new ArrayList<>();
    private final int id;
    private Semaphore semaphore;
    private final Queue<String> queueToLog;
    private final Semaphore filterLock;
    private Queue<Message> buffer;
    private Queue<Message> filteredBuffer;
    private ReentrantLock bufferLock;
    private ReentrantLock filteredBufferLock;

    /**
     * This is the constructor of ClientWorker class.
     * @param request is the socket connection to the client.
     * @param id is the id of the Client Worker thread.
     * @param buffer is the buffer responsible for storing incoming chat messages.
     * @param filteredBuffer is the buffer responsible for storing filtered chat messages.
     * @param bufferLock is the reentrant lock for accessing the buffer.
     * @param filteredBufferLock is the reentrant lock for accessing the filtered buffer.
     * @param messageQueue is the queue of chat messages to log.
     */
    public ClientWorker (Socket request, int id, Queue<Message> buffer,Queue<Message> filteredBuffer, ReentrantLock bufferLock, ReentrantLock filteredBufferLock, Queue<String> messageQueue, Semaphore semaphore) {

        try {
            this.request = request;
            this.in = new DataInputStream( request.getInputStream ( ) );
            this.out = new PrintWriter( request.getOutputStream ( ) , true );
            this.id = id;
            ClientWorkers.add(this);
            this.queueToLog=messageQueue;
            sendMessage("The Client "+ id +" has connected to the chat", ClientWorkers);
            this.filterLock = new Semaphore(0);
            this.buffer = buffer;
            this.filteredBuffer = filteredBuffer;
            this.bufferLock = bufferLock;
            this.filteredBufferLock = filteredBufferLock;
            this.semaphore = semaphore;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This method represents the main logic of the ClientWorker, which runs in a separate thread when a client connects to the chat.
     * It adds a log entry showing that the client is connected and reading incoming messages from the client socket.
     * It then adds messages to the shared message buffer and tries to filter messages specific to that client.
     * If it finds a message, it sends the message to all other clients in the chat, logs the message, and removes it from the filter buffer.
     * If reading the message throws an IOException, it calls the disconnectClient method to disconnect the client from the chat.
     *
     * @throws RuntimeException if an error occurs while disconnecting the client.
     */
    @Override
    public void run() {

        queueToLog.add( "CONNECTED Client "+id);

        while ( request.isConnected() ) {
            try {
                String simpleMessage = in.readUTF ( );

                Message message = new Message(id, simpleMessage);
                bufferLock.lock();
                buffer.add(message);
                bufferLock.unlock();

                while(true) {
                    filteredBufferLock.lock();
                    Message filteredMessage = filteredBuffer.peek();
                    filteredBufferLock.unlock();
                    if (filteredMessage != null && filteredMessage.getClientWorkerId() == id) {

                        sendMessage(filteredMessage.getMessage(), ClientWorkers);
                        queueToLog.add("Message - Client "+id +" - "+simpleMessage);
                        filteredBufferLock.lock();
                        filteredBuffer.remove();
                        filteredBufferLock.unlock();
                        break;
                    }
                }
            } catch ( IOException e ) {
                disconnectClient(ClientWorkers,id, queueToLog,request, out, in);
                break;
            }
        }
    }

    /**
     * This method send a message to all connected Clients but not for the Client who sends it.
     *
     * @param message is the message that will be sent.
     */
    public void sendMessage(String message, ArrayList<ClientWorker> ClientWorkers) {

        for(ClientWorker clientWorker : ClientWorkers){
            if(clientWorker.id != id){
                clientWorker.out.write(message);
                clientWorker.out.println();
                clientWorker.out.flush();
            }
        }
    }
    /**
     * This method removes the Client from the server and notifies the other Clients that this determinate Client left the chat.
     * After this, logs the event, then, close's de Client socket and release the semaphore used for controlling the number of active Clients.
     *
     * @throws RuntimeException if an I/O error occurs while trying to close the socket, input stream, or output stream.
     */
    public void disconnectClient(ArrayList<ClientWorker> ClientWorkers, int id, Queue<String> messageQueue, Socket socket, PrintWriter out, DataInputStream in){

        ClientWorkers.remove(this);
        sendMessage("Client "+  id + " has left the chat", ClientWorkers);
        queueToLog.add("DISCONNECTED Client "+id);
        try{
            if(socket != null){
                socket.close();
            }

            if(out != null){
                out.close();
            }
            if(in != null){
                in.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            semaphore.release();

        }

    }
}