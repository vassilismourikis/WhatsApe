public class MultimediaFile {

    private String multimediaFileName;
    private String profileName;
    private String dateCreated;
    private String length;
    private String framerate;
    private String frameWidth;
    private String frameHeight;
    private byte[] multimediaFileChunk;

    public MultimediaFile(String multimediaFileName, String profileName, String dateCreated, String length, String framerate, String frameWidth, String frameHeight, byte[] multimediaFileChunk) {
        this.multimediaFileName = multimediaFileName;
        this.profileName = profileName;
        this.dateCreated = dateCreated;
        this.length = length;
        this.framerate = framerate;
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        this.multimediaFileChunk = multimediaFileChunk;
    }
}
