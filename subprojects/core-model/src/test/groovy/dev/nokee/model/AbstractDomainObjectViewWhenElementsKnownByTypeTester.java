package dev.nokee.model;

import lombok.val;
import org.gradle.internal.Cast;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

public abstract class AbstractDomainObjectViewWhenElementsKnownByTypeTester<T> extends AbstractDomainObjectViewConfigureEachTester<T> {
	private final DomainObjectView<T> subject = createSubject();

	protected abstract <S extends T> void whenElementKnown(DomainObjectView<T> subject, Class<S> type, Consumer<? super KnownDomainObject<S>> action);

	private <S extends T> ArgumentCaptor<KnownDomainObject<S>> whenElementKnown(DomainObjectView<T> subject, Class<S> type) {
		Consumer<KnownDomainObject<S>> action = Cast.uncheckedCast(mock(Consumer.class));
		ArgumentCaptor<KnownDomainObject<S>> captor = Cast.uncheckedCast(ArgumentCaptor.forClass(type));
		doNothing().when(action).accept(captor.capture());

		whenElementKnown(subject, type, action);
		return captor;
	}

	@Test
	void callsWhenElementKnowByType() {
		val captor = when(() -> whenElementKnown(subject, getSubElementType()));
		assertThat("calls back for realized and unrealized view elements matching a type, e.g. e2, e3, e6, e7",
			captor.getAllValues(),
			contains(
				known("e2", getSubElementType()), known("e3", getSubElementType()),
				known("e6", getSubElementType()), known("e7", getSubElementType())));
	}
}
