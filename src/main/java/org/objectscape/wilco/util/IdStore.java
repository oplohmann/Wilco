package org.objectscape.wilco.util;

import org.objectscape.wilco.core.DuplicateIdException;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Nutzer on 12.04.2015.
 */
public class IdStore {

    final private Set<String> channelIds = new ConcurrentSkipListSet<>();
    final private AtomicLong currentId = new AtomicLong(0);

    public String generateId() {
            String nextId = null;
            do {
                nextId = String.valueOf(currentId.getAndIncrement());
            } while (channelIds.contains(nextId));
            return nextId;
    }

    public String compareAndSetId(String id) {
        if(id == null) {
            throw new NullPointerException("queue id null");
        }
        if(!channelIds.add(id)) {
            throw new DuplicateIdException("id " + id + " already in use by other channel");
        };
        return id;
    }

    public boolean freeId(String id) {
        return channelIds.remove(id);
    }

}
