import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
/*
This class is used for printing the server's responses because in the main scanner blocks the incoming updates while waits for user's input.
 */
public class ServerResponseHandler implements Runnable{
    private Socket server;
    private ObjectInputStream in;

    public ServerResponseHandler(Socket s) throws IOException {
        this.server = s;
        this.in = new ObjectInputStream(server.getInputStream());
    }

    @Override
    public void run(){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");


        String serverResponse=null;
            try{

                while(true) {
                    Value value=(Value) in.readObject();
                    serverResponse = value.getMessage();
                    System.out.println(serverResponse);
                    if(serverResponse==null) break;
                    if (serverResponse.startsWith("SUBMITNAME")) {
                        System.out.println("["+(dtf.format(LocalDateTime.now()))+"]: "+"Specify your UserName by typing /name <name>" );
                    } else if (serverResponse.startsWith("NAMEACCEPTED")) {
                        System.out.println("["+(dtf.format(LocalDateTime.now()))+"]: "+"Chatter - " + serverResponse.substring(13) );
                    } else if (serverResponse.startsWith("MESSAGE")) {
                        System.out.println("["+(dtf.format(LocalDateTime.now()))+"]: "+serverResponse.substring(8)  + "\n");
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

