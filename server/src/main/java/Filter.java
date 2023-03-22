import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Semaphore;

public class Filter extends Thread{

    private String message;
    private static final List<String> bannedWords= new ArrayList<>();

    Queue<Message> buffer;
    ;

    Queue<Message> filteredBuffer;




    public Filter(Queue<Message> buffer,Queue<Message> filteredBuffer) throws IOException {
        this.buffer=buffer;
        this.filteredBuffer = filteredBuffer;
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
                Message bufferMessage = buffer.poll();
                if (bufferMessage == null) {
                    continue;
                }
                if (FilterVerify(bufferMessage.getMessage())) {
                    System.out.println("A message was removed for being inappropriate: " + bufferMessage.getMessage());
                    bufferMessage.setMessage("A message was removed for being inappropriate");
                } else {
                    filteredBuffer.offer(bufferMessage);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }



        /*try {
            bannedWordsFile("bannedWords.txt");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        while (true) {
            try {

                if (buffer.size() > 0) {
                    for (Message bufferMessage : buffer) {
                        System.out.println("entrei no ciclo");
                        if (FilterVerify(bufferMessage.getMessage())) {

                            message = "A message was removed for being inappropriate";
                        } else {
                            message = bufferMessage;
                        }
                        filterLock.release();
                    }
                    buffer.remove(buffer.size() - 1);
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

         */
    }
}
