package dev.nokee.model.core;

import lombok.val;
import org.gradle.api.specs.Spec;
import org.mockito.Mockito;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;

final class ModelNodeTestUtils {
	private ModelNodeTestUtils() {}

	public static ModelNode rootNode() {
		val result = Mockito.mock(ModelNode.class);
		Mockito.when(result.getParent()).thenReturn(Optional.empty());
		return result;
	}

	public static ModelNode childNodeOf(ModelNode node) {
		val result = Mockito.mock(ModelNode.class);
		Mockito.when(result.getParent()).thenReturn(Optional.of(node));
		return result;
	}

	public static ModelProjection projectionOf(ModelNode node) {
		val result = Mockito.mock(ModelProjection.class);
		Mockito.when(result.getOwner()).thenReturn(node);
		Mockito.when(result.canBeViewedAs(any())).thenReturn(true);
		return result;
	}

	public static <T> ModelSpec<T> specOf(Class<T> type) {
		return new ModelSpec<T>() {
			@Override
			public boolean isSatisfiedBy(ModelProjection node) {
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
			public boolean isSatisfiedBy(ModelProjection node) {
				return spec.isSatisfiedBy(node);
			}

			@Override
			public Class<T> getProjectionType() {
				return type;
			}
		};
	}
}
