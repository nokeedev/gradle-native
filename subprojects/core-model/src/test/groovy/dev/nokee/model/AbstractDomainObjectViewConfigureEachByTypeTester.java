package dev.nokee.model;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

public abstract class AbstractDomainObjectViewConfigureEachByTypeTester<T> extends AbstractDomainObjectViewConfigureEachTester<T> {
	private final DomainObjectView<T> subject = createSubject();
	// TODO: configureEach(type, Action)
	// TODO: configureEach(type::isInstance, Action)

	protected abstract <S extends T> void configureEach(DomainObjectView<T> subject, Class<S> type, Consumer<? super S> action);

	private <S extends T> ArgumentCaptor<S> configureEach(DomainObjectView<T> subject, Class<S> type) {
		@SuppressWarnings("unchecked")
		Consumer<S> action = (Consumer<S>) mock(Consumer.class);
		val captor = ArgumentCaptor.forClass(type);
		doNothing().when(action).accept(captor.capture());

		configureEach(subject, type, action);
		return captor;
	}

	@Test
	void callsConfigureEachByType() {
		val captor = when(() -> configureEach(subject, getSubElementType()));
		assertThat("can configure elements matching a type, e.g. e3 and e7",
			captor.getAllValues(), contains(e("e3"), e("e7")));
	}
}
