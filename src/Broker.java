public interface Broker extends Node{

    public Consumer acceptConnection(Consumer consumer);

    public Publisher acceptConnection(Publisher publisher);

    public void calculateKeys();

    public void filterConsumers(String con);

    public void notifyBrokersOnChanges();

    public void notifyPublisher(String pub);

    public void pull(String top);

}