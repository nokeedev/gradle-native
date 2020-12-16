package dev.nokee.model;

import lombok.val;
import org.gradle.api.InvalidUserDataException;
import org.gradle.internal.Cast;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.mockito.ArgumentCaptor;

import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

public abstract class AbstractNamedDomainObjectViewConfigureByNameTypedTester<T> extends AbstractNamedDomainObjectViewTester<T> {
	private final NamedDomainObjectView<T> subject = createSubject();

	protected abstract <S extends T> void configure(NamedDomainObjectView<T> subject, String name, Class<S> type, Consumer<? super S> action);

	private <S extends T> ArgumentCaptor<S> configure(NamedDomainObjectView<T> subject, String name, Class<S> type) {
		Consumer<S> action = Cast.uncheckedCast(mock(Consumer.class));
		val captor = ArgumentCaptor.forClass(type);
		doNothing().when(action).accept(captor.capture());

		configure(subject, name, type, action);
		return captor;
	}

	@Test
	void doesNotCallConfigureActionWhenNotRealized() {
		element("e0", getSubElementType());
		val captor = configure(subject, "e0", getSubElementType());
		assertThat(captor.getAllValues(), empty());
	}

	@Test
	void callConfigureActionForSameType() {
		element("e1", getSubElementType()).get();
		val captor = configure(subject, "e1", getSubElementType());
		assertThat(captor.getAllValues(), contains(e("e1")));
	}

	@Test
	@DisabledIf("isTestingContainerType")
	void canConfigureKnownElementByBaseType() {
		element("e2", getSubElementType()).get();
		val captor = configure(subject, "e2", getElementType());
		assertThat(captor.getAllValues(), contains(e("e2")));
	}

	@Test
	void throwsExceptionWhenConfiguringKnownElementOfSubtypeUnrelatedType() {
		element("e3", getElementType());
		val ex = assertThrows(InvalidUserDataException.class, () -> configure(subject, "e3", getSubElementType()));
		assertThat(ex.getMessage(), equalTo("The domain object 'e3' (interface dev.nokee.model.CustomDomainObjectContainerTypeIntegrationTest$MyType) is not a subclass of the given type (dev.nokee.model.CustomDomainObjectContainerTypeIntegrationTest.MyChildType)."));
	}

//	@Test
//	void throwsExceptionWhenConfiguringWrongType() {
//		element("e4", WrongType.class);
//		val ex = assertThrows(InvalidUserDataException.class, () -> configure(subject, "e4", getSubElementType()));
//		assertThat(ex.getMessage(), equalTo("bob"));
//	}
//
//	interface WrongType {}
}
