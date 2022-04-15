import java.util.ArrayList;
import java.util.HashMap;

public class ProfileName {

    private String profileName;
    private HashMap<String, ArrayList<Value>> userVideoFilesMap;
    private HashMap<String,Integer> subscribedConversations;

    public ProfileName(String profileName, HashMap<String, ArrayList<Value>> userVideoFilesMap, HashMap<String, Integer> subscribedConversations) {
        this.profileName = profileName;
        this.userVideoFilesMap = userVideoFilesMap;
        this.subscribedConversations = subscribedConversations;
    }

}
