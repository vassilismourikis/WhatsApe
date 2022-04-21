import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.*;

public class UserNode implements Consumer,Publisher {
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
                }else if(input.startsWith("/multi")){
                    push(channel,new MultimediaValue(null,new MultimediaFile("C:\\Users\\Vasilis Mourikis\\Downloads\\test",client.getProfileName())));
                }
                    out.writeObject(new TextValue(channel,input));

            }
        } finally {
            System.err.println("Error occurred during communication");
        }
    }

    @Override
    public void disconnect(String str) {

    }

    @Override
    public void register(String topic) {

    }

    @Override
    public void showConversationData(String topic) {

    }

    @Override
    public void connect() {

    }

    @Override
    public void disconnect() {

    }

    @Override
    public void init() {

    }

    @Override
    public void updateNodes() {

    }

    @Override
    public ArrayList<Value> generateChunks(MultimediaFile file) {
        return null;
    }

    @Override
    public ArrayList<Broker> getBrokerList() {
        return null;
    }

    @Override
    public Broker hashTopic(String str) {
        return null;
    }

    @Override
    public void notifyBrokersNewMessage(String message) {

    }

    @Override
    public void notifyFailure(Broker broker) {

    }

    @Override
    public void push(String top, Value value) {

    }
}
