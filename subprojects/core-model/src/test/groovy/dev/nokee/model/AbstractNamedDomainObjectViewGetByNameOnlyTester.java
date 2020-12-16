package dev.nokee.model;

import dev.nokee.model.internal.core.ModelIdentifier;
import dev.nokee.model.internal.core.ModelNode;
import groovy.lang.MissingMethodException;
import groovy.lang.MissingPropertyException;
import lombok.val;
import org.gradle.api.InvalidUserDataException;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

public abstract class AbstractNamedDomainObjectViewGetByNameOnlyTester<T> extends AbstractNamedDomainObjectViewTester<T> {
	private final NamedDomainObjectView<T> subject = createSubject();

	protected abstract DomainObjectProvider<T> get(NamedDomainObjectView<T> subject, String name);

	@Test
	void canGetProviderForKnownElement() {
		element("e0");
		val provider = get(subject, "e0");
		assertThat(provider.getIdentifier(), equalTo(ModelIdentifier.of("myTypes.e0", getElementType())));
	}

	@Test
	void canGetProjectionValueFromProvider() {
		element("e1");
		assertThat(get(subject, "e1").get(), equalTo(e("e1")));
	}

	@Test
	void doesNotRealizeNode() {
		element("e2");
		get(subject, "e2");
		assertThat(node("e2").getState(), lessThan(ModelNode.State.Realized));
	}

	@Test
	void canRealizeNodeViaProvider() {
		element("e3");
		get(subject, "e3").get();
		assertThat(node("e3").getState(), equalTo(ModelNode.State.Realized));
	}

	@Test
	void throwsExceptionWhenGettingUnknownElements() {
		try {
			get(subject, "e4");
			fail("Expecting method to throw exception");
		} catch (IllegalArgumentException ex) {
			assertThat(ex.getMessage(), equalTo("Element at 'myTypes.e4' wasn't found."));
		} catch (MissingMethodException ex) {
			assertThat(ex.getMessage(), startsWith("Could not find method e4() for arguments"));
		} catch (MissingPropertyException ex) {
			assertThat(ex.getMessage(), startsWith("Could not get unknown property 'e4' for object of type"));
		}
	}

	@Test
	void throwsExceptionWhenGettingKnownElementOfUnrelatedType() {
		element("e5", WrongType.class);
		val ex = assertThrows(InvalidUserDataException.class, () -> get(subject, "e5"));
		assertThat(ex.getMessage(), equalTo("The domain object 'e5' (interface dev.nokee.model.AbstractNamedDomainObjectViewGetByNameOnlyTester$WrongType) is not a subclass of the given type (dev.nokee.model.CustomDomainObjectContainerTypeIntegrationTest.MyType)."));
	}

	interface WrongType {}
}
