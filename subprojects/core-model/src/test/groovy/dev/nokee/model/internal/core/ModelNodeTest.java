package dev.nokee.model.internal.core;

import dev.nokee.model.internal.type.ModelType;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;

import static dev.nokee.model.internal.core.ModelPath.path;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ModelNodeTest {
	private static final ModelType<MyType> TYPE = ModelType.of(MyType.class);
	private final ModelProjection projection1 = Mockito.mock(ModelProjection.class);
	private final ModelProjection projection2 = Mockito.mock(ModelProjection.class);
	private final ModelProjection projection3 = Mockito.mock(ModelProjection.class);
	private final ModelNode subject = new ModelNode(path("po.ta.to"), Arrays.asList(projection1, projection2, projection3));

	@Test
	void returnFirstProjectionMatchingType() {
		val expectedInstance = new MyType();
		when(projection2.canBeViewedAs(TYPE)).thenReturn(true);
		when(projection2.get(TYPE)).thenReturn(expectedInstance);

		val actualInstance = subject.get(ModelType.of(MyType.class));

		assertEquals(expectedInstance, actualInstance);
		verify(projection1, never()).get(any());
		verify(projection2, times(1)).get(TYPE);
		verify(projection3, never()).get(any());
	}

	@Test
	void canQueryModelNodePath() {
		assertEquals(path("po.ta.to"), subject.getPath());
	}

	static class MyType {}
}
