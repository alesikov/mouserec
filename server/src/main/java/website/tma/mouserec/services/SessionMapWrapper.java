package website.tma.mouserec.services;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionMapWrapper {

    private final Map<String, UUID> internalStorage = new ConcurrentHashMap<>();

    public boolean containsKey(String key) {
        return internalStorage.containsKey(key);
    }

    public void put(String key, UUID value) {
        internalStorage.put(key, value);
    }

    public UUID get(String key) {
        return internalStorage.get(key);
    }

    public UUID remove(String key) {
        return internalStorage.remove(key);
    }

}
