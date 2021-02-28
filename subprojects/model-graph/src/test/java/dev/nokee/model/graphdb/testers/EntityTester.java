package dev.nokee.model.graphdb.testers;

import dev.nokee.model.graphdb.Entity;
import dev.nokee.model.graphdb.NotFoundException;
import lombok.val;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

public interface EntityTester {
	Entity createEntity();

	default Entity createEntityWithProperties() {
		val entity = createEntity();
		entity.setProperty("boolean", true);
		entity.setProperty("string", "ABCdef");
		entity.setProperty("long", Long.MAX_VALUE);
		entity.setProperty("array", new short[] { 1, 2, 3});
		return entity;
	}

	@Test
	default void newEntityHasNoProperties() {
		assertThat(createEntity().getAllProperties(), anEmptyMap());
	}

	@Test
	default void hasDifferentIdsForEachNewEntity() {
		assertThat(createEntity().getId(), not(equalTo(createEntity().getId())));
	}

	//region Property Access
	@Test
	default void canGetExistingProperty() {
		val subject = createEntityWithProperties();
		assertThat(subject.getProperty("boolean"), is(true));
		assertThat(subject.getProperty("string"), equalTo("ABCdef"));
		assertThat(subject.getProperty("long"), is(Long.MAX_VALUE));
		assertThat(subject.getProperty("array"), is(new short[] { 1, 2, 3 }));
	}

	@Test
	default void throwsNotFoundExceptionForUnknownProperties() {
		assertThrows(NotFoundException.class, () -> createEntityWithProperties().getProperty("missing"));
	}

	@Test
	default void returnDefaultValueForUnknownProperties() {
		assertThat(createEntityWithProperties().getProperty("missing", "default"),
			equalTo("default"));
	}

	@Test
	default void returnExistingValueForKnownProperties() {
		assertThat(createEntityWithProperties().getProperty("string", "default"),
			equalTo("ABCdef"));
	}

	@Test
	default void canAccessAllProperties() {
		val subject = createEntityWithProperties();
		assertThat(subject.getAllProperties(), aMapWithSize(4));
		assertThat(subject.getAllProperties(), hasEntry("boolean", true));
		assertThat(subject.getAllProperties(), hasEntry("string", "ABCdef"));
		assertThat(subject.getAllProperties(), hasEntry("long", Long.MAX_VALUE));
		assertThat(subject.getAllProperties(), hasEntry("array", new short[] { 1, 2, 3 }));
	}

	@Test
	default void throwsExceptionWhenPropertyKeyIsNullDuringAccess() {
		val subject = createEntityWithProperties();
		assertThrows(NullPointerException.class, () -> subject.getProperty(null));
		assertThrows(NullPointerException.class, () -> subject.getProperty(null, "default"));
	}

	@Test
	default void canCheckIfPropertyExists() {
		val subject = createEntityWithProperties();
		assertThat(subject.hasProperty("string"), is(true));
		assertThat(subject.hasProperty("missing"), is(false));
	}

	@Test
	default void doesNotMutatePropertyEntityWhenMutatingAllPropertiesReturnValue() {
		val subject = createEntityWithProperties();
		subject.getAllProperties().put("new", "42");
		assertThat(subject.hasProperty("new"), is(false));
		assertThat(subject.getAllProperties(), not(hasEntry("new", "42")));
	}
	//endregion

	//region Property Mutation
	@Test
	default void canSetNewProperty() {
		val subject = createEntityWithProperties();
		subject.setProperty("new", 42);
		assertThat(subject.getAllProperties(), hasEntry("new", 42));
	}

	@Test
	default void canSetNewPropertyFluently() {
		val subject = createEntityWithProperties();
		assertThat(subject.property("new", 42), is(subject));
		assertThat(subject.getAllProperties(), hasEntry("new", 42));
	}

	@Test
	default void throwsExceptionWhenPropertyKeyIsNullDuringMutation() {
		val subject = createEntityWithProperties();
		assertThrows(NullPointerException.class, () -> subject.setProperty(null, 42));
		assertThrows(NullPointerException.class, () -> subject.property(null, 42));
	}
	//endregion
}
