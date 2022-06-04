import com.example.whatsape.MultimediaFile;
import com.example.whatsape.MultimediaValue;
import com.example.whatsape.TextValue;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.util.*;

public class UserNode {
    private static ProfileName profileName;

    static Scanner scanner = new Scanner(System.in);
    static String serverAddress;
    static ObjectOutputStream out;
    static List<BrokerInfo> brokers=new ArrayList<BrokerInfo>(Arrays.asList(new BrokerInfo("192.168.1.14"),new BrokerInfo("192.168.1.16"),new BrokerInfo("192.168.1.13")));
    static List<BrokerInfo> sortedBrokers=new ArrayList<BrokerInfo>();
    static String channel=null;
    static ArrayList<BigInteger> brokerHashes=new ArrayList<BigInteger>() {
        {
            add(brokers.get(0).getMaxHash());

            add(brokers.get(1).getMaxHash());

            add(brokers.get(2).getMaxHash());
        }
    };


    public UserNode(String serverAddress,String name) {
        this.serverAddress = serverAddress;
        this.profileName= new ProfileName(name);
        Collections.sort(brokerHashes);
        for(BigInteger b : brokerHashes){
            System.out.println(b);
        }
        BrokerInfo b=brokers.get(0);
        int counter=0,num=0;
        while(sortedBrokers.size()<3){//sorting brokers array
            if(b.getMaxHash()==brokerHashes.get(counter)) {
                sortedBrokers.add(b);
                counter++;
            }
            if(num==3) num=0;
            b=brokers.get(num++);
        }
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
        out.flush();
    }



    public static void main(String[] args) throws Exception {

        UserNode client = new UserNode(brokers.get(new Random().nextInt(brokers.size())).getIp(),"");
        try {
            String otherPeer=brokers.get(new Random().nextInt(brokers.size())).getIp();
            var socket = new Socket(otherPeer, 9090);
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
                    out.flush();
                    continue;
                }else if(input.startsWith("/channel")){ //user picks channel to send message, broker checks if he is registered and initialises the channel var to know where to keep incoming messages as history
                    channel = input.substring(9);
                    BigInteger channelHash=MD5.getMd5(channel);
                    if(channelHash.mod(brokerHashes.get(2)).compareTo(sortedBrokers.get(0).getMaxHash())<0){
                        if(!otherPeer.equals(sortedBrokers.get(0).getIp())){
                            otherPeer=sortedBrokers.get(0).getIp();
                            socket = new Socket(otherPeer, 9090);
                            out = new ObjectOutputStream(socket.getOutputStream());
                            serverConn.stop();
                            serverConn = new ServerResponseHandler(socket,client);
                            new Thread(serverConn).start();
                            out.writeObject(new TextValue(getProfileName(),"/name "+client.getProfileName()));
                            out.flush();
                            out.writeObject(new TextValue(channel,input));
                            out.flush();
                            continue;
                        }

                    }else if(channelHash.mod(brokerHashes.get(2)).compareTo(sortedBrokers.get(1).getMaxHash())<0){
                        if(!otherPeer.equals(sortedBrokers.get(1).getIp())){
                            otherPeer=sortedBrokers.get(1).getIp();
                            socket = new Socket(otherPeer, 9090);
                            out = new ObjectOutputStream(socket.getOutputStream());
                            serverConn.stop();
                            serverConn = new ServerResponseHandler(socket,client);
                            new Thread(serverConn).start();
                            out.writeObject(new TextValue(getProfileName(),"/name "+client.getProfileName()));
                            out.flush();
                            out.writeObject(new TextValue(channel,input));
                            out.flush();
                            continue;
                        }

                    }else if(channelHash.mod(brokerHashes.get(2)).compareTo(sortedBrokers.get(2).getMaxHash())<0){
                        if(!otherPeer.equals(sortedBrokers.get(2).getIp())){
                            otherPeer=sortedBrokers.get(2).getIp();
                            socket = new Socket(otherPeer, 9090);
                            out = new ObjectOutputStream(socket.getOutputStream());
                            serverConn.stop();
                            serverConn = new ServerResponseHandler(socket,client);
                            new Thread(serverConn).start();
                            out.writeObject(new TextValue(getProfileName(),"/name "+client.getProfileName()));
                            out.flush();
                            out.writeObject(new TextValue(channel,input));
                            out.flush();
                            continue;
                        }

                    }
                    out.writeObject(new TextValue(channel,input));
                    out.flush();
                }else if(input.startsWith("/upload")){
                    push(new MultimediaValue(null,new MultimediaFile(input.substring(8),client.getProfileName())));
                }else if(input.startsWith("/getvideo")) {
                    pull(input);
                }else if(input.startsWith("/gethistory")){
                    //for some reason get history not working after the first time (MUST INVESTIGATE)
                    //so we restart the connection after every time
                    out.writeObject(new TextValue(channel,input));
                    out.flush();
                    out.reset();
                }
                else{
                    out.writeObject(new TextValue(channel,input));
                    out.flush();
                }


            }
        } finally {
            System.err.println("Error occurred during communication");
        }
    }


    public static void push(MultimediaValue file) throws IOException {
        ArrayList<byte[]> chunks = file.getMultimediaFile().getMultimediaFileChunk();
        out.writeObject(new TextValue(getProfileName(), "VIDEONAME "+file.getMultimediaFile().getMultimediaFileName()));
        out.flush();
        for(byte[] chunk : chunks) {
            out.writeObject(chunk);
            out.flush();
        }
        out.writeObject(null);
        out.flush();
    }

    public static void pull(String topic) throws IOException {
        out.writeObject(new TextValue(channel,topic));
        out.flush();
    }
}
