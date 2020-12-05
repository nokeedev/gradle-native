package dev.nokee.model.internal.type;

import com.google.common.reflect.TypeToken;
import lombok.EqualsAndHashCode;

/**
 * A type token to representing a resolved type.
 *
 * @param <T> the resolved type
 */
@EqualsAndHashCode
@SuppressWarnings("UnstableApiUsage")
public final class ModelType<T> {
    private final TypeToken<T> type;

    private ModelType(TypeToken<T> type) {
        this.type = type;
    }

    public Class<? super T> getRawType() {
        return type.getRawType();
    }

    @SuppressWarnings("unchecked")
    public Class<T> getConcreteType() {
        return (Class<T>) type.getRawType();
    }

    public static <T> ModelType<T> of(Class<T> type) {
        return new ModelType<>(TypeToken.of(type));
    }
}
