package dev.nokee.model.dsl;

import dev.nokee.model.core.ModelNode;
import dev.nokee.model.core.ModelProjection;
import lombok.val;
import org.gradle.api.specs.Spec;
import org.gradle.internal.Cast;
import org.mockito.Mockito;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;

@Deprecated
final class ModelNodeTestUtils {
	private ModelNodeTestUtils() {}

	public static dev.nokee.model.core.ModelNode rootNode() {
		val result = Mockito.mock(dev.nokee.model.core.ModelNode.class);
		Mockito.when(result.getParent()).thenReturn(Optional.empty());
		return result;
	}

	public static dev.nokee.model.core.ModelNode childNodeOf(dev.nokee.model.core.ModelNode node) {
		val result = Mockito.mock(dev.nokee.model.core.ModelNode.class);
		Mockito.when(result.getParent()).thenReturn(Optional.of(node));
		return result;
	}

	public static ModelProjection projectionOf(ModelNode node) {
		val result = Mockito.mock(ModelProjection.class);
		Mockito.when(result.getOwner()).thenReturn(node);
		Mockito.when(result.canBeViewedAs(any())).thenReturn(true);
		Mockito.when(result.getType()).thenReturn(Cast.<Class>uncheckedCast(Object.class));
		return result;
	}

	public static <T> ModelSpec<T> specOf(Class<T> type) {
		return new ModelSpec<T>() {
			@Override
			public boolean test(ModelProjection node) {
				return true;
			}

			@Override
			public Class<T> getProjectionType() {
				return type;
			}

			@Override
			public String toString() {
				return "test spec for '" + type.getCanonicalName() + "'";
			}
		};
	}

	public static <T> ModelSpec<T> specOf(Class<T> type, Spec<? super ModelProjection> spec) {
		return new ModelSpec<T>() {
			@Override
			public boolean test(ModelProjection node) {
				return spec.isSatisfiedBy(node);
			}

			@Override
			public Class<T> getProjectionType() {
				return type;
			}
		};
	}
}
