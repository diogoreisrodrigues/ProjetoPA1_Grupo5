import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;

/**
 *
 */
public class ServerMenu extends Thread {

    private final Logger logger;
    private int maxClients;
    private MySemaphore semaphore;

    /**
     * @param logger
     * @param maxClients
     * @param semaphore
     */
    public ServerMenu(Logger logger, int maxClients, MySemaphore semaphore) {
        this.logger = logger;
        this.maxClients = maxClients;
        this.semaphore = semaphore;
    }

    /**
     *
     */
    public void run(){
        try {
            serverMenu();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
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
                    case 1:
                        System.out.println("Para sair do logger introduza '0'");
                        Scanner scanner1 = new Scanner(System.in);
                        int choice1 = 1;
                        ConsoleHandler ch = new ConsoleHandler();
                        ch.setFormatter(new MyFormatter());
                        logger.addHandler(ch);
                        while (choice1 != 0) {
                            choice1 = scanner.nextInt();
                        }
                        System.out.println("Saiu do logger");
                        logger.removeHandler(ch);
                        break;
                    case 2:
                        System.out.println("Adicione a palavra:");
                        Scanner scanner2= new Scanner(System.in);
                        String word= scanner2.nextLine();
                        while(Objects.equals(word, "")){
                            System.out.println("Não é possível adicionar um espaço em branco. Introduza uma palavra válida: ");
                            word=scanner2.nextLine();
                        }
                        word=word.toLowerCase();
                        if(wordInFile("bannedWords.txt",word)){
                            System.out.println("A palavra que pretende adicionar já existe no ficheiro de palavras proibidas.");
                        }
                        else {
                            addWordToFile("bannedWords.txt", word);
                            System.out.println("A palavra "+word+" foi adicionada com sucesso.");
                        }
                        break;
                    case 3:
                        removeWordsFromFile("bannedWords.txt");
                        break;
                    case 4:
                        changeMaxClients();
                        break;
                    default:
                        System.out.println("Opção incorreta");
                        break;
                }
            }
        });
        m.start();
    }

    /**
     *
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

    /**
     * @param filePath
     * @param word
     */
    public void addWordToFile(String filePath, String word) {
        try {

            StringBuilder fileContent = new StringBuilder();
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String line = reader.readLine();

            while (line != null) {
                fileContent.append(line);
                fileContent.append(System.lineSeparator());
                line = reader.readLine();
            }
            reader.close();

            int lastLineIndex = fileContent.lastIndexOf(System.lineSeparator());
            if (lastLineIndex != -1) {
                fileContent.insert(lastLineIndex + 1, word);
            }

            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
            writer.write(fileContent.toString());
            writer.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param fileName
     * @param word
     * @return
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
     * @param fileName
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
}