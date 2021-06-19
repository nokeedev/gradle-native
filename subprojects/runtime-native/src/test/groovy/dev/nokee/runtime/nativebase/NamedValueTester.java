package dev.nokee.runtime.nativebase;

import com.google.common.testing.EqualsTester;
import dev.nokee.runtime.core.CoordinateAxis;
import dev.nokee.runtime.core.Coordinates;
import lombok.val;
import org.gradle.api.Named;
import org.gradle.api.attributes.Attribute;
import org.gradle.api.reflect.ObjectInstantiationException;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Stream;

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.createChildProject;
import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.rootProject;
import static dev.nokee.internal.testing.ConfigurationMatchers.attributes;
import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.runtime.base.internal.ProvideAttributes.of;
import static dev.nokee.runtime.nativebase.NamedValueTestUtils.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.io.FileMatchers.aFileNamed;
import static org.junit.jupiter.api.Assertions.*;

public interface NamedValueTester<T extends Named> {
	T createSubject(String name);

	@Test
	default void canCreateNamedValue() {
		val subject = assertDoesNotThrow(() -> createSubject("test"));
		assertThat(subject.getName(), equalTo("test"));
	}

	@Test
	default void throwsExceptionWhenCreatingValueWithNullName() {
		val ex = assertThrows(RuntimeException.class, () -> createSubject(null));
		assertThat("exception thrown depends on creation method (e.g. ObjectFactory or factory method)",
			ex, anyOf(isA(ObjectInstantiationException.class), isA(NullPointerException.class)));
	}

	default void canResolveConfigurationUsingNamedValueAsAttribute(String name) {
		val consumer = rootProject();
		val producer = createChildProject(consumer);
		val attribute = Attribute.of(namedValueTypeUnderTest(this));

		producer.getConfigurations().create("testElements", configuration -> {
			configuration.setCanBeConsumed(true);
			configuration.setCanBeResolved(false);
			configuration.attributes(attributes -> {
				attributes.attribute(attribute, createSubject(name));
			});
			configuration.getOutgoing().artifact(new File("my-file"));
		});
		val test = consumer.getConfigurations().create("test", configuration -> {
			configuration.setCanBeConsumed(false);
			configuration.setCanBeResolved(true);
			configuration.attributes(attributes -> {
				attributes.attribute(attribute, createSubject(name));
			});
		});
		consumer.getDependencies().add(test.getName(), producer);

		assertThat(test.resolve(), contains(aFileNamed(equalTo("my-file"))));
	}

	@TestFactory
	default Stream<DynamicTest> canResolveConfigurationUsingNamedValueAsAttribute() {
		return Stream.concat(Stream.of("test"), knownValues()).map(it -> DynamicTest.dynamicTest("can resolve configuration using named value as attribute [" + it + "]", () -> canResolveConfigurationUsingNamedValueAsAttribute(it)));
	}

	default Stream<String> knownValues() {
		return Stream.empty();
	}

	@Test
	default void canProvideAttribute() {
		val project = rootProject();
		val test = project.getConfigurations().create("test", configuration -> {
			configuration.attributes(of(createSubject("foo")));
		});
		assertThat(test, attributes(hasEntry(equalTo(attributeType(this)), named("foo"))));
	}

	@Test
	default void hasOnlyOneAttributeTypeOnNamedValueClass() {
		val fields = findAllPublicConstantFieldOf(namedValueTypeUnderTest(this));
		assertThat(fields.filter(fieldOf(Attribute.class)).count(), is(1L));
	}

	static <T extends Named> Attribute<T> attributeType(NamedValueTester<T> self) {
		val type = namedValueTypeUnderTest(self);
		val field = Arrays.stream(type.getDeclaredFields()).filter(it -> Attribute.class.isAssignableFrom(it.getType())).findFirst(); // TODO: check only for public one
		try {
			return (Attribute<T>) field.orElseThrow(RuntimeException::new).get(null);
		} catch (IllegalAccessException e) {
			throw new RuntimeException();
		}
	}

	@Test
	default void canProvideCoordinate() {
		val subject = createSubject("bar");
		val coordinate = Coordinates.of(subject);
		assertAll(
			() -> assertThat(coordinate.getValue(), is(subject)),
			() -> assertThat(coordinate.getAxis(), is(coordinateAxisUnderTest(this)))
		);
	}

	@Test
	default void hasOnlyOneCoordinateAxisOnNamedValueClass() {
		val fields = findAllPublicConstantFieldOf(namedValueTypeUnderTest(this));
		assertThat(fields.filter(fieldOf(CoordinateAxis.class)).count(), is(1L));
	}

	@Test
	default void checkToString() {
		assertThat(createSubject("test"), hasToString("test"));
		assertThat(createSubject("my-value"), hasToString("my-value"));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	default void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(createSubject("foo"), createSubject("foo"))
			.addEqualityGroup(createSubject("bar"))
			.testEquals();
	}
}
