package dev.nokee.model.internal.core;

import dev.nokee.internal.testing.utils.TestUtils;
import dev.nokee.model.internal.type.ModelType;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mockito;

import java.util.Arrays;

import static dev.nokee.model.internal.core.ModelPath.path;
import static dev.nokee.model.internal.core.ModelTestUtils.node;
import static dev.nokee.model.internal.core.ModelTestUtils.projectionOf;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ModelNodeTest {
	private static final ModelType<MyType> TYPE = ModelType.of(MyType.class);
	private static final ModelType<WrongType> WRONG_TYPE = ModelType.of(WrongType.class);
	private final ModelProjection projection1 = Mockito.mock(ModelProjection.class);
	private final ModelProjection projection2 = Mockito.mock(ModelProjection.class);
	private final ModelProjection projection3 = Mockito.mock(ModelProjection.class);
	private final ModelNode subject = new ModelNode(path("po.ta.to"), Arrays.asList(projection1, projection2, projection3));

	@ParameterizedTest
	@EnumSource(Get.class)
	void returnFirstProjectionMatchingType(GetMethod get) {
		val expectedInstance = TestUtils.objectFactory().newInstance(MyType.class);
		when(projection2.canBeViewedAs(TYPE)).thenReturn(true);
		when(projection2.get(TYPE)).thenReturn(expectedInstance);

		val actualInstance = get.invoke(subject, MyType.class);

		assertEquals(expectedInstance, actualInstance);
		verify(projection1, never()).get(any());
		verify(projection2, times(1)).get(TYPE);
		verify(projection3, never()).get(any());
	}

	interface GetMethod {
		<T> T invoke(ModelNode target, Class<T> type);
	}
	enum Get implements GetMethod {
		GET_USING_MODEL_TYPE() {
			@Override
			public <T> T invoke(ModelNode target, Class<T> type) {
				return target.get(ModelType.of(type));
			}
		},
		GET_USING_CLASS() {
			@Override
			public <T> T invoke(ModelNode target, Class<T> type) {
				return target.get(type);
			}
		};
	}

	@Test
	void canQueryModelNodePath() {
		assertEquals(path("po.ta.to"), node("po.ta.to").getPath());
	}

	@Test
	void canCheckProjectedTypeCompatibility() {
		assertTrue(node(projectionOf(MyType.class)).canBeViewedAs(TYPE));
		assertFalse(node(projectionOf(MyType.class)).canBeViewedAs(WRONG_TYPE));
	}

	@Test
	void stateOfNewlyCreatedNodeIsInitialized() {
		assertEquals(ModelNode.State.Initialized, node().getState());
	}

	interface MyType {}
	interface WrongType {}
}
