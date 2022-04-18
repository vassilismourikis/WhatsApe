import java.io.Serializable;

public class TextValue extends Value implements Serializable {
    private String message;

    public TextValue(String name,String text){
        super(name);
        this.message=text;
    }
}