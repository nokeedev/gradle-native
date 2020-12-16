package dev.nokee.model;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

public abstract class AbstractDomainObjectViewElementTransformTester<T> extends AbstractDomainObjectViewTester<T> {
	private final DomainObjectView<T> subject = createSubject();

	@BeforeEach
	void addElements() {
		elements("e1", "e2", "e3");
	}

	protected abstract Iterable<?> transform(DomainObjectView<T> subject, Function<T, ?> mapper);

	@Test
	void canTransformElements() {
		val transformedElements = transform(subject, T::toString);
		assertThat("all registered elements should be transformed",
			transformedElements, contains(e("e1").toString(), e("e2").toString(), e("e3").toString()));
	}
}
