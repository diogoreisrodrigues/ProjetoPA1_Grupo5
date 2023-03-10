import java.io.*;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class ClientWorker implements Runnable{


    private final Socket request;
    //private final FileServer fileServer;
    //private final ReentrantLock lockQueueReplies;
    private final DataInputStream in;
    private final PrintWriter out;

    private byte[] result;

    private static final List<String> bannedWords= new ArrayList<>();

    private AtomicInteger nClients;

    private Queue <Client> queueReplies;

    public ClientWorker (Socket request) {

        try {
            this.request = request;
            this.in = new DataInputStream( request.getInputStream ( ) );
            this.out = new PrintWriter( request.getOutputStream ( ) , true );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void run() {
        while ( true ) {
            try {

                String message = in.readUTF ( );
                System.out.println ( "***** " + message + " *****" );
                bannedWordsFile("bannedWords.txt");
                if(Filter(message)){
                    out.println("One of your following messages was removed for being inappropriate");
                }
                else{
                    out.println ( message.toUpperCase ( ) );
                }

            } catch ( IOException e ) {
                throw new RuntimeException();
            }
        }
    }

    private boolean Filter(String message) throws IOException {

        String[] wordSplitter= message.split(" ");

        for(String word:wordSplitter){
            if(bannedWords.contains(word.toLowerCase())){
                return true;
            }
        }
        return false;
    }

    public static List<String> bannedWordsFile(String fileName) throws IOException{
        //List of banned words
        Path filePath= Paths.get(fileName);

        BufferedReader bannedWordsFile=new BufferedReader(new FileReader(filePath.toFile()));

        String lineFile;

        while((lineFile=bannedWordsFile.readLine())!=null){

            String[] lineWordsFile = lineFile.split(" ");

            Collections.addAll(bannedWords, lineWordsFile);

        }
        bannedWordsFile.close();
        return bannedWords;
    }
}
