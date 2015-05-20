package org.objectscape.wilco.util;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Nutzer on 20.05.2015.
 */
public interface CollectorsUtil {

    default <T> List<T> toList(Stream<T> stream) {
        return stream.collect(Collectors.toList());
    }

    default <T> Set<T> toSet(Stream<T> stream) {
        return stream.collect(Collectors.toSet());
    }

}
