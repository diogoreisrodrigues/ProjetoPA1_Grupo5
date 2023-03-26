import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;

/**
 *This class extends Thread, is responsible for the interaction of the user with the user
 */
public class ServerMenu extends Thread {

    private final Logger logger;
    private int maxClients;
    private MySemaphore semaphore;

    /**
     * This is the constructor of the Server Menu
     * @param logger is the logger object used for logging messages.
     * @param maxClients is the number of maximum clients allowed by the server
     * @param semaphore is the semaphore used to control server entrances and exits
     */
    public ServerMenu(Logger logger, int maxClients, MySemaphore semaphore) {
        this.logger = logger;
        this.maxClients = maxClients;
        this.semaphore = semaphore;
    }
    /**
     * this function is responsible for showing the user interface and calling the other functions when a certain input is given
     * @throws InterruptedException
     */
    public void serverMenu () throws InterruptedException {
        Thread m = new Thread(() -> {
            while(true) {
                Scanner scanner = new Scanner(System.in);
                System.out.println();
                System.out.println("|            Menu do Servidor              |");
                System.out.println("|                                          |");
                System.out.println("| 1. Verificar o log do servidor           |");
                System.out.println("| 2. Adicionar palavras ao filtro          |");
                System.out.println("| 3. Remover palavras ao filtro            |");
                System.out.println("| 4. Atualizar número máximo de clientes   |");
                int choice = scanner.nextInt();
                switch (choice) {
                    case 1 -> showLog();
                    case 2 -> addWordsToMenu("bannedWords.txt");
                    case 3 -> removeWordsFromFile("bannedWords.txt");
                    case 4 -> changeMaxClients();
                    default -> System.out.println("Opção incorreta");
                }
            }
        });
        m.start();
    }

    /**
     *This method calls the Server Menu method and is being used so that the menu is being run in parallel with the server
     */
    public void run(){
        try {
            serverMenu();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    /**
     *This function is responsible for showing the log in console and removing it if the user wants it
     */
    public void showLog(){

        System.out.println("Para sair do logger introduza '0'");
        Scanner scanner1 = new Scanner(System.in);
        int choice1 = 1;
        ConsoleHandler ch = new ConsoleHandler();
        ch.setFormatter(new MyFormatter());
        logger.addHandler(ch);
        while (choice1 != 0) {
            choice1 = scanner1.nextInt();
        }
        System.out.println("Saiu do logger");
        logger.removeHandler(ch);

    }

    /**
     * This function is responsible for asking wthe word that the user wants to add the file checking if it already exists,
     * if the word is already in the file the user is notified
     * @param fileName the name of the file that has the banned words
     */
    public void addWordsToMenu(String fileName){

        System.out.println("Adicione a palavra:");
        Scanner scanner2= new Scanner(System.in);
        String word= scanner2.nextLine();
        while(Objects.equals(word, "")){
            System.out.println("Não é possível adicionar um espaço em branco. Introduza uma palavra válida: ");
            word=scanner2.nextLine();
        }
        word=word.toLowerCase();
        if(wordInFile(fileName,word)){
            System.out.println("A palavra que pretende adicionar já existe no ficheiro de palavras proibidas.");
        }
        else {
            addWordToFile(fileName, word);
            System.out.println("A palavra "+word+" foi adicionada com sucesso.");
        }

    }

    /**
     * This function is responsible for adding the word that the user wants to the file
     * @param filePath path of the file that has the banned words
     * @param word word to be added
     */
    public void addWordToFile(String filePath, String word) {
        try {

            StringBuilder fileContent = new StringBuilder();
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String line = reader.readLine();
            boolean fileIsEmpty=true;

            while (line != null) {
                fileContent.append(line);
                fileContent.append(System.lineSeparator());
                line = reader.readLine();
                fileIsEmpty = false;
            }
            reader.close();
            if (!fileIsEmpty) {
                int lastLineIndex = fileContent.lastIndexOf(System.lineSeparator());
                if (lastLineIndex != -1) {
                    fileContent.insert(lastLineIndex + 1, word);
                }
            } else {
                fileContent.append(word);
            }
            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
            writer.write(fileContent.toString());
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * this functions checks if a word is in a file
     * @param fileName the name of the file that has the banned words
     * @param word word to be added
     * @return true if the word is in the file, false if it isn´t
     */
    public boolean wordInFile(String fileName, String word) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains(word)) {
                    return true;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    /**
     * this function is responsible for showing the user what words can be removed, and removing the one he chooses
     * @param fileName the name of the file that has the banned words
     */
    public void removeWordsFromFile(String fileName) {

        List<String> bannedWords = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {

            String line;
            System.out.println("Que palavras deseja remover?");
            System.out.println();

            while ((line = br.readLine()) != null) {
                String[] words = line.split("\\s+");
                for (String word : words) {
                    bannedWords.add(word);
                    System.out.println(bannedWords.indexOf(word)+". "+word);
                }
            }

            Scanner scanner3 = new Scanner(System.in);
            int wordToRemoveOption= scanner3.nextInt();

            while(wordToRemoveOption<0 || wordToRemoveOption> bannedWords.size() ){
                System.out.println("Opção incorreta");
                wordToRemoveOption=scanner3.nextInt();
            }

            String wordToRemove= bannedWords.remove(wordToRemoveOption);

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
                for (String word : bannedWords) {
                    bw.write(word + "\n");
                }
                System.out.println("A palavra "+wordToRemove+" foi removida com sucesso.");
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     *this function is responsible for changing the maximum number of clients allowed in the server
     * it checks if the number provided by the user is greater than the Max clients if it is, a space to the server is added
     * if it´s smaller than the Max Clients it takes away a permit of the semaphore
     */
    private void changeMaxClients() {
        Scanner scanner4 = new Scanner(System.in);
        System.out.println("Introduza o novo número máximo de clientes:");
        int newMaxClients = scanner4.nextInt();
        while(newMaxClients <= 0){
            System.out.println("O número máximo de clientes deve ser maior que zero. Introduza um número válido:");
            newMaxClients = scanner4.nextInt();
        }
        if(newMaxClients > maxClients){
            semaphore.release(newMaxClients - maxClients);
        }
        else {
            semaphore.reducePermits(maxClients-newMaxClients);
        }
        maxClients = newMaxClients;
        System.out.println("O número máximo de clientes foi atualizado para "+maxClients+".");

    }

}