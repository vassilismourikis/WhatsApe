import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ServerResponseHandler implements Runnable{
    private Socket server;
    private BufferedReader in;

    public ServerResponseHandler(Socket s) throws IOException {
        this.server = s;
        this.in = new BufferedReader(new InputStreamReader(server.getInputStream()));
    }

    @Override
    public void run(){

            String serverResponse=null;
            try{

                while(true) {

                    serverResponse = in.readLine();
                    if(serverResponse==null) break;
                    if (serverResponse.startsWith("SUBMITNAME")) {
                        System.out.println("Specify your UserName by typing /name <name>" );
                    } else if (serverResponse.startsWith("NAMEACCEPTED")) {
                        System.out.println("Chatter - " + serverResponse.substring(13) );
                    } else if (serverResponse.startsWith("MESSAGE")) {
                        System.out.println(serverResponse.substring(8)  + "\n");
                    }
                }
            }
            catch(IOException e){
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

