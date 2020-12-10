package dev.nokee.model.internal.core;

import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.internal.type.ModelType;
import lombok.EqualsAndHashCode;

import static java.util.Objects.requireNonNull;

/**
 * Represent a specific projection of a model node.
 *
 * @param <T>  the projection type
 */
// TODO: This may not be needed anymore...
@EqualsAndHashCode
public final class ModelIdentifier<T> implements DomainObjectIdentifier {
    private final ModelPath path;
    private final ModelType<T> type;

    private ModelIdentifier(ModelPath path, ModelType<T> type) {
        this.path = requireNonNull(path);
        this.type = requireNonNull(type);
    }

    public static <T> ModelIdentifier<T> of(String path, Class<T> type) {
        return new ModelIdentifier<>(ModelPath.path(path), ModelType.of(type));
    }

    public static <T> ModelIdentifier<T> of(ModelPath path, ModelType<T> type) {
        return new ModelIdentifier<>(path, type);
    }

    public ModelPath getPath() {
        return path;
    }

    public ModelType<T> getType() {
        return type;
    }
}
