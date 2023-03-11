import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.lang.System.out;

public class Filter extends Thread{

    private String message;
    private static final List<String> bannedWords= new ArrayList<>();

    public Filter(String message) throws IOException {
        this.message=message;
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
            if(FilterVerify(message)){
                message="One of your messages was removed for being inappropriate";
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
