package dev.nokee.model.internal.registry;

import com.google.common.testing.EqualsTester;
import dev.nokee.model.internal.core.ModelProjection;
import dev.nokee.model.internal.type.ModelType;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static dev.nokee.model.internal.type.ModelType.of;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MemoizedModelProjectionTest {
	private static final ModelType<MyType> TYPE = ModelType.of(MyType.class);
	private final ModelProjection delegate = Mockito.mock(ModelProjection.class);
	private final ModelProjection subject = new MemoizedModelProjection(delegate);

	@BeforeEach
	void configuresDelegateDefaults() {
		when(delegate.canBeViewedAs(TYPE)).thenReturn(true);
		when(delegate.canBeViewedAs(of(WrongType.class))).thenReturn(false);
		when(delegate.get(TYPE)).thenAnswer(invocation -> new MyType());
	}

	@Test
	void queryDelegateForTypeCompatibility() {
		subject.canBeViewedAs(TYPE);
		verify(delegate, times(1)).canBeViewedAs(TYPE);
	}

	@Test
	void returnsTypeCompatibilityQueryFromDelegate() {
		assertTrue(subject.canBeViewedAs(TYPE), "should return delegate value");
		assertFalse(subject.canBeViewedAs(of(WrongType.class)), "should return delegate value");
	}

	@Test
	void keepsDelegatingAfterMemoized() {
		memoized(subject).canBeViewedAs(TYPE);
		verify(delegate, times(1)).canBeViewedAs(TYPE);
	}

	@Test
	void delegatesTypeDescriptions() {
		subject.getTypeDescriptions();
		Mockito.verify(delegate, times(1)).getTypeDescriptions();
	}

	@Test
	void valueMemoizedOnFirstCall() {
		subject.get(TYPE);
		verify(delegate, times(1)).get(TYPE);
		reset(delegate);

		subject.get(TYPE);
		verify(delegate, never()).get(TYPE);
	}

	@Test
	void returnSameValueForCompatibleType() {
		assertEquals(subject.get(TYPE), subject.get(of(BaseType.class)));
	}

	@Test
	void alwaysReturnTheSameValue() {
		assertEquals(subject.get(TYPE), subject.get(TYPE));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		val projection = mock(ModelProjection.class);
		new EqualsTester()
			.addEqualityGroup(new MemoizedModelProjection(projection), new MemoizedModelProjection(projection))
			.addEqualityGroup(new MemoizedModelProjection(mock(ModelProjection.class)))
			.testEquals();
	}

	private static ModelProjection memoized(ModelProjection projection) {
		projection.get(TYPE);
		return projection;
	}

	interface BaseType {}
	static class MyType implements BaseType {}
	interface WrongType {}
}
