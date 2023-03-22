public class Message {


    private final int clientWorkerId;
    private String Message;

    public Message(int clientWorkerId, String Message){
        this.clientWorkerId = clientWorkerId;
        this.Message = Message;

    }

    public int getClientWorkerId() {
        return clientWorkerId;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message){
        this.Message = message;
    }
}
