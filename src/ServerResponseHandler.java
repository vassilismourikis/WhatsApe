import org.apache.tika.exception.TikaException;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;

/*
This class is used for printing the server's responses because in the main scanner blocks the incoming updates while waits for user's input.
 */
public class ServerResponseHandler implements Runnable{
    private Socket server;
    private ObjectInputStream in;
    private UserNode client;
    Object obj = null;

    public ServerResponseHandler(Socket s,UserNode c) throws IOException {
        this.server = s;
        this.in = new ObjectInputStream(server.getInputStream());
        this.client=c;
    }

    @Override
    public void run(){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        ArrayList<byte[]> chunks = new ArrayList<byte[]>();
        Integer videonum=0;
        String serverResponse=null;
            try{

                while(true) {
                    obj = in.readObject();
                    Value incomingObject=null;
                    try {
                        incomingObject = (Value) obj;
                    }
                    catch (ClassCastException ce) {

                        if (chunks.isEmpty()) videonum++;
                        try {
                            client.sendResp("Receiving video chunks");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        chunks.add((byte[]) obj);
                        continue;
                    }
                    try {
                        serverResponse = ((TextValue)incomingObject).getMessage();
                    }catch (NullPointerException n){
                        try {
                            BrokerNode.writeBytesToFile("video"+videonum+".mp4", chunks);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        chunks= new ArrayList<byte[]>();
                        continue;
                    }
                    if(serverResponse==null) break;
                    if (serverResponse.startsWith("SUBMITNAME")) {
                        System.out.println("["+(dtf.format(LocalDateTime.now()))+"]: "+"Specify your UserName by typing /name <name>" );
                    } else if (serverResponse.startsWith("NAMEACCEPTED")) {
                        System.out.println("["+(dtf.format(LocalDateTime.now()))+"]: "+"Name: " + serverResponse.substring(13) );
                    } else if (serverResponse.startsWith("MESSAGE")) {
                        System.out.println("["+(dtf.format(LocalDateTime.now()))+"]: "+serverResponse.substring(8)  + "\n");
                    } else{ //brokerinfo
                        System.out.println("["+(dtf.format(LocalDateTime.now()))+"]: "+serverResponse  + "\n");
                    }
                }
            }
            catch(IOException | ClassNotFoundException e){
                e.printStackTrace();
            }finally {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }

