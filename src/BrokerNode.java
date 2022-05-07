import org.apache.commons.lang3.ObjectUtils;
import org.apache.tika.exception.TikaException;
import org.xml.sax.SAXException;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.Executors;

public class BrokerNode{

    private static List<BrokerInfo> brokers=new ArrayList<BrokerInfo>(Arrays.asList(new BrokerInfo("192.168.1.10"),new BrokerInfo("192.168.1.11"),new BrokerInfo("192.168.1.13")));
    private static HashMap<String, ArrayList<Value>> channelHistory =new HashMap<String, ArrayList<Value>>();

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
            //used for receiving video chunks
            ArrayList<byte[]> chunks = new ArrayList<byte[]>();
            Object obj = null;
            String channel=null;
            String videoName=null;
            //used for receiving video chunks
            try {
                in = new ObjectInputStream(socket.getInputStream());
                out = new ObjectOutputStream(socket.getOutputStream());

                // Keep requesting a name until we get a unique one.
                while (true) {
                    out.writeObject(new TextValue("server", "SUBMITNAME"));
                    name = ((TextValue) in.readObject()).getMessage();
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
                out.writeObject(new TextValue("server", "NAMEACCEPTED" + name));
                synchronized (writers) {
                    for (ObjectOutputStream writer : writers) {
                        writer.writeObject(new TextValue("server", "MESSAGE " + name + " has joined"));
                    }
                    writers.add(out);
                }
                // Accept messages from this client and broadcast them.
                while (true) {
                    obj = in.readObject();
                    Value incomingObject=null;
                    try {
                        incomingObject = (Value) obj;
                    }catch (ClassCastException ce) {
                            try {
                                out.writeObject(new TextValue("server","Receiving video chunks"));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            chunks.add((byte[]) obj);
                            continue;
                    }
                    String input =null;
                    try {
                        input = ((TextValue) incomingObject).getMessage();
                    }catch (NullPointerException n){
                        try {
                            writeBytesToFile(videoName.substring(videoName.lastIndexOf("/") + 1), chunks);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        chunks= new ArrayList<byte[]>();
                        continue;
                    }

                    if (input.toLowerCase().startsWith("/quit")) { //disconnect
                        return;
                    } else if (input.startsWith("/channel")) { //user picks channel to send message, broker checks if he is registered and initialises the channel var to know where to keep incoming messages as history
                                channel = input.substring(9);
                                synchronized (channelHistory) {
                                    if(!channelHistory.containsKey(channel)) {

                                        channelHistory.put(channel, new ArrayList<Value>());
                                        out.writeObject(new TextValue("server", "channel doesn't exist, just created"));

                                        continue;
                                    }
                                }
                    }
                    else if(input.startsWith("/getvideo")) {
                        ArrayList<byte[]> chunkss = (new MultimediaValue(null,new MultimediaFile(input.substring(10),"server"))).getMultimediaFile().getMultimediaFileChunk();
                        for(byte[] chunk : chunkss) {
                            out.writeObject(chunk);
                            in.readObject();
                        }
                        out.writeObject(null);
                        continue;
                    }else if(input.startsWith("VIDEONAME")) {
                        videoName=input.substring(10);
                    }else if(input.startsWith("/gethistory")){
                        synchronized (channelHistory) {
                            out.writeObject(channelHistory.get(input.substring(12)));
                        }
                        continue;
                    }

                    if (channel != null) {
                        synchronized (channelHistory) {
                            ArrayList<Value> history = channelHistory.get(channel);
                            history.add(new TextValue("server",  name + ": " + input));
                            channelHistory.replace(channel, history);
                            for(Value v : channelHistory.get(channel)){
                                System.out.println(v.getMessage());
                            }
                            System.out.println(channelHistory.size());
                        }
                    }
                    else{
                        out.writeObject(new TextValue("server", "SPECIFY CHANNEL"));
                    }

                    synchronized (writers) {
                        for (ObjectOutputStream writer : writers) {
                            String finalInput = input;
                            new Thread()
                            {
                                public void run() {
                                    try {
                                        writer.writeObject(new TextValue("server", "MESSAGE " + name + ": " + finalInput));
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }.start();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                synchronized (this){
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
            }
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }

    }
    public static void writeBytesToFile(String fileName, ArrayList<byte[]> bytes)
            throws IOException {


        File file = new File(fileName);
        BufferedOutputStream fileOutput = new BufferedOutputStream(new FileOutputStream(file));

        for(byte[] bytee : bytes){
            fileOutput.write(bytee);
        }
        fileOutput.close();

    }
}
