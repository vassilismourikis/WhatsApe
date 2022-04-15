import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class UserNode implements Consumer,Publisher {
    private static ProfileName profileName;

    static Scanner scanner = new Scanner(System.in);
    static String serverAddress;
    static PrintWriter out;



    public UserNode(String serverAddress,String name) {
        this.serverAddress = serverAddress;
        this.profileName= new ProfileName(name);
    }

    private static String getProfileName() {
        return profileName.getProfileName();
    }

    private static void setProfileName(String name){
        profileName.setProfileName(name);
    }



    public static void main(String[] args) throws Exception {

        UserNode client = new UserNode("localhost","");
        try {
            var socket = new Socket(serverAddress, 9090);
            out = new PrintWriter(socket.getOutputStream(), true);
            ServerResponseHandler serverConn = new ServerResponseHandler(socket);
            new Thread(serverConn).start();
            String input;
            while (true) {
                input=scanner.nextLine();
                if(input.startsWith("/name")){
                    client.setProfileName(input.substring(5));
                    out.println(client.getProfileName());
                    continue;
                }
                    out.println(input);

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
