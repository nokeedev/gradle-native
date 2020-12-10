package dev.nokee.model.internal.registry;

import dev.nokee.model.internal.core.ModelProjection;
import dev.nokee.model.internal.type.ModelType;
import lombok.EqualsAndHashCode;
import org.gradle.api.model.ObjectFactory;

import java.util.Objects;

/**
 * Represent a managed model projection.
 * A managed projection needs to be bind with an instantiator before it can be used.
 *
 * @param <M>  the managed type of the model projection
 */
@EqualsAndHashCode
public final class ManagedModelProjection<M> implements ModelProjection {
	private final ModelType<M> type;

	private ManagedModelProjection(ModelType<M> type) {
		this.type = Objects.requireNonNull(type);
	}

	public static <T> ManagedModelProjection<T> of(Class<T> type) {
		return new ManagedModelProjection<>(ModelType.of(type));
	}

	public static <T> ManagedModelProjection<T> of(ModelType<T> type) {
		return new ManagedModelProjection<>(type);
	}

	@Override
	public <T> boolean canBeViewedAs(ModelType<T> type) {
		throw new UnsupportedOperationException("This projection is a placeholder and require to be bind with an instantiator.");
	}

	@Override
	public <T> T get(ModelType<T> type) {
		throw new UnsupportedOperationException("This projection is a placeholder and require to be bind with an instantiator.");
	}

	/**
	 * Returned a model projection bounded to the Gradle {@link ObjectFactory} instantiator.
	 * The model object will use the instantitator to create an instance.
	 * The returned instance is memoized.
	 *
	 * @param objectFactory  the instantiator to use when creating an instance
	 * @return a model projection bounded to the specified instantiator, never null.
	 */
	public ModelProjection bind(ObjectFactory objectFactory) {
		return new MemoizedModelProjection(UnmanagedCreatingModelProjection.of(type, () -> objectFactory.newInstance(type.getConcreteType())));
	}
}
