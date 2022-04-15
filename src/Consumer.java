public interface Consumer extends Node{

    /*
        This function unregisters the user from a channel
    */
    public void disconnect(String str);
    /*
        This function registers the user to a channel
    */
    public void register(String topic);

    /*
            This function shows the data of a channel
    */
    public void showConversationData(String topic);
}
