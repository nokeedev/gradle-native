package dev.nokee.model;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

public abstract class AbstractDomainObjectViewElementFilterTester<T> extends AbstractDomainObjectViewTester<T> {
	private final DomainObjectView<T> subject = createSubject();

	@BeforeEach
	void addElements() {
		elements("e1", "e2", "e3");
	}

	protected abstract Iterable<?> filter(DomainObjectView<T> subject, Predicate<T> predicate);

	@Test
	void canFilterElements() {
		val filteredElements = filter(subject, t -> t.toString().equals(e("e1").toString()));
		assertThat("only e1 should be returned", filteredElements, contains(e("e1")));
	}

	// TODO: Filter by type in another tester
	//   -> flatMap -> is type >> return list of element else empty list
	//   -> filter -> is type
	//   -> withType(type).get() && withType(type).getElements().get() && withType(type).map(noOp).get() && withType(type).flatMap(noOp).get() && withTYpe(type).filter(alwaysTrue()).get()
}
