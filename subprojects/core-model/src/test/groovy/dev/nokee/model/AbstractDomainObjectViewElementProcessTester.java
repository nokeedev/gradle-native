package dev.nokee.model;

import lombok.val;
import org.gradle.api.provider.Provider;
import org.gradle.internal.Cast;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;

public abstract class AbstractDomainObjectViewElementProcessTester<T> extends AbstractDomainObjectViewTester<T> {
	private final DomainObjectView<T> subject = createSubject();

	protected abstract Provider<? extends Iterable<?>> process(DomainObjectView<T> subject, Consumer<? super T> captor);

	private static <T> Consumer<T> noProcessingOfElements() {
		return t -> { throw new UnsupportedOperationException(); };
	}

	private ArgumentCaptor<T> capturesAllElements(Consumer<Consumer<? super T>> action) {
		Consumer<T> capturingAction = Cast.uncheckedCast(mock(Consumer.class));
		val captor = ArgumentCaptor.forClass(getElementType());
		Mockito.doNothing().when(capturingAction).accept(captor.capture());
		action.accept(capturingAction);
		return captor;
	}

	@BeforeEach
	void addElements() {
		elements("e1", "e2");
	}

	// Processing of element assertion
	@Test
	void doesNotEagerlyProcessElements() {
		process(subject, noProcessingOfElements());
	}

	@Test
	void callsTransformationForWithEachElements() {
		val captor = capturesAllElements(action -> {
			process(subject, action).get();
		});
		assertThat("invoke mapper transformer with each registered elements",
			captor.getAllValues(), contains(e("e1"), e("e2")));
	}

	@Test
	void wrongElementTypesAreNotTransformed() {
		element("foo", WrongType.class);
		val captor = capturesAllElements(action -> {
			process(subject, action).get();
		});
		assertThat("unscoped elements should not be mapped",
			(Iterable<Object>)captor.getAllValues(), not(hasItem(e("foo")))); // TODO: Remove cast
	}

	interface WrongType {}
}
