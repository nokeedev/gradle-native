package dev.nokee.model.internal.registry;

import dev.nokee.internal.testing.utils.TestUtils;
import dev.nokee.model.internal.core.ModelAction;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.core.ModelRegistration;
import lombok.val;
import org.gradle.api.provider.Property;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static dev.nokee.model.internal.core.ModelIdentifier.of;
import static dev.nokee.model.internal.core.ModelPath.path;
import static dev.nokee.model.internal.core.ModelPath.root;
import static dev.nokee.model.internal.core.ModelRegistration.bridgedInstance;
import static dev.nokee.model.internal.core.ModelRegistration.unmanagedInstance;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;

public class DefaultModelRegistryIntegrationTest {
	private final DefaultModelRegistry modelRegistry = new DefaultModelRegistry(TestUtils.objectFactory());

	@Test
	void modelNodeRegistrationGivesAccessToProviderThatResolvesToTheNodeValue() {
		val provider = modelRegistry.register(ModelRegistration.of("myType", MyType.class));
		assertAll(() -> {
			assertThat(provider.getIdentifier(), equalTo(of("myType", MyType.class)));
			assertThat(provider.get(), isA(MyType.class));
		});
	}

	@Test
	void canAccessRegisteredModel() {
		assertThrows(IllegalStateException.class, () -> modelRegistry.get("foo", MyType.class));
		val provider = modelRegistry.register(ModelRegistration.of("foo", MyType.class));
		assertEquals(provider, modelRegistry.get("foo", MyType.class));
	}

	// TODO: register unmanaged node
	// TODO: register node instance

	@Test
	void canRegisterBridgedInstanceModel() {
		val instance = new UnmanagedType();
		val provider = modelRegistry.register(bridgedInstance(of("bar", UnmanagedType.class), instance));
		assertEquals(instance, provider.get());
	}

	@Test
	void canRegisterManagedInstanceModel() {
		val provider = modelRegistry.register(unmanagedInstance(of("bar", ManagedType.class), () -> TestUtils.objectFactory().newInstance(ManagedType.class)));
		assertThat(provider.get(), isA(ManagedType.class));
	}

	@Test
	void canRegisterUnmanagedInstanceModel() {
		val provider = modelRegistry.register(unmanagedInstance(of("bar", UnmanagedType.class), UnmanagedType::new));
		assertThat(provider.get(), isA(UnmanagedType.class));
	}

	@Test
	void canAccessModelNodeOnManagedType() {
		val provider = modelRegistry.register(unmanagedInstance(of("a", ModelNodeAccessingType.class), () -> TestUtils.objectFactory().newInstance(ModelNodeAccessingType.class)));
		assertEquals("a", provider.get().getModelPathAsString());
	}
	interface ModelNodeAccessingType {
		default String getModelPathAsString() {
			return ModelNodes.of(this).getPath().get();
		}
	}

	// Values are kept between
	@Test
	void valuesArePersistedOnNodeForManagedType() {
		val provider = modelRegistry.register(ModelRegistration.of("a", ManagedValueType.class));
		provider.get().getValue().set("foo-value");
		assertEquals("foo-value", provider.get().getValue().getOrNull());
	}

	interface ManagedValueType {
		Property<String> getValue();
	}

	@Test
	void valuesArePersistedOnNodeForUnmanagedType() {
		val provider = modelRegistry.register(ModelRegistration.bridgedInstance(of("a", UnmanagedValueType.class), new UnmanagedValueType()));
		provider.get().setValue("foo-value");
		assertEquals("foo-value", provider.get().getValue());
	}
	static final class UnmanagedValueType {
		private String value;

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}

	@Test
	void canConfigureNodesAlreadyRegistered() {
		val action = Mockito.mock(ModelAction.class);

		modelRegistry.register(ModelRegistration.of("a", MyType.class));
		modelRegistry.register(ModelRegistration.of("b", MyType.class));
		modelRegistry.configureMatching(node -> true, action);

		val captor = ArgumentCaptor.forClass(ModelNode.class);
		Mockito.verify(action, times(3)).execute(captor.capture());
		assertThat(captor.getAllValues().stream().map(ModelNode::getPath).collect(toList()), contains(root(), path("a"), path("b")));
	}

	@Test
	void canConfigureFutureNodesRegistered() {
		val action = Mockito.mock(ModelAction.class);

		modelRegistry.configureMatching(node -> true, action);
		modelRegistry.register(ModelRegistration.of("x", MyType.class));
		modelRegistry.register(ModelRegistration.of("y", MyType.class));

		val captor = ArgumentCaptor.forClass(ModelNode.class);
		Mockito.verify(action, times(5)).execute(captor.capture());
		assertThat(captor.getAllValues().stream().map(ModelNode::getPath).collect(toList()), contains(root(), path("x"), path("x"), path("y"), path("y")));
	}

	@Test
	@Disabled("Not implemented")
	void canConfigureNodesOnlyWhenOnSpecificState() {
		val action = Mockito.mock(ModelAction.class);

		modelRegistry.register(ModelRegistration.of("i", MyType.class));
		modelRegistry.configureMatching(node -> true, action);
		modelRegistry.register(ModelRegistration.of("j", MyType.class));

		val captor = ArgumentCaptor.forClass(ModelNode.class);
		Mockito.verify(action, times(3)).execute(captor.capture());
		assertThat(captor.getAllValues().stream().map(ModelNode::getPath).collect(toList()), contains(root(), path("foo"), path("bar")));
	}

//	@Test
//	void canAccessModelNodeOnManagedType() {
//		val provider = modelRegistry.register(unmanagedInstance(of("a", ModelNodeAccessingType.class), () -> TestUtils.objectFactory().newInstance(ModelNodeAccessingType.class)));
//		assertEquals("a", provider.get().getModelPathAsString());
//	}
//


	// NOT SURE...
//	@Test
//	void canRegisterMultipleModel() {
//		val provider1 = modelRegistry.register(ModelRegistration.of("foo", MyType.class));
//		val provider2 = modelRegistry.register(ModelRegistration.of("bar", MyType.class));
//
//	}

	interface MyType {}
	interface ManagedType {}
	static final class UnmanagedType {}
	// TODO: Can register node (with path and type)
	// TODO: Can execute action on the node's state (registered, discovered, created, realized)
	// TODO: Can register node for already created node
	// TODO: Can register node with differed creation
	// TODO: Can realize node (from discovered)
	// TODO: Can realize node (from created)
	// TODO: Can initialize node when registering

	// TODO: Can register sub-node from node (add link)

	interface MyComponentContainer {}
	interface MyComponent {}
}
