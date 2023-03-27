import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.LinkedList;
import java.util.NoSuchElementException;

import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;

public class FilterTest {

    Queue<Message> buffer;


    Queue<Message> filteredBuffer;

    ReentrantLock bufferLock;

    ReentrantLock filteredBufferLock;
    private Filter f;

    private Message m;
    private Message m2;
    private String FilteredMessage = "A message was removed for being inappropriate";

    private String MessageTest = "bom dia";

    @BeforeEach
    void setUp ( ) throws IOException {
        String AbsolutePath="C:\\Users\\FF\\Desktop\\Universidade\\2Semestre\\Programação Avançada\\Práticas\\ProjetoPA1_Grupo5\\server\\src\\test\\java\\ServerMenuTest.java";
        bufferLock = new ReentrantLock();
        filteredBufferLock = new ReentrantLock();
        filteredBuffer = new LinkedList<>();
        buffer = new LinkedList<>();
        Message m = new Message(1, "hello");
        Message m2 = new Message(1, "bom dia");
        buffer.add(m);
        buffer.add(m2);
        f = new Filter(buffer, filteredBuffer, bufferLock, filteredBufferLock);
        Filter.bannedWordsFile(AbsolutePath);
    }

    @Test
    void testVerify() throws IOException {
        assertAll(
                ( ) -> assertFalse ( f.FilterVerify("bom dia")),
                ( ) -> assertFalse ( f.FilterVerify("hello")),
                ( ) -> assertFalse ( f.FilterVerify("HELLo bom dia"))
        );

    }

    /*@Test
    public void testFilter1() throws InterruptedException {

        f.start();
        sleep(1000);
        assertEquals(FilteredMessage, f.getFilteredBuffer().poll().getMessage());
        assertEquals(MessageTest, f.getFilteredBuffer().poll().getMessage());

    }*/


    @Test
    public void testException() {
        f.start();

        f.interrupt();
        assertThrows(RuntimeException.class, () -> f.run());

    }
}
