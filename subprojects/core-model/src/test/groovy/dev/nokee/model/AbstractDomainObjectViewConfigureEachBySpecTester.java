package dev.nokee.model;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

public abstract class AbstractDomainObjectViewConfigureEachBySpecTester<T> extends AbstractDomainObjectViewConfigureEachTester<T> {
	private final DomainObjectView<T> subject = createSubject();

	// TODO: configureEach(Spec, Action)

	protected abstract void configureEach(DomainObjectView<T> subject, Predicate<? super T> predicate, Consumer<? super T> action);

	private ArgumentCaptor<T> configureEach(DomainObjectView<T> subject, Predicate<? super T> predicate) {
		@SuppressWarnings("unchecked")
		val action = (Consumer<T>) mock(Consumer.class);
		val captor = ArgumentCaptor.forClass(getElementType());
		doNothing().when(action).accept(captor.capture());

		configureEach(subject, predicate, action);
		return captor;
	}

	@Test
	void callsConfigureEachBySpec() {
		val captor = when(() -> configureEach(subject, t -> t.equals(e("e1"))));
		assertThat("can configure elements matching a spec, e.g. e1",
			captor.getAllValues(), contains(e("e1")));
	}
}
