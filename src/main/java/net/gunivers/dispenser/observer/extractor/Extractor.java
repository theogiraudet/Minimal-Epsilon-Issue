package net.gunivers.dispenser.observer.extractor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

public interface Extractor<T> {

    Collection<T> extractFrom(Path location) throws IOException;

}
