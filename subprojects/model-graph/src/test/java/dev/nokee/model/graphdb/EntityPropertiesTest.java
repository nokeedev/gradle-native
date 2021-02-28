package dev.nokee.model.graphdb;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.google.common.collect.ImmutableMap.copyOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

class EntityPropertiesTest {
	private static final Object DEFAULT_VALUE = new Object();
	private final EntityProperties subject = createSubject();

	private static EntityProperties createSubject() {
		return EntityProperties.of(42);
	}

	@Test
	void hasEmptyProperties() {
		assertThat(subject, emptyIterable());
	}

	@ParameterizedTest
	@MethodSource("providePropertyValue")
	void canPutAndRetrieveValues(Object value) {
		assertDoesNotThrow(() -> subject.put("key", value));
		assertAll(
			() -> assertThat("has entry", copyOf(subject), hasEntry("key", value)),
			() -> assertThat("can get value", subject.get("key"), is(value)),
			() -> assertThat("can get value", subject.getOrDefault("key", DEFAULT_VALUE), is(value)),
			() -> assertThat("has property", subject.has("key"), is(true))
		);
	}
	private static Stream<Object> providePropertyValue() {
		return Stream.of(true, "ABCdef", Long.MAX_VALUE, new short[] { 1, 2, 3});
	}

	@Test
	void returnsNullWhenPuttingNewValue() {
		assertThat(subject.put("key", "foo"), nullValue());
	}

	@Test
	void returnsPreviousValueWhenPuttingExistingValue() {
		subject.put("key", "foo");
		assertThat(subject.put("key", "newFoo"), is("foo"));
	}

	@Test
	void canGetId() {
		assertAll(
			() -> assertThat(subject.getId(), is(42L)),
			() -> assertThat(EntityProperties.of(52).getId(), is(52L))
		);
	}

	@Test
	void throwsNotFoundExceptionForUnknownProperties() {
		assertThrows(NotFoundException.class, () -> subject.get("missing"));
	}

	@Test
	void returnDefaultValueForUnknownProperties() {
		assertThat(subject.getOrDefault("missing", DEFAULT_VALUE), equalTo(DEFAULT_VALUE));
	}

	@Test
	void throwsExceptionWhenPropertyKeyIsNull() {
		assertAll(
			() -> assertThrows(NullPointerException.class, () -> subject.get(null)),
			() -> assertThrows(NullPointerException.class, () -> subject.has(null)),
			() -> assertThrows(NullPointerException.class, () -> subject.put(null, "value")),
			() -> assertThrows(NullPointerException.class, () -> subject.getOrDefault(null, "default"))
		);
	}

	@Test
	void returnsFalseIfPropertyDoesNotExists() {
		assertThat(subject.has("missing"), is(false));
	}
}
