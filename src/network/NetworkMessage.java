package network;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class NetworkMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public String command;
    public Map<String, String> data = new HashMap<>();
    public boolean success;
    public String responseBody;
    public Object payload;

    public NetworkMessage(String command) {
        this.command = command;
    }
}