package dev.nokee.model.internal.core;

import com.google.common.base.Splitter;
import lombok.EqualsAndHashCode;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * A path of a model node.
 */
@EqualsAndHashCode
public final class ModelPath {
    private static final char PATH_SEPARATOR = '.';
    private static final int PATH_SEPARATOR_LENGTH = 1;
    private static final Splitter PATH_SPLITTER = Splitter.on(PATH_SEPARATOR).omitEmptyStrings();
    private static final ModelPath ROOT = new ModelPath("", new String[0]);
    @EqualsAndHashCode.Exclude private final String path;
    private final String[] components;

    @Nullable
    @EqualsAndHashCode.Exclude
    private final ModelPath parent;

    private ModelPath(String path, String[] components) {
        this.path = path;
        this.components = components;
        this.parent = doGetParent();
    }

    public static ModelPath root() {
        return ROOT;
    }

    public static ModelPath path(String path) {
        return new ModelPath(path, splitPath(path));
    }

    public String get() {
        return path;
    }

    @Override
    public String toString() {
        if (components.length == 0) {
            return "<root>";
        }
        return path;
    }

    public Optional<ModelPath> getParent() {
        return Optional.ofNullable(parent);
    }

    public boolean isDirectDescendant(ModelPath other) {
        Objects.requireNonNull(other);

        if (other.components.length != components.length + 1) {
            return false;
        }

        return other.getParent().map(this::equals).orElse(false);
    }

    private static String[] splitPath(String path) {
        return PATH_SPLITTER.splitToStream(path).toArray(String[]::new);
    }

    private ModelPath doGetParent() {
        if (components.length == 0) {
            return null;
        }

        if (components.length == 1) {
            return ROOT;
        }

        return new ModelPath(dropRight(path, nameOf(components).length() + PATH_SEPARATOR_LENGTH), removeLastElement(components));
    }

    private static String[] removeLastElement(String[] components) {
        return Arrays.copyOf(components, components.length - 1);
    }

    private static String dropRight(String self, int count) {
        return self.substring(0, self.length() - count);
    }

    private static String nameOf(String[] components) {
        return components[components.length - 1];
    }
}
