package dev.nokee.model.internal.registry;

import com.google.common.collect.ImmutableList;
import dev.nokee.internal.testing.utils.TestUtils;
import dev.nokee.model.internal.core.*;
import lombok.Value;
import lombok.val;
import org.gradle.api.provider.Property;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static com.google.common.base.Predicates.alwaysTrue;
import static dev.nokee.model.internal.core.ModelActions.*;
import static dev.nokee.model.internal.core.ModelIdentifier.of;
import static dev.nokee.model.internal.core.ModelNode.State.Realized;
import static dev.nokee.model.internal.core.ModelNode.State.Registered;
import static dev.nokee.model.internal.core.ModelNodes.stateAtLeast;
import static dev.nokee.model.internal.core.ModelNodes.stateOf;
import static dev.nokee.model.internal.core.ModelPath.path;
import static dev.nokee.model.internal.core.ModelPath.root;
import static dev.nokee.model.internal.core.ModelRegistration.bridgedInstance;
import static dev.nokee.model.internal.core.ModelRegistration.unmanagedInstance;
import static dev.nokee.model.internal.core.ModelSpecs.satisfyAll;
import static dev.nokee.model.internal.core.ModelTestActions.doSomething;
import static dev.nokee.model.internal.core.NodePredicate.allDirectDescendants;
import static dev.nokee.model.internal.registry.DefaultModelRegistryIntegrationTest.MyComponent.aComponent;
import static dev.nokee.model.internal.registry.DefaultModelRegistryIntegrationTest.NodeStateTransitionCollectingAction.*;
import static dev.nokee.model.internal.type.ModelType.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

public class DefaultModelRegistryIntegrationTest {
	private final DefaultModelRegistry modelRegistry = new DefaultModelRegistry(TestUtils.objectFactory()::newInstance);

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
		val action = new NodeStateTransitionCollectingAction();

		modelRegistry.register(ModelRegistration.of("a", MyType.class));
		modelRegistry.register(ModelRegistration.of("b", MyType.class));
		modelRegistry.configureMatching(satisfyAll(), action);

		assertThat(action.values,
			contains(registered(root()), registered("a"), registered("b")));
	}

	@Test
	void canConfigureFutureNodesRegistered() {
		val action = new NodeStateTransitionCollectingAction();

		modelRegistry.configureMatching(satisfyAll(), action);
		modelRegistry.register(ModelRegistration.of("x", MyType.class));
		modelRegistry.register(ModelRegistration.of("y", MyType.class));

		assertThat(action.values,
			contains(registered(root()), initialized("x"), registered("x"), initialized("y"), registered("y")));
	}

	@Test
	void queryProviderRealizeNodeAndParent() {
		val action = new NodeStateTransitionCollectingAction();

		modelRegistry.configureMatching(satisfyAll(), action);
		modelRegistry.register(ModelRegistration.of("x", MyType.class)).get();

		assertThat(action.values,
			contains(registered(root()), initialized("x"), registered("x"), realized(root()), realized("x")));
	}

	@Test
	void canConfigureNodesOnlyWhenOnSpecificState() {
		val action = new NodeStateTransitionCollectingAction();

		modelRegistry.register(ModelRegistration.of("i", MyType.class));
		modelRegistry.configureMatching(node -> node.isAtLeast(Realized), action);
		modelRegistry.register(ModelRegistration.of("j", MyType.class)).get();

		assertThat(action.values, contains(realized(root()), realized("j")));
	}

	static class NodeStateTransitionCollectingAction implements ModelAction {
		private final List<NodeStateTransition> values = new ArrayList<>();

		@Override
		public void execute(ModelNode node) {
			values.add(new NodeStateTransition(node.getPath(), node.getState()));
		}

		static NodeStateTransition realized(String path) {
			return new NodeStateTransition(ModelPath.path(path), Realized);
		}

		static NodeStateTransition realized(ModelPath path) {
			return new NodeStateTransition(path, Realized);
		}

		static NodeStateTransition registered(String path) {
			return new NodeStateTransition(ModelPath.path(path), Registered);
		}

		static NodeStateTransition registered(ModelPath path) {
			return new NodeStateTransition(path, Registered);
		}

		static NodeStateTransition initialized(String path) {
			return new NodeStateTransition(ModelPath.path(path), ModelNode.State.Initialized);
		}

		@Value
		static class NodeStateTransition {
			ModelPath path;
			ModelNode.State state;
		}
	}

	protected ModelNode registerNode(String path) {
		modelRegistry.register(ModelRegistration.of(path, MyType.class));
		return modelRegistry.get(path(path));
	}

	@Test
	void canGetDirectNodeDescendants() {
		registerNode("a");
		val expectedNodes = ImmutableList.of(
			registerNode("a.b0"),
			registerNode("a.b1"),
			registerNode("a.b2"));
		registerNode("a.b2.c0");
		registerNode("a.b2.c1");

		assertThat("direct descendant of a should be only b*",
			modelRegistry.get(path("a")).getDirectDescendants(),
			contains(expectedNodes.toArray()));
	}

	@Test
	void canRegisterComplexModelSimply() {
		modelRegistry.register(aComponent("main"));
		val paths = ImmutableList.<ModelPath>builder();
		modelRegistry.query(alwaysTrue()::test).map(ModelNode::getPath).forEach(paths::add);
		assertThat(paths.build(), contains(root(), path("main"), path("main.sources"), path("main.sources.foo"), path("main.sources.bar")));
	}

	interface MyComponent {
		static NodeRegistration<MyComponent> aComponent(String name) {
			return NodeRegistration.of(name, of(MyComponent.class))
				.action(stateAtLeast(Registered), once(register(MyComponentSources.componentSources())));
		}
	}
	interface MyComponentSources {
		static NodeRegistration<MyComponentSources> componentSources() {
			return NodeRegistration.of("sources", of(MyComponentSources.class))
				.action(stateAtLeast(Registered), once(register(MySourceSet.aSourceSet("foo"))))
				.action(stateAtLeast(Registered), once(register(MySourceSet.aSourceSet("bar"))));
		}
	}
	interface MySourceSet {
		static NodeRegistration<MySourceSet> aSourceSet(String name) {
			return NodeRegistration.of(name, of(MySourceSet.class));
		}
	}

	@Test
	void canIncludeActionInNodeRegistrationThatAppliesOnlyToSelfModelNode() {
		val modelPaths = new HashSet<ModelPath>();
		modelRegistry.register(ModelRegistration.of("x", MyType.class));
		modelRegistry.register(NodeRegistration.of("y", of(MyType.class)).action(node -> modelPaths.add(node.getPath()), doSomething()));
		modelRegistry.register(ModelRegistration.of("y.foo", MyType.class));
		modelRegistry.register(ModelRegistration.of("z", MyType.class));
		assertThat("action for specific node isn't called for other nodes", modelPaths, hasItems(path("y")));
	}

	@Test
	void canIncludeActionInNodeRegistrationThatAppliesOnlyToDescendantModelNode() {
		val modelPaths = new HashSet<ModelPath>();
		modelRegistry.register(ModelRegistration.of("a", MyType.class));
		modelRegistry.register(NodeRegistration.of("b", of(MyType.class)).action(allDirectDescendants(node -> modelPaths.add(node.getPath())), doSomething()));
		modelRegistry.register(ModelRegistration.of("b.bar", MyType.class));
		modelRegistry.register(ModelRegistration.of("c", MyType.class));
		assertThat("action for descendant node isn't called for other nodes", modelPaths, hasItems(path("b.bar")));
	}

	@Test
	void canCheckExistingDescendantNode() {
		val parent = registerNode("foo");
		registerNode("foo.bar");
		assertTrue(parent.hasDescendant("bar"), "existing child node can be checked from parent node");
	}

	@Test
	void canCheckNonExistingDescendantNode() {
		val parent = registerNode("foo");
		assertFalse(parent.hasDescendant("bar"), "non-existing child node can be checked from parent node");
	}

	@Test
	void canGetExistingDescendantNode() {
		val parent = registerNode("foo");
		val child = registerNode("foo.bar");
		assertThat("existing child node can query from parent node", parent.getDescendant("bar"), equalTo(child));
	}

	@Test
	void throwsExceptionWhenGettingNonExistingDescendantNode() {
		val parent = registerNode("foo");
		assertThrows(IllegalArgumentException.class, () -> parent.getDescendant("bar"),
			"non-existing child node cannot be queried from parent node");
	}

	@Test
	void honorsNestedConfigurationActionOrder() {
		val executionOrder = new ArrayList<String>();
		modelRegistry.configureMatching(ModelSpecs.satisfyAll(), once(n1 -> {
			executionOrder.add("n1 - " + n1.getPath());
			n1.applyTo(allDirectDescendants(), once(n2 -> {
				executionOrder.add("n2 - " + n2.getPath());
				n2.applyTo(allDirectDescendants(), once(n3 -> executionOrder.add("n3 - " + n3.getPath())));
			}));
		}));

		registerNode("foo");
		assertThat(executionOrder, contains("n1 - <root>", "n1 - foo", "n2 - foo"));
		registerNode("foo.bar");
		assertThat(executionOrder, contains("n1 - <root>", "n1 - foo", "n2 - foo", "n1 - foo.bar", "n2 - foo.bar", "n3 - foo.bar"));
	}

	@Test
	void canRegisterNodeWhileDispatchingConfigurationActions() {
		val paths = new ArrayList<ModelPath>();
		registerNode("foo");
		modelRegistry.configureMatching(ModelSpecs.of(stateOf(Registered)), node -> {
			paths.add(node.getPath());
			if (node.getPath().equals(path("foo"))) {
				node.register(NodeRegistration.of("bar", of(MyType.class)));
			}
		});

		System.out.println("Current paths: " + paths);
		assertThat(paths, contains(root(), path("foo"), path("foo.bar")));
	}

	@Test // This may not be exactly the behaviour we want, let's keep a close eye
	void dispatchConfigurationActionsAsNodeAreRegistered() {
		val paths = new ArrayList<ModelPath>();
		registerNode("foo");
		registerNode("bar");
		modelRegistry.configureMatching(ModelSpecs.of(stateOf(Registered)), node -> {
			paths.add(node.getPath());
			if (node.getPath().equals(path("foo"))) {
				node.register(NodeRegistration.of("bar", of(MyType.class)));
			}
		});

		System.out.println("Current paths: " + paths);
		assertThat(paths, contains(root(), path("foo"), path("foo.bar"), path("bar")));
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
}
