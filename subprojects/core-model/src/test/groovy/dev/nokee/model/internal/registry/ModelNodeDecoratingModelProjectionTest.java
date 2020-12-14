package dev.nokee.model.internal.registry;

import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelProjection;
import dev.nokee.model.internal.core.ModelTestUtils;
import dev.nokee.model.internal.type.ModelType;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static dev.nokee.internal.testing.utils.TestUtils.objectFactory;
import static dev.nokee.model.internal.core.ModelNodes.inject;
import static dev.nokee.model.internal.core.ModelNodes.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

class ModelNodeDecoratingModelProjectionTest {
	private static final ModelType<MyType> TYPE = ModelType.of(MyType.class);
	private final ModelProjection delegate = Mockito.mock(ModelProjection.class);
	private final ModelNode node = ModelTestUtils.node("x.y.z");
	private final ModelProjection subject = new ModelNodeDecoratingModelProjection(delegate, () -> node);

	@Test
	void canDecorateIfDelegateProjectionIsExtensionAware() {
		when(delegate.get(TYPE)).thenReturn(objectFactory().newInstance(MyType.class));
		assertEquals(node, of(subject.get(TYPE)));
	}

	@Test
	void doesNotThrowExceptionWhenDelegateProjectionIsAlreadyDecorated() {
		val instance = objectFactory().newInstance(MyType.class);
		when(delegate.get(TYPE)).thenReturn(inject(instance, node));
		assertEquals(node, of(subject.get(TYPE)));
	}

	@Test
	void throwsExceptionIfDelegateProjectionIsDecoratedWithAnotherNode() {
		val anotherNode = ModelTestUtils.node("a.b.c");
		val instance = objectFactory().newInstance(MyType.class);
		when(delegate.get(TYPE)).thenReturn(inject(instance, anotherNode));
		assertThrows(IllegalStateException.class, () -> subject.get(TYPE));
	}

	@Test
	void delegatesTypeViewCheck() {
		subject.canBeViewedAs(TYPE);
		Mockito.verify(delegate, times(1)).canBeViewedAs(TYPE);
	}

	@Test
	void delegatesTypeDescriptions() {
		subject.getTypeDescriptions();
		Mockito.verify(delegate, times(1)).getTypeDescriptions();
	}

	interface MyType {}
}
