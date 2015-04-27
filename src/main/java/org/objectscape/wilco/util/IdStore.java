package org.objectscape.wilco.util;

import org.objectscape.wilco.core.DuplicateIdException;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Nutzer on 12.04.2015.
 */
public class IdStore {

    final public Set<String> channelIds = new HashSet<>();
    final public Object channelIdsLock = new Object();

    public long currentId = 0;

    public String generateId() {
        synchronized (channelIdsLock) {
            String nextId = null;
            do {
                nextId = String.valueOf(currentId++);
            } while (channelIds.contains(nextId));
            return nextId;
        }
    }

    public String compareAndSetId(String id) {
        synchronized (channelIdsLock) {
            if(!channelIds.contains(id)) {
                channelIds.add(id);
                return id;
            }
            else {
                throw new DuplicateIdException("id " + id + " already in use by other channel");
            }
        }
    }

}
