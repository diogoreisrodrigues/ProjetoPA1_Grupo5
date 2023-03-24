import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class extends Thread and implements a word filtering system for the chat.
 */
public class Filter extends Thread{

    private String message;
    private static final List<String> bannedWords= new ArrayList<>();

    Queue<Message> buffer;
    ;

    Queue<Message> filteredBuffer;

    ReentrantLock bufferLock;

    ReentrantLock filteredBufferLock;

    /**
     * This is the constructor of Filter class.
     *
     * @param buffer is the buffer where the incoming messages are stored.
     * @param filteredBuffer is the buffer where the filtered messages are stored.
     * @param bufferLock is the lock used for the buffer.
     * @param filteredBufferLock is the lock used for the filtered buffer.
     *
     * @throws IOException if there is an error reading the banned words file.
     */

    public Filter(Queue<Message> buffer, Queue<Message> filteredBuffer , ReentrantLock bufferLock, ReentrantLock filteredBufferLock) throws IOException {
        this.buffer=buffer;
        this.filteredBuffer = filteredBuffer;
        this.bufferLock = bufferLock;
        this.filteredBufferLock = filteredBufferLock;
        //this.filterLock = filterLock;
    }

    /**
     * Getter for the message being filtered.
     *
     * @return the message being filtered.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Verifies if the given message contains any banned words.
     *
     * @param message is the message to be verified.
     *
     * @return true if the message contains banned words, return false otherwise.
     *
     * @throws IOException if there is an error reading the banned words file.
     */
    private boolean FilterVerify(String message) throws IOException {

        String[] wordSplitter= message.split(" ");

        for(String word:wordSplitter){
            if(bannedWords.contains(word.toLowerCase())){
                return true;
            }
        }
        return false;
    }

    /**
     * Reads the file with the banned words and stores them in the bannedWords list.
     *
     * @param fileName is the name of the file that contains the banned words.
     *
     * @throws IOException if there is an error reading the file.
     */
    public static void bannedWordsFile(String fileName) throws IOException{

        //List of banned words
        Path fileBannedWords= Paths.get(fileName);

        BufferedReader bannedWordsFile=new BufferedReader(new FileReader(fileBannedWords.toFile()));

        String lineFile;

        while((lineFile=bannedWordsFile.readLine())!=null){

            String[] lineWordsFile = lineFile.split(" ");

            Collections.addAll(bannedWords, lineWordsFile);

        }
        bannedWordsFile.close();
    }



    /**
     * This method runs the thread that filters the messages from the clients and places them in a filtered buffer.
     * Firstly reads a list of banned words from a file, and then it continuously polls the input buffer for messages.
     * If a message contains a banned word, it is removed from the buffer and a modified message is placed in the filtered buffer.
     * The modified message indicates that the original message was removed for being inappropriate.
     *
     * @throws RuntimeException if an IOException or InterruptedException occurs while reading the list of banned words or filtering the messages.
     */
    @Override
    public void run() {

        try {
            bannedWordsFile("bannedWords.txt");

            while (true) {
                bufferLock.lock();
                Message bufferMessage = buffer.poll();
                bufferLock.unlock();
                if (bufferMessage == null) {
                    sleep(1000);
                    continue;
                }
                if (FilterVerify(bufferMessage.getMessage())) {

                    System.out.println("A message was removed for being inappropriate: " + bufferMessage.getMessage());
                    bufferMessage.setMessage("A message was removed for being inappropriate");
                    filteredBufferLock.lock();
                    filteredBuffer.offer(bufferMessage);
                    filteredBufferLock.unlock();
                } else {
                    filteredBufferLock.lock();
                    filteredBuffer.offer(bufferMessage);
                    filteredBufferLock.unlock();
                }

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}
