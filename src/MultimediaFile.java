import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

public class MultimediaFile {

    private String multimediaFileName;
    private String profileName;
    private String dateCreated;
    private String length;
    private String framerate;
    private String frameWidth;
    private String frameHeight;
    private byte[] multimediaFileChunk;

    public MultimediaFile(String multimediaFileName,String profileName) throws IOException, TikaException, SAXException {
        this.multimediaFileName = multimediaFileName;

        File file = new File(multimediaFileName);

        //Parser method parameters
        Parser parser = new AutoDetectParser();
        BodyContentHandler handler = new BodyContentHandler();
        Metadata metadata = new Metadata();
        FileInputStream inputstream = new FileInputStream(file);
        ParseContext context = new ParseContext();

        parser.parse(inputstream, handler, metadata, context);

        this.profileName = profileName;
        this.dateCreated = metadata.get("dcterms:created");
        this.length = metadata.get("xmpDM:duration");
        this.frameWidth = metadata.get("tiff:ImageWidth");
        this.frameHeight = metadata.get("tiff:ImageLength");

        System.out.println(profileName + "\n"+dateCreated + "\n"+ length + "\n"+ frameHeight+ "\n"+frameWidth);

        //init array with file length
        byte[] bytesArray = new byte[(int) file.length()];

        FileInputStream fis = new FileInputStream(file);
        fis.read(bytesArray); //read file into bytes[]
        fis.close();
        this.multimediaFileChunk = bytesArray;
    }

    public String getMultimediaFileName() {
        return multimediaFileName;
    }

    public void setMultimediaFileName(String multimediaFileName) {
        this.multimediaFileName = multimediaFileName;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public String getFramerate() {
        return framerate;
    }

    public void setFramerate(String framerate) {
        this.framerate = framerate;
    }

    public String getFrameWidth() {
        return frameWidth;
    }

    public void setFrameWidth(String frameWidth) {
        this.frameWidth = frameWidth;
    }

    public String getFrameHeight() {
        return frameHeight;
    }

    public void setFrameHeight(String frameHeight) {
        this.frameHeight = frameHeight;
    }

    public byte[] getMultimediaFileChunk() {
        return multimediaFileChunk;
    }

    public void setMultimediaFileChunk(byte[] multimediaFileChunk) {
        this.multimediaFileChunk = multimediaFileChunk;
    }
}
