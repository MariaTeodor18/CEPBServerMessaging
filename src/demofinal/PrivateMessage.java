package demofinal;

public class PrivateMessage extends Message {

    String receiver;

    public PrivateMessage(String receiver, MessageType messageType, String message) {
        super(messageType, message);
        this.receiver = receiver;
    }
}
