/*import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ServerMenuTest {

    private Logger logger;
    private ServerMenu sm;
    private MySemaphore semaphore;
    private int maxClients;

    @BeforeEach
    void setUp() throws IOException {
        this.sm = new ServerMenu(logger,maxClients,semaphore);
    }

    @Test
    public void testWordInFile() {
        String AbsolutePath="C:\\Users\\FF\\Desktop\\Universidade\\2Semestre\\Programação Avançada\\Práticas\\ProjetoPA1_Grupo5\\server\\src\\test\\java\\ServerMenuTest.java";
        assertAll(
                () -> assertFalse(sm.wordInFile(AbsolutePath, "hello")),
                () -> assertTrue(sm.wordInFile(AbsolutePath, "casa"))
        );
        assertThrows(RuntimeException.class, () -> {
            sm.wordInFile("invalidFileName.txt", "hello");
        });
    }

    @Test
    public void testAddWordToFile() throws IOException {

        //Caso em que o ficheiro está vazio

        File tempFile = File.createTempFile("testFile", ".txt");
        sm.addWordToFile(tempFile.getPath(), "test");

        BufferedReader reader = new BufferedReader(new FileReader(tempFile));
        List<String> lines = reader.lines().toList();
        reader.close();

        assertEquals(lines.size(), 1);
        assertTrue(lines.get(0).contains("test"));

        tempFile.delete();

        //Caso em que o ficheiro já tem conteúdo

        File tempFile1 = File.createTempFile("testFile1", ".txt");
        BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile1));
        writer.write("test");
        writer.close();
        sm.addWordToFile(tempFile1.getPath(), "test1");

        BufferedReader reader1 = new BufferedReader(new FileReader(tempFile1));
        List<String> lines1 = reader1.lines().toList();
        reader1.close();
        tempFile1.delete();

        assertEquals(lines1.size(), 2);
        assertTrue(lines1.get(1).contains("test1"));
        assertThrows(RuntimeException.class, () -> sm.addWordToFile(tempFile.getPath(), "world"));

    }



}*/