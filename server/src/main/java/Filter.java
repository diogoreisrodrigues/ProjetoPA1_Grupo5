import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

public class Filter extends Thread{

    private String message;
    private static final List<String> bannedWords= new ArrayList<>();

    Queue<Message> buffer;
    ;

    Queue<Message> filteredBuffer;

    ReentrantLock bufferLock;

    ReentrantLock filteredBufferLock;

    public Filter(Queue<Message> buffer, Queue<Message> filteredBuffer , ReentrantLock bufferLock, ReentrantLock filteredBufferLock) throws IOException {
        this.buffer=buffer;
        this.filteredBuffer = filteredBuffer;
        this.bufferLock = bufferLock;
        this.filteredBufferLock = filteredBufferLock;
        //this.filterLock = filterLock;
    }

    public String getMessage() {
        return message;
    }

    private boolean FilterVerify(String message) throws IOException {

        String[] wordSplitter= message.split(" ");

        for(String word:wordSplitter){
            if(bannedWords.contains(word.toLowerCase())){
                return true;
            }
        }
        return false;
    }

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
