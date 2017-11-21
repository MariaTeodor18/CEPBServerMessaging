package demofinal;

public class Message {

    String message;
    private MessageType messageType;

    public Message(MessageType messageType, String message){
        this.messageType = messageType;
        this.message = message;
    }

    public  String getMessage(){
        return  message;
    };
}
