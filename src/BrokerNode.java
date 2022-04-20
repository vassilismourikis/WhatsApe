import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.Executors;

public class BrokerNode implements Broker{

    private static List<BrokerInfo> brokers=new ArrayList<BrokerInfo>(Arrays.asList(new BrokerInfo("192.168.1.10"),new BrokerInfo("192.168.1.11"),new BrokerInfo("192.168.1.13")));
    private static HashMap<String, ArrayList<Value>> channelHistory =new HashMap<String, ArrayList<Value>>();
    private static HashMap<String, ArrayList<String>> channelSubs=new HashMap<String, ArrayList<String>>();

    // All client names, so we can check for duplicates upon registration.
    private static Set<String> names = new HashSet<>();

    // The set of all the print writers for all the clients, used for broadcast.
    private static Set<ObjectOutputStream> writers = new HashSet<>();

    public static void main(String[] args) throws Exception {
        System.out.println("The chat server is running...");
        var pool = Executors.newFixedThreadPool(500);
        try (var listener = new ServerSocket(9090)) {
            while (true) {
                pool.execute(new Handler(listener.accept()));
            }
        }
    }

    @Override
    public Consumer acceptConnection(Consumer consumer) {
        return null;
    }

    @Override
    public Publisher acceptConnection(Publisher publisher) {
        return null;
    }

    @Override
    public void calculateKeys() {

    }

    @Override
    public void filterConsumers(String con) {

    }

    @Override
    public void notifyBrokersOnChanges() {

    }

    @Override
    public void notifyPublisher(String pub) {

    }

    @Override
    public void pull(String top) {

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

    private static class Handler implements Runnable {
        private String name;
        private Socket socket;
        private ObjectInputStream in;
        private ObjectOutputStream out;

        /**
         * Constructs a handler thread, squirreling away the socket. All the interesting
         * work is done in the run method. Remember the constructor is called from the
         * server's main method, so this has to be as short as possible.
         */
        public Handler(Socket socket) {
            this.socket = socket;
        }

        /**
         * Services this thread's client by repeatedly requesting a screen name until a
         * unique one has been submitted, then acknowledges the name and registers the
         * output stream for the client in a global set, then repeatedly gets inputs and
         * broadcasts them.
         */
        public void run() {
            try {
                in = new ObjectInputStream(socket.getInputStream());
                out = new ObjectOutputStream(socket.getOutputStream());

                // Keep requesting a name until we get a unique one.
                while (true) {
                    out.writeObject(new TextValue("server","SUBMITNAME"));
                    name = ((TextValue)in.readObject()).getMessage();
                    if (name == null) {
                        return;
                    }
                    synchronized (names) {
                        if (!name.isBlank() && !names.contains(name)) {
                            names.add(name);
                            break;
                        }
                    }
                }

                // Now that a successful name has been chosen, add the socket's print writer
                // to the set of all writers so this client can receive broadcast messages.
                // But BEFORE THAT, let everyone else know that the new person has joined!
                out.writeObject(new TextValue("server","NAMEACCEPTED" + name));
                for (ObjectOutputStream writer : writers) {
                    writer.writeObject(new TextValue("server","MESSAGE " + name + " has joined"));
                }
                writers.add(out);
                out.writeObject(new TextValue("server",brokers.get(0).toString()+brokers.get(1).toString()+brokers.get(2).toString()));
                String channel=null;
                // Accept messages from this client and broadcast them.
                while (true) {
                    Value incomingObject= (Value)in.readObject();
                    String input =((TextValue)incomingObject).getMessage();
                    if(channel!=null){
                        List<Value> history=channelHistory.get(channel);
                        history.add(incomingObject);
                    }
                    if (input.toLowerCase().startsWith("/quit")) { //disconnect
                        return;
                    }else if(input.startsWith("/channel")){ //user picks channel to send message, broker checks if he is registered and initialises the channel var to know where to keep incoming messages as history
                        if(channelSubs.get(input.substring(8))!=null){
                            if(channelSubs.get(input.substring(8)).contains(name)) channel=input.substring(8);
                        }
                        else{
                            var subs=channelSubs.get(input.substring(9));
                            if(subs!= null){
                                subs.add(name);
                                channelSubs.put(input.substring(9), subs);
                                channel=input.substring(8);
                            }
                            else{
                                channelHistory.put(input.substring(9), new ArrayList<Value>(Arrays.asList(incomingObject)));
                                channelSubs.put(input.substring(9), new ArrayList<String>(Arrays.asList(name)));
                                out.writeObject(new TextValue("server","channel doesn't exist, just created"));
                            }
                        }
                    }
                    else if(input.startsWith("/register")){                        //Registers consumer to a channel
                        var subs=channelSubs.get(input.substring(9));
                        if(subs!= null){
                            subs.add(name);
                            channelSubs.put(input.substring(9), subs);
                        }
                        else{
                            channelHistory.put(input.substring(9), new ArrayList<Value>(Arrays.asList(incomingObject)));
                            channelSubs.put(input.substring(9), new ArrayList<String>(Arrays.asList(name)));
                            out.writeObject(new TextValue("server","channel doesn't exist, just created"));
                        }
                    }else if(input.startsWith("/unregister")){//Unregisters consumer from a channel
                        var subs=channelSubs.get(input.substring(9));
                        if(subs!= null){
                            subs.remove(name);
                            channelSubs.put(input.substring(9), subs);
                        }
                        else{
                            out.writeObject(new TextValue("server","not registered to this channel"));
                        }
                    }
                    for (ObjectOutputStream writer : writers) {
                        //TODO: THREAD
                        writer.writeObject(new TextValue("server","MESSAGE " + name + ": " + input));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (out != null) {
                    writers.remove(out);
                }
                if (name != null) {
                    System.out.println(name + " is leaving");
                    names.remove(name);
                    for (ObjectOutputStream writer : writers) {
                        try {
                            writer.writeObject(new TextValue("server", "MESSAGE " + name + " has left"));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
    }

}
