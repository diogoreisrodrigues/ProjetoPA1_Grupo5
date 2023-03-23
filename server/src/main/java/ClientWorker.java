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

    private final int id;

    //private final String username;
    private Socket socket;
    private Semaphore semaphore;
    private  Queue<Socket> waitingClients;
    private  ExecutorService executor;
    private AtomicInteger counterId;
    private final ReentrantLock lockLogger;
    private final Queue<String> queueToLog;


    public ClientWorker (Socket request, Logger logger, int id, Semaphore semaphore, ReentrantLock lockLog, Queue<String> messageQueue ) {


        try {
            this.request = request;
            this.in = new DataInputStream( request.getInputStream ( ) );
            this.out = new PrintWriter( request.getOutputStream ( ) , true );
            this.logger = logger;
            this.semaphore = semaphore;
            this.waitingClients = waitingClients;
            this.counterId = counterId;
            this.executor = executor;
            //this.username = in.readUTF ( );
            this.id = id;
            ClientWorkers.add(this);
            this.lockLogger = lockLog;
            this.queueToLog=messageQueue;
            sendMessage("The Client "+ id +" has connected to the chat");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
   public void run() {

        queueToLog.add( "CONNECTED Client "+id);

        while ( request.isConnected() ) {
            try {
                String message = in.readUTF ( );
                queueToLog.add("Message - Client "+id +" - "+message);
                sendMessage(/*username +" : "+*/message);


                //Filter f= new Filter(message);
                //f.start();
                //f.join();
                //String filteredMessage =f.getMessage();
                //System.out.println ( "***** " + message + " *****" );
                //out.println(filteredMessage);
                //log("Message - Client "+id +" -  "+message);
                //out.println ( "Message received" );

            } catch ( IOException e/*| InterruptedException e */) {
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
