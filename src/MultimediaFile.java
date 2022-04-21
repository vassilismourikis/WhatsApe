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
        System.out.println(handler.toString());

        //getting the list of all meta data elements
        String[] metadataNames = metadata.names();

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



}
