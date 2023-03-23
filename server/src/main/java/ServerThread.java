import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.*;

public class ServerThread extends Thread {
    private final int port;
    private DataInputStream in;
    private PrintWriter out;
    private ServerSocket server;
    private Socket socket;
    private final ExecutorService executor;
    private static final Logger logger = Logger.getLogger(ServerThread.class.getName());
    private final ReentrantLock lockLog;
    private final Queue<String> queueToLog;
    private AtomicInteger counterId;
    private int maxClients;
    private final Semaphore semaphore;

    Queue<Message> buffer = new LinkedList<>();

    Queue<Message> filteredBuffer = new LinkedList<>();

    ReentrantLock bufferLock;

    ReentrantLock filteredBufferLock;

    public ServerThread ( int port ) throws IOException {
        this.port = port;
        this.maxClients = readMaxClientsFromConfig();
        this.executor = Executors.newFixedThreadPool(4);         //Por agora nthread ta um numero fixo mas depois corrigir para ficar dinâmico
        this.semaphore = new Semaphore(maxClients);
        this.counterId = new AtomicInteger(0);

        try {
            server = new ServerSocket ( this.port );
        } catch ( IOException e ) {
            e.printStackTrace ( );
        }
        this.lockLog=new ReentrantLock();

        this.bufferLock = new ReentrantLock();
        this.filteredBufferLock = new ReentrantLock();
        this.queueToLog= new LinkedList<>();

    }



    /**
     * Explicar Java Doc
     */
    public void run ( ) {
        try {
            setupLogger();
            setupLogThread();
            logger.info("Server started");
            System.out.println ( "Accepting Data" );

            Filter f = startFilter(buffer, filteredBuffer, bufferLock, filteredBufferLock);
            f.start();
            Filter f2 = startFilter(buffer, filteredBuffer, bufferLock, filteredBufferLock);
            f2.start();

            serverMenu();

            acceptClient();
        } catch ( IOException e ) {
            throw new RuntimeException();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void acceptClient() throws IOException, InterruptedException {

        Thread t = new Thread(() -> {
            while (true) {
                try {
                    Socket socket = server.accept();
                    semaphore.acquire();

                    int id = counterId.incrementAndGet();



                    ClientWorker clientWorker = new ClientWorker(socket, logger, id, semaphore,lockLog, buffer, filteredBuffer , bufferLock, filteredBufferLock ,queueToLog);

                    executor.submit(clientWorker);

                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        t.start();
        t.join();
    }

    private int readMaxClientsFromConfig() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("server.config"));
        String line;
        int maxClients = 0;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("maxClients = ")) {
                maxClients = Integer.parseInt(line.substring("maxClients = ".length()));
            }
        }
        reader.close();
        return maxClients;
    }


    private void setupLogger() throws IOException {
        Handler[] handlers = logger.getHandlers();
        for(Handler handler : handlers)
        {
            if(handler.getClass() == ConsoleHandler.class)
                logger.removeHandler(handler);
        }
        FileHandler fh;
        fh = new FileHandler("server.log");
        logger.addHandler(fh);

        MyFormatter formatter = new MyFormatter();
        fh.setFormatter(formatter);
        logger.setUseParentHandlers(false);
    }

    private void setupLogThread(){
        LogThread l = new LogThread(queueToLog,lockLog,logger);
        l.start();
    }
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

                    default:
                        System.out.println("Opção incorreta");
                        break;
                }
            }
        });
        m.start();
    }

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

    public void closeServer(){
        //TODO: function that ends the server thread
    }


    public Filter startFilter(Queue<Message> buffer, Queue<Message> filteredBuffer, ReentrantLock bufferLock, ReentrantLock filteredBufferLock){
        Filter f= null;
        try {
            f = new Filter(buffer, filteredBuffer, bufferLock, filteredBufferLock);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return f;
    }
}
