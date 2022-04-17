public class MultimediaValue extends Value{

    private MultimediaFile multimediaFile;

    public MultimediaValue(String name,MultimediaFile multimediaFile) {
        super(name);
        this.multimediaFile = multimediaFile;
    }

}
