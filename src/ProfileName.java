import com.example.whatsape.Value;

import java.util.ArrayList;
import java.util.HashMap;

public class ProfileName {

    private String profileName;
    private HashMap<String, ArrayList<Value>> userVideoFilesMap;
    private HashMap<String,Integer> subscribedConversations;

    public ProfileName(String profileName) {
        this.profileName = profileName;
        this.userVideoFilesMap = userVideoFilesMap;
        this.subscribedConversations = subscribedConversations;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public String getProfileName() {
        return profileName;
    }
}
