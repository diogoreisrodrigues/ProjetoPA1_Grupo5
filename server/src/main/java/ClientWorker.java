import java.io.*;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.*;

import static java.lang.Thread.sleep;


public class ClientWorker implements Runnable{


    private final Socket request;

    //private final ReentrantLock lockQueueReplies;
    private final DataInputStream in;
    private final PrintWriter out;

    public static ArrayList<ClientWorker> ClientWorkers = new ArrayList<>();

    private final Logger logger;

    private byte[] result;

    private static final List<String> bannedWords= new ArrayList<>();

    private AtomicInteger nClients;

    private Queue <Client> queueReplies;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final int id;

    //private final String username;
    private Socket socket;
    private Semaphore semaphore;
    private  Queue<Socket> waitingClients;
    private  ExecutorService executor;
    private AtomicInteger counterId;
    private final ReentrantLock lockLogger;

    private final Semaphore filterLock;

    Queue<Message> buffer;

    Queue<Message> filteredBuffer;

    ReentrantLock bufferLock;

    ReentrantLock filteredBufferLock;


    public ClientWorker (Socket request, Logger logger, int id, Semaphore semaphore, ReentrantLock lockLog, Queue<Message> buffer,Queue<Message> filteredBuffer, ReentrantLock bufferLock, ReentrantLock filteredBufferLock) {

        try {
            this.request = request;
            this.in = new DataInputStream( request.getInputStream ( ) );
            this.out = new PrintWriter( request.getOutputStream ( ) , true );
            this.logger = logger;
            this.semaphore = semaphore;

            this.id = id;
            ClientWorkers.add(this);
            sendMessage("The Client "+ id +" has connected to the chat");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
            this.lockLogger = lockLog;
            this.filterLock = new Semaphore(0);
            this.buffer = buffer;
            this.filteredBuffer = filteredBuffer;
            this.bufferLock = bufferLock;
            this.filteredBufferLock = filteredBufferLock;
    }

    @Override
   public void run() {

        log( "CONNECTED Client "+id);


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

                        sendMessage(filteredMessage.getMessage());
                        log("Message - Client "+id +" - "+simpleMessage);
                        filteredBufferLock.lock();
                        filteredBuffer.remove();
                        filteredBufferLock.unlock();
                        break;
                    }
                }
            } catch ( IOException e ) {
                disconnectClient();
                break;
            }


        }

    }

    private void sendMessage(String message) {

        for(ClientWorker clientWorker : ClientWorkers){
            if(clientWorker.id != id){
                clientWorker.out.write(message);
                clientWorker.out.println();
                clientWorker.out.flush();
            }
        }
    }

    public void disconnectClient(){

        ClientWorkers.remove(this);
        sendMessage("Client "+  id + " has left the chat");
        log("DISCONNECTED Client "+id);
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

    public void log ( String message){
        lockLogger.lock();
        LocalDateTime timeOfAction = LocalDateTime.now();
        logger.info(timeOfAction.format(formatter)+"- Action : "+ message);
         lockLogger.unlock();

    }



}
