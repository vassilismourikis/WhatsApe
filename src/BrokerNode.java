import org.apache.tika.exception.TikaException;
import org.xml.sax.SAXException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.Executors;

public class BrokerNode{

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
            byte[] chunks = new byte[0];
            Object obj = null;
            int counter = 0;
            String channel=null;
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
                for (ObjectOutputStream writer : writers) {
                    writer.writeObject(new TextValue("server", "MESSAGE " + name + " has joined"));
                }
                writers.add(out);
                chunks = null;
                // Accept messages from this client and broadcast them.
                while (true) {
                    boolean mustPass=false;
                    obj = in.readObject();
                    Value incomingObject=null;
                    try {
                        incomingObject = (Value) obj;
                    }catch (ClassCastException ce) {
                        mustPass=true;
                        if(obj!=null) {
                            try {
                                out.writeObject(new TextValue("server","Recieving video chunks"));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            chunks[counter++] = (byte) obj;

                        }
                        else{
                            try {
                                writeBytesToFile("video.mp4", chunks);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            try {
                                var hist = channelHistory.get(channel);
                                if (hist != null) {
                                    hist.add(new MultimediaValue(channel,new MultimediaFile("video.mp4",name)));
                                    channelHistory.put(channel, hist);
                                } else {
                                    channelHistory.put(channel, new ArrayList<Value>(Arrays.asList(new MultimediaValue(channel,new MultimediaFile("video.mp4",name)))));
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (TikaException e) {
                                e.printStackTrace();
                            } catch (SAXException e) {
                                e.printStackTrace();
                            }
                            counter=0;
                            chunks=null;

                        }

                    }
                    if(mustPass) continue;
                    String input = ((TextValue) incomingObject).getMessage();
                    if (channel != null) {
                        List<Value> history = channelHistory.get(channel);
                        history.add(incomingObject);
                    }
                    if (input.toLowerCase().startsWith("/quit")) { //disconnect
                        return;
                    } else if (input.startsWith("/channel")) { //user picks channel to send message, broker checks if he is registered and initialises the channel var to know where to keep incoming messages as history
                        if (channelSubs.get(input.substring(8)) != null) {
                            if (channelSubs.get(input.substring(8)).contains(name)) channel = input.substring(8);
                        } else {
                            var subs = channelSubs.get(input.substring(9));
                            if (subs != null) {
                                subs.add(name);
                                channelSubs.put(input.substring(9), subs);
                                channel = input.substring(8);
                            } else {
                                channelHistory.put(input.substring(9), new ArrayList<Value>(Arrays.asList(incomingObject)));
                                channelSubs.put(input.substring(9), new ArrayList<String>(Arrays.asList(name)));
                                out.writeObject(new TextValue("server", "channel doesn't exist, just created"));
                            }
                        }
                    } else if (input.startsWith("/register")) {                        //Registers consumer to a channel
                        var subs = channelSubs.get(input.substring(10));
                        if (subs != null) {
                            subs.add(name);
                            channelSubs.put(input.substring(10), subs);
                        } else {
                            channelHistory.put(input.substring(10), new ArrayList<Value>(Arrays.asList(incomingObject)));
                            channelSubs.put(input.substring(10), new ArrayList<String>(Arrays.asList(name)));
                            out.writeObject(new TextValue("server", "channel doesn't exist, just created"));
                        }
                    } else if (input.startsWith("/unregister")) {//Unregisters consumer from a channel
                        var subs = channelSubs.get(input.substring(9));
                        if (subs != null) {
                            subs.remove(name);
                            channelSubs.put(input.substring(12), subs);
                        } else {
                            out.writeObject(new TextValue("server", "not registered to this channel"));
                        }
                    } else if (input.startsWith("LENGTH")) {
                        chunks = new byte[Integer.parseInt(input.substring(7))];
                    }
                    else if (input.startsWith("VIDEOCHANNEL")) {
                        channel=input.substring(12);
                    }
                    for (ObjectOutputStream writer : writers) {
                        //TODO: THREAD
                        writer.writeObject(new TextValue("server", "MESSAGE " + name + ": " + input));
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
        private static void writeBytesToFile(String fileOutput, byte[] bytes)
                throws IOException {

            try (FileOutputStream fos = new FileOutputStream(fileOutput)) {
                fos.write(bytes);
            }

        }
    }

}
