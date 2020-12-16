package dev.nokee.model;

import dev.nokee.model.internal.core.ModelNode;
import lombok.val;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isA;

public abstract class AbstractDomainObjectContainerRegisterTester<T> extends AbstractDomainObjectContainerTester<T> {
	private final DomainObjectContainer<T> subject = createSubject();

	protected abstract DomainObjectProvider<T> register(DomainObjectContainer<T> subject, String name, Class<T> type);

	// TODO: getRegistrableManagedType()
	// TODO: getRegistrableUnmanagedType()
	// TODO: getUnregistrableType() // newspeak

	@Test
	void canRegisterManagedType() {
		val provider = register(subject, "foo", getElementType());
		assertThat(node("foo").getState(), equalTo(ModelNode.State.Registered));
		assertThat(e("foo"), isA(getElementType()));

		// Realize through provider
		assertThat(provider.get(), isA(getElementType()));
		assertThat(node("foo").getState(), equalTo(ModelNode.State.Realized));
	}

	// TODO: Can create element without factory that is creatable via ObjectFactory (managed)
	// TODO: Throws exception if not creatable via ObjectFactory (unmanaged)
	// TODO: Can create element with factory (unmanaged) -> support the other tests as well
	// TODO: Can bind element to a factory (unmanaged) -> support the other tests as well
	// TODO: Can create element that creates a NodeRegistration



	// TODO: Add support for Groovy DSL for registering here
}
