/**
 * This class represents a message object that contains the id of the client worker and the message text.
 */
public class Message {

    private final int clientWorkerId;
    private String Message;


    /**
     * This is the constructor for the Message object.
     *
     * @param clientWorkerId is the Client Worker id.
     * @param Message is the message text.
     */
    public Message(int clientWorkerId, String Message){
        this.clientWorkerId = clientWorkerId;
        this.Message = Message;

    }

    /**
     * Getter for the Client Worker id.
     *
     * @return the id of the Client Worker.
     */
    public int getClientWorkerId() {
        return clientWorkerId;
    }

    /**
     * Getter for the message text.
     *
     * @return the message text.
     */
    public String getMessage() {
        return Message;
    }

    /**
     * Setter for the message text.
     *
     * @param message is the new message text.
     */
    public void setMessage(String message){
        this.Message = message;
    }
}
