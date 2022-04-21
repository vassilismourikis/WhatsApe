import java.util.ArrayList;

public interface Publisher extends Node{

    /*
        This function splits the video into chunks
    */
    public ArrayList<Value> generateChunks(MultimediaFile file);

    /*
        This function returns all the brokers that exists
    */
    public ArrayList<Broker> getBrokerList();

    /*
    This function generates the MD5 hash code from specified string(name+topic)
     */
    public Broker hashTopic(String topic);

    /*
    This function notifies the brokers that there is a new message
    for sending to the registered consumers
     */
    public void notifyBrokersNewMessage(String message);

    /*
    TODO:
     */
    public void notifyFailure(Broker broker);

    public void push(String top, Value value);
}
