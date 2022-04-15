import java.util.ArrayList;

public class UserNode implements Consumer,Publisher {
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
