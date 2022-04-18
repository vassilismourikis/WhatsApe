import java.io.Serializable;

public class Value implements Serializable {
    private String publisherName;

    public Value(String name){
        this.publisherName=name;
    }

    public String getPublisherName() {
        return publisherName;
    }

    public void setPublisherName(String publisherName) {
        this.publisherName = publisherName;
    }

}