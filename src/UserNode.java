import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.*;

public class UserNode {
    private static ProfileName profileName;

    static Scanner scanner = new Scanner(System.in);
    static String serverAddress;
    static ObjectOutputStream out;
    static List<BrokerInfo> brokers=new ArrayList<BrokerInfo>(Arrays.asList(new BrokerInfo("192.168.1.10"),new BrokerInfo("192.168.1.11"),new BrokerInfo("192.168.1.13")));



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
        new MultimediaFile("C:\\Users\\Vasilis Mourikis\\Downloads\\test.mp4",client.getProfileName());
        try {
            var socket = new Socket(client.serverAddress, 9090);
            out = new ObjectOutputStream(socket.getOutputStream());
            ServerResponseHandler serverConn = new ServerResponseHandler(socket,client);
            new Thread(serverConn).start();
            String input,channel=null;
            System.out.println(client.getProfileName());
            while (true) {
                input=scanner.nextLine();
                if(input.startsWith("/name")){
                    client.setProfileName(input.substring(5));
                    out.writeObject(new TextValue(getProfileName(),client.getProfileName()));
                    continue;
                }else if(input.startsWith("/channel")){ //user picks channel to send message, broker checks if he is registered and initialises the channel var to know where to keep incoming messages as history
                        channel = input.substring(8);
                        continue;
                }else if(input.startsWith("/multi")){
                    push(channel,new MultimediaValue(null,new MultimediaFile("C:\\Users\\Vasilis Mourikis\\Downloads\\test",client.getProfileName())));
                }else{
                    out.writeObject(new TextValue(channel,input));
                }


            }
        } finally {
            System.err.println("Error occurred during communication");
        }
    }


    public static void push(String top, MultimediaValue file) throws IOException {
        byte[] chunks = file.getMultimediaFile().getMultimediaFileChunk();
        for(int i=0;i<chunks.length;i++) {
            out.writeObject(chunks[i]);
        }
    }
}
