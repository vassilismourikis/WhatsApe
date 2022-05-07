import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.*;

public class UserNode {
    private static ProfileName profileName;

    static Scanner scanner = new Scanner(System.in);
    static String serverAddress;
    static ObjectOutputStream out;
    static List<BrokerInfo> brokers=new ArrayList<BrokerInfo>(Arrays.asList(new BrokerInfo("192.168.1.14")));//new BrokerInfo("192.168.1.14"),new BrokerInfo("192.168.1.11"),
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

    public void sendResp(String resp) throws IOException {
        System.out.println("message send");
        out.writeObject(new TextValue(getProfileName(),resp));
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
                }else if(input.startsWith("/upload")){
                    push(new MultimediaValue(null,new MultimediaFile(input.substring(8),client.getProfileName())));
                }else if(input.startsWith("/getvideo")) {
                    pull(input);
                }else if(input.startsWith("/gethistory")){
                    out.writeObject(new TextValue(channel,input));
                }
                else{
                    out.writeObject(new TextValue(channel,input));
                }


            }
        } finally {
            System.err.println("Error occurred during communication");
        }
    }


    public static void push(MultimediaValue file) throws IOException {
        ArrayList<byte[]> chunks = file.getMultimediaFile().getMultimediaFileChunk();
        out.writeObject(new TextValue(getProfileName(), "VIDEONAME "+file.getMultimediaFile().getMultimediaFileName()));
        for(byte[] chunk : chunks) {
            out.writeObject(chunk);
        }
        out.writeObject(null);
    }

    public static void pull(String topic) throws IOException {
        out.writeObject(new TextValue(channel,topic));
    }
}
