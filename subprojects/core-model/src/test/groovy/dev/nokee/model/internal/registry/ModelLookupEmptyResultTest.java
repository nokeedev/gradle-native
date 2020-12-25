package dev.nokee.model.internal.registry;

import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@Subject(ModelLookupEmptyResult.class)
class ModelLookupEmptyResultTest {
	private final ModelLookup.Result subject = ModelLookupEmptyResult.INSTANCE;

	@Test
	void returnsAnEmptyListWhenGettingTheValues() {
		assertThat(subject.get(), empty());
	}

	@Test
	void returnsAnEmptyListWhenMappingTheResult() {
		assertThat(subject.map(Function.identity()), empty());
	}

	@Test
	void canCreateEmptyResultUsingFactoryMethod() {
		assertThat(ModelLookup.Result.empty(), equalTo(subject));
	}

	@Test
	void hasNoValuesToIterateOver() {
		assertDoesNotThrow(() -> subject.forEach(t -> { throw new AssertionError("Not expecting this exception"); }));
	}
}
