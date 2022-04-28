import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.*;

public class UserNode {
    private static ProfileName profileName;

    static Scanner scanner = new Scanner(System.in);
    static String serverAddress;
    static ObjectOutputStream out;
    static List<BrokerInfo> brokers=new ArrayList<BrokerInfo>(Arrays.asList(new BrokerInfo("192.168.1.9"),new BrokerInfo("192.168.1.11"),new BrokerInfo("192.168.1.13")));
    static String channel=null;


    public UserNode(String serverAddress,String name) {
        this.serverAddress = serverAddress;
        this.profileName= new ProfileName(name);
    }

    public static String getProfileName() {
        return profileName.getProfileName();
    }

    public static void setProfileName(String name){
        profileName.setProfileName(name);
    }



    public static void main(String[] args) throws Exception {

        UserNode client = new UserNode(brokers.get(new Random().nextInt(brokers.size())).getIp(),"");
        try {
            var socket = new Socket(client.serverAddress, 9090);
            out = new ObjectOutputStream(socket.getOutputStream());
            ServerResponseHandler serverConn = new ServerResponseHandler(socket,client);
            new Thread(serverConn).start();
            String input=null;
            System.out.println(client.getProfileName());
            while (true) {
                input=scanner.nextLine();
                if(input.startsWith("/name")){
                    client.setProfileName(input.substring(5));
                    out.writeObject(new TextValue(getProfileName(),client.getProfileName()));
                    continue;
                }else if(input.startsWith("/channel")){ //user picks channel to send message, broker checks if he is registered and initialises the channel var to know where to keep incoming messages as history
                        channel = input.substring(8);
                        out.writeObject(new TextValue(channel,input));
                }else if(input.startsWith("/multi")){
                    push(channel,new MultimediaValue(null,new MultimediaFile("C:\\Users\\Vasilis Mourikis\\Downloads\\test.mp4",client.getProfileName())));
                }else{
                    out.writeObject(new TextValue(channel,input));
                }


            }
        } finally {
            System.err.println("Error occurred during communication");
        }
    }


    public static void push(String top, MultimediaValue file) throws IOException {
        ArrayList<byte[]> chunks = file.getMultimediaFile().getMultimediaFileChunk();
        out.writeObject(new TextValue(getProfileName(),"LENGTH "+chunks.size()));
        out.writeObject(new TextValue(getProfileName(),"VIDEOCHANNEL:"+channel));
        for(byte[] chunk : chunks) {
            out.writeObject(chunk);
            if(chunk==null) break;
        }
    }
}
