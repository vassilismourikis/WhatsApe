public class BrokerNode implements Broker{


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
}
