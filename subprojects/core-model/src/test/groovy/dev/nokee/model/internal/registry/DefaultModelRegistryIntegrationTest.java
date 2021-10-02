/*
 * Copyright 2020-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.nokee.model.internal.registry;

import com.google.common.collect.ImmutableList;
import dev.nokee.internal.testing.util.ProjectTestUtils;
import dev.nokee.model.internal.core.*;
import dev.nokee.model.internal.type.ModelType;
import lombok.val;
import org.gradle.api.provider.Property;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.function.Predicate;

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
import static dev.nokee.model.internal.core.ModelTestActions.CaptureNodeTransitionAction.*;
import static dev.nokee.model.internal.core.ModelTestActions.doSomething;
import static dev.nokee.model.internal.core.NodePredicate.allDirectDescendants;
import static dev.nokee.model.internal.core.NodePredicate.self;
import static dev.nokee.model.internal.registry.DefaultModelRegistryIntegrationTest.MyComponent.aComponent;
import static dev.nokee.model.internal.type.ModelType.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

public class DefaultModelRegistryIntegrationTest {
	private final DefaultModelRegistry modelRegistry = new DefaultModelRegistry(ProjectTestUtils.objectFactory()::newInstance);

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
		val provider = modelRegistry.register(unmanagedInstance(of("bar", ManagedType.class), () -> ProjectTestUtils.objectFactory().newInstance(ManagedType.class)));
		assertThat(provider.get(), isA(ManagedType.class));
	}

	@Test
	void canRegisterUnmanagedInstanceModel() {
		val provider = modelRegistry.register(unmanagedInstance(of("bar", UnmanagedType.class), UnmanagedType::new));
		assertThat(provider.get(), isA(UnmanagedType.class));
	}

	@Test
	void canAccessModelNodeOnManagedType() {
		val provider = modelRegistry.register(unmanagedInstance(of("a", ModelNodeAccessingType.class), () -> ProjectTestUtils.objectFactory().newInstance(ModelNodeAccessingType.class)));
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
		val action = new ModelTestActions.CaptureNodeTransitionAction();

		modelRegistry.register(ModelRegistration.of("a", MyType.class));
		modelRegistry.register(ModelRegistration.of("b", MyType.class));
		modelRegistry.configure(action);

		assertThat(action.getAllTransitions(),
			contains(registered(root()), registered("a"), registered("b")));
	}

	@Test
	void canConfigureFutureNodesRegistered() {
		val action = new ModelTestActions.CaptureNodeTransitionAction();

		modelRegistry.configure(action);
		modelRegistry.register(ModelRegistration.of("x", MyType.class));
		modelRegistry.register(ModelRegistration.of("y", MyType.class));

		assertThat(action.getAllTransitions(),
			contains(registered(root()), created("x"), initialized("x"), registered("x"), created("y"), initialized("y"), registered("y")));
	}

	@Test
	void queryProviderRealizeNodeAndParent() {
		val action = new ModelTestActions.CaptureNodeTransitionAction();

		modelRegistry.configure(action);
		modelRegistry.register(ModelRegistration.of("x", MyType.class)).get();

		assertThat(action.getAllTransitions(),
			contains(registered(root()), created("x"), initialized("x"), registered("x"), realized(root()), realized("x")));
	}

	@Test
	void canConfigureNodesOnlyWhenOnSpecificState() {
		val action = new ModelTestActions.CaptureNodeTransitionAction();

		modelRegistry.register(ModelRegistration.of("i", MyType.class));
		modelRegistry.configure(matching(node -> ModelNodeUtils.isAtLeast(node, Realized), action));
		modelRegistry.register(ModelRegistration.of("j", MyType.class)).get();

		assertThat(action.getAllTransitions(), contains(realized(root()), realized("j")));
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
			ModelNodeUtils.getDirectDescendants(modelRegistry.get(path("a"))),
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
				.action(self(stateAtLeast(Registered)).apply(once(register(MyComponentSources.componentSources()))));
		}
	}
	interface MyComponentSources {
		static NodeRegistration<MyComponentSources> componentSources() {
			return NodeRegistration.of("sources", of(MyComponentSources.class))
				.action(self(stateAtLeast(Registered)).apply(once(register(MySourceSet.aSourceSet("foo")))))
				.action(self(stateAtLeast(Registered)).apply(once(register(MySourceSet.aSourceSet("bar")))));
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
		modelRegistry.register(NodeRegistration.of("y", of(MyType.class)).action(self((Predicate<ModelNode>) node -> modelPaths.add(node.getPath())).apply(doSomething())));
		modelRegistry.register(ModelRegistration.of("y.foo", MyType.class));
		modelRegistry.register(ModelRegistration.of("z", MyType.class));
		assertThat("action for specific node isn't called for other nodes", modelPaths, hasItems(path("y")));
	}

	@Test
	void canIncludeActionInNodeRegistrationThatAppliesOnlyToDescendantModelNode() {
		val modelPaths = new HashSet<ModelPath>();
		modelRegistry.register(ModelRegistration.of("a", MyType.class));
		modelRegistry.register(NodeRegistration.of("b", of(MyType.class)).action(allDirectDescendants(node -> modelPaths.add(node.getPath())).apply(doSomething())));
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
		modelRegistry.configure(once(n1 -> {
			executionOrder.add("n1 - " + n1.getPath());
			n1.applyTo(allDirectDescendants().apply(once(n2 -> {
				executionOrder.add("n2 - " + n2.getPath());
				n2.applyTo(allDirectDescendants().apply(once(n3 -> executionOrder.add("n3 - " + n3.getPath()))));
			})));
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
		modelRegistry.configure(matching(ModelSpecs.of(stateOf(Registered)), node -> {
			paths.add(node.getPath());
			if (node.getPath().equals(path("foo"))) {
				node.register(NodeRegistration.of("bar", of(MyType.class)));
			}
		}));

		System.out.println("Current paths: " + paths);
		assertThat(paths, contains(root(), path("foo"), path("foo.bar")));
	}

	@Test // This may not be exactly the behaviour we want, let's keep a close eye
	void dispatchConfigurationActionsAsNodeAreRegistered() {
		val paths = new ArrayList<ModelPath>();
		registerNode("foo");
		registerNode("bar");
		modelRegistry.configure(matching(ModelSpecs.of(stateOf(Registered)), node -> {
			paths.add(node.getPath());
			if (node.getPath().equals(path("foo"))) {
				node.register(NodeRegistration.of("bar", of(MyType.class)));
			}
		}));

		System.out.println("Current paths: " + paths);
		assertThat(paths, contains(root(), path("foo"), path("foo.bar"), path("bar")));
	}

	@Test
	void realizingChildNodeWhileTheParentNodeIsBeingRealizedCallbacksOnlyOnce() {
		val action = new ModelTestActions.CaptureNodeTransitionAction();
		modelRegistry.configure(action);

		modelRegistry.register(NodeRegistration.of("a", of(MyParent.class))
			.action(self(discover(ctx -> ctx.register(NodeRegistration.of("child", of(MyChild.class))))))
			.action(self(mutate(of(MyParent.class), MyParent::getChild))));
		ModelNodeUtils.realize(modelRegistry.get(path("a")));

		assertThat(action.getAllTransitions(), contains(registered(root()),
			created("a"), initialized("a"), registered("a"),
			created("a.child"), initialized("a.child"), registered("a.child"),
			realized(root()), realized("a"), realized("a.child")));
	}

	@Test
	void realizingChildNodeWhileTheChildNodeIsBeingRealizedCallbacksOnlyOnce() {
		val action = new ModelTestActions.CaptureNodeTransitionAction();
		modelRegistry.configure(action);

		modelRegistry.register(NodeRegistration.of("a", of(MyParent.class))
			.action(self(discover(ctx -> ctx.register(NodeRegistration.of("child", of(MyChild.class))))))
			.action(self(mutate(of(MyParent.class), MyParent::getChild))));
		ModelNodeUtils.realize(modelRegistry.get(path("a.child")));

		assertThat(action.getAllTransitions(), contains(registered(root()),
			created("a"), initialized("a"), registered("a"),
			created("a.child"), initialized("a.child"), registered("a.child"),
			realized(root()), realized("a"), realized("a.child")));
	}

	interface MyParent {
		default MyChild getChild() {
			// When querying descendant node, it's best practice to realize the node.
			return ModelNodeUtils.realize(ModelNodes.of(this).getDescendant("child")).get(MyChild.class);
		}
	}

	interface MyChild {}

	@Test
	void canExecuteActionWithComponentInputs() {
		val result = new ArrayList<ModelPath>();
		modelRegistry.configure(ModelActionWithInputs.of(ModelType.of(MyFooComponent.class), (node, i) -> {
			result.add(node.getPath());
		}));
		modelRegistry.register(ModelRegistration.bridgedInstance(ModelIdentifier.of("foo", Object.class), new Object()));
		modelRegistry.register(ModelRegistration.bridgedInstance(ModelIdentifier.of("foo.bar", Object.class), new Object()));
		modelRegistry.register(ModelRegistration.bridgedInstance(ModelIdentifier.of("foo.far", MyFooComponent.class), new MyFooComponent()));
		assertThat(result, contains(ModelPath.path("foo.far")));
	}

	static class MyFooComponent {}

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
