public class TextValue extends Value{

    private String message;

    public TextValue(String name,String text){
        super(name);
        this.message=text;
    }
}
