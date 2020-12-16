package dev.nokee.model.internal.registry;

import dev.nokee.internal.reflect.Instantiator;
import dev.nokee.model.internal.core.ModelProjection;
import dev.nokee.model.internal.core.ModelProjections;
import dev.nokee.model.internal.core.TypeCompatibilityModelProjectionSupport;
import dev.nokee.model.internal.type.ModelType;
import lombok.EqualsAndHashCode;
import lombok.val;
import org.gradle.api.model.ObjectFactory;

/**
 * Represent a managed model projection.
 * A managed projection needs to be bind with an instantiator before it can be used.
 *
 * @param <M>  the managed type of the model projection
 */
@EqualsAndHashCode(callSuper = true)
public final class ManagedModelProjection<M> extends TypeCompatibilityModelProjectionSupport<M> {
	private final Object[] parameters;

	public ManagedModelProjection(ModelType<M> type, Object... parameters) {
		super(type);
		this.parameters = parameters;
	}

	@Override
	public <T> T get(ModelType<T> type) {
		throw new UnsupportedOperationException("This projection is a placeholder and require to be bind with an instantiator.");
	}

	@Override
	public String toString() {
		val builder = new StringBuilder();
		builder.append("ModelProjections.managed(").append(getType());
		for (Object parameter : parameters) {
			builder.append(", ").append(parameter);
		}
		builder.append(")");
		return builder.toString();
	}

	/**
	 * Returned a model projection bounded to the Gradle {@link ObjectFactory} instantiator.
	 * The model object will use the instantitator to create an instance.
	 * The returned instance is memoized.
	 *
	 * @param instantiator  the instantiator to use when creating an instance
	 * @return a model projection bounded to the specified instantiator, never null.
	 */
	public ModelProjection bind(Instantiator instantiator) {
		return ModelProjections.createdUsing(getType(), () -> instantiator.newInstance(getType().getConcreteType(), parameters));
	}
}
