import java.io.Serializable;

public class Value implements Serializable {
    private String publisherName;
    private String message;

    public Value(String name){
        this.publisherName=name;
        message= null;
    }

    public String getPublisherName() {
        return publisherName;
    }

    public void setPublisherName(String publisherName) {
        this.publisherName = publisherName;
    }

    public String getMessage(){
        return message;
    }
}