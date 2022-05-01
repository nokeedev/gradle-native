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
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelComponent;
import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.core.ModelIdentifier;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.core.ModelPath;
import dev.nokee.model.internal.core.ModelPathComponent;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.core.ModelSpecs;
import dev.nokee.model.internal.core.ModelTestActions;
import dev.nokee.model.internal.core.NodeRegistration;
import dev.nokee.model.internal.core.RelativeConfigurationService;
import dev.nokee.model.internal.core.RelativeRegistrationService;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.model.internal.state.ModelStates;
import lombok.Value;
import lombok.val;
import org.gradle.api.provider.Property;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.function.Predicate;

import static dev.nokee.model.internal.core.ModelActions.matching;
import static dev.nokee.model.internal.core.ModelActions.once;
import static dev.nokee.model.internal.core.ModelIdentifier.of;
import static dev.nokee.model.internal.core.ModelNodes.stateOf;
import static dev.nokee.model.internal.core.ModelPath.path;
import static dev.nokee.model.internal.core.ModelPath.root;
import static dev.nokee.model.internal.core.ModelRegistration.bridgedInstance;
import static dev.nokee.model.internal.core.ModelRegistration.unmanagedInstance;
import static dev.nokee.model.internal.core.ModelTestActions.CaptureNodeTransitionAction.created;
import static dev.nokee.model.internal.core.ModelTestActions.CaptureNodeTransitionAction.initialized;
import static dev.nokee.model.internal.core.ModelTestActions.CaptureNodeTransitionAction.realized;
import static dev.nokee.model.internal.core.ModelTestActions.CaptureNodeTransitionAction.registered;
import static dev.nokee.model.internal.core.ModelTestActions.doSomething;
import static dev.nokee.model.internal.core.NodePredicate.allDirectDescendants;
import static dev.nokee.model.internal.core.NodePredicate.self;
import static dev.nokee.model.internal.state.ModelState.Realized;
import static dev.nokee.model.internal.state.ModelState.Registered;
import static dev.nokee.model.internal.type.ModelType.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DefaultModelRegistryIntegrationTest {
	private final DefaultModelRegistry modelRegistry = new DefaultModelRegistry(ProjectTestUtils.objectFactory()::newInstance);

	@Test
	void modelNodeRegistrationGivesAccessToProviderThatResolvesToTheNodeValue() {
		val provider = modelRegistry.register(ModelRegistration.of("myType", MyType.class)).as(MyType.class);
		assertAll(() -> {
			assertThat(provider.getIdentifier(), equalTo(of("myType", MyType.class)));
			assertThat(provider.get(), isA(MyType.class));
		});
	}

	@Test
	void canAccessRegisteredModel() {
		assertThrows(IllegalStateException.class, () -> modelRegistry.get("foo", MyType.class));
		val provider = modelRegistry.register(ModelRegistration.of("foo", MyType.class)).as(MyType.class);
		assertEquals(provider, modelRegistry.get("foo", MyType.class));
	}

	@Test
	void canRegisterBridgedInstanceModel() {
		val instance = new UnmanagedType();
		val provider = modelRegistry.register(bridgedInstance(of("bar", UnmanagedType.class), instance)).as(UnmanagedType.class);
		assertEquals(instance, provider.get());
	}

	@Test
	void canRegisterManagedInstanceModel() {
		val provider = modelRegistry.register(unmanagedInstance(of("bar", ManagedType.class), () -> ProjectTestUtils.objectFactory().newInstance(ManagedType.class))).as(ManagedType.class);
		assertThat(provider.get(), isA(ManagedType.class));
	}

	@Test
	void canRegisterUnmanagedInstanceModel() {
		val provider = modelRegistry.register(unmanagedInstance(of("bar", UnmanagedType.class), UnmanagedType::new)).as(UnmanagedType.class);
		assertThat(provider.get(), isA(UnmanagedType.class));
	}

	@Test
	void canAccessModelNodeOnManagedType() {
		val provider = modelRegistry.register(unmanagedInstance(of("a", ModelNodeAccessingType.class), () -> ProjectTestUtils.objectFactory().newInstance(ModelNodeAccessingType.class))).as(ModelNodeAccessingType.class);
		assertEquals("a", provider.get().getModelPathAsString());
	}
	interface ModelNodeAccessingType {
		default String getModelPathAsString() {
			return ModelNodeUtils.getPath(ModelNodes.of(this)).get();
		}
	}

	// Values are kept between
	@Test
	void valuesArePersistedOnNodeForManagedType() {
		val provider = modelRegistry.register(ModelRegistration.of("a", ManagedValueType.class)).as(ManagedValueType.class);
		provider.get().getValue().set("foo-value");
		assertEquals("foo-value", provider.get().getValue().getOrNull());
	}

	interface ManagedValueType {
		Property<String> getValue();
	}

	@Test
	void valuesArePersistedOnNodeForUnmanagedType() {
		val provider = modelRegistry.register(ModelRegistration.bridgedInstance(of("a", UnmanagedValueType.class), new UnmanagedValueType())).as(UnmanagedValueType.class);
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

		modelRegistry.register(ModelRegistration.of("a", MyType.class)).as(MyType.class);
		modelRegistry.register(ModelRegistration.of("b", MyType.class)).as(MyType.class);
		modelRegistry.configure(action);

		assertThat(action.getAllTransitions(),
			contains(registered(root()), registered("a"), registered("b")));
	}

	@Test
	void canConfigureFutureNodesRegistered() {
		val action = new ModelTestActions.CaptureNodeTransitionAction();

		modelRegistry.configure(action);
		modelRegistry.register(ModelRegistration.of("x", MyType.class)).as(MyType.class);
		modelRegistry.register(ModelRegistration.of("y", MyType.class)).as(MyType.class);

		assertThat(action.getAllTransitions(),
			contains(registered(root()), created("x"), initialized("x"), registered("x"), created("y"), initialized("y"), registered("y")));
	}

	@Test
	void queryProviderRealizeNodeAndParent() {
		val action = new ModelTestActions.CaptureNodeTransitionAction();

		modelRegistry.configure(action);
		modelRegistry.register(ModelRegistration.of("x", MyType.class)).as(MyType.class).get();

		assertThat(action.getAllTransitions(),
			contains(registered(root()), created("x"), initialized("x"), registered("x"), realized(root()), realized("x")));
	}

	@Test
	void canConfigureNodesOnlyWhenOnSpecificState() {
		val action = new ModelTestActions.CaptureNodeTransitionAction();

		modelRegistry.register(ModelRegistration.of("i", MyType.class));
		modelRegistry.configure(matching(node -> ModelStates.isAtLeast(node, Realized), action));
		modelRegistry.register(ModelRegistration.of("j", MyType.class)).as(MyType.class).get();

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
	void canIncludeActionInNodeRegistrationThatAppliesOnlyToSelfModelNode() {
		val modelPaths = new HashSet<ModelPath>();
		modelRegistry.register(ModelRegistration.of("x", MyType.class));
		modelRegistry.register(NodeRegistration.of("y", of(MyType.class)).action(self((Predicate<ModelNode>) node -> modelPaths.add(ModelNodeUtils.getPath(node))).apply(doSomething())));
		modelRegistry.register(ModelRegistration.of("y.foo", MyType.class));
		modelRegistry.register(ModelRegistration.of("z", MyType.class));
		assertThat("action for specific node isn't called for other nodes", modelPaths, hasItems(path("y")));
	}

	@Test
	void canIncludeActionInNodeRegistrationThatAppliesOnlyToDescendantModelNode() {
		val modelPaths = new HashSet<ModelPath>();
		modelRegistry.register(ModelRegistration.of("a", MyType.class));
		modelRegistry.register(NodeRegistration.of("b", of(MyType.class)).action(allDirectDescendants(node -> modelPaths.add(ModelNodeUtils.getPath(node))).apply(doSomething())));
		modelRegistry.register(ModelRegistration.of("b.bar", MyType.class));
		modelRegistry.register(ModelRegistration.of("c", MyType.class));
		assertThat("action for descendant node isn't called for other nodes", modelPaths, hasItems(path("b.bar")));
	}

	@Test
	void canCheckExistingDescendantNode() {
		val parent = registerNode("foo");
		registerNode("foo.bar");
		assertTrue(ModelNodeUtils.hasDescendant(parent, "bar"), "existing child node can be checked from parent node");
	}

	@Test
	void canCheckNonExistingDescendantNode() {
		val parent = registerNode("foo");
		assertFalse(ModelNodeUtils.hasDescendant(parent, "bar"), "non-existing child node can be checked from parent node");
	}

	@Test
	void canGetExistingDescendantNode() {
		val parent = registerNode("foo");
		val child = registerNode("foo.bar");
		assertThat("existing child node can query from parent node", ModelNodeUtils.getDescendant(parent, "bar"), equalTo(child));
	}

	@Test
	void throwsExceptionWhenGettingNonExistingDescendantNode() {
		val parent = registerNode("foo");
		assertThrows(IllegalArgumentException.class, () -> ModelNodeUtils.getDescendant(parent, "bar"),
			"non-existing child node cannot be queried from parent node");
	}

	@Test
	void honorsNestedConfigurationActionOrder() {
		val executionOrder = new ArrayList<String>();
		modelRegistry.configure(once(ModelActionWithInputs.of(ModelComponentReference.of(ModelPathComponent.class), ModelComponentReference.of(ModelState.class), ModelComponentReference.of(RelativeConfigurationService.class), (n1, path1, state1, configurer1) -> {
			executionOrder.add("n1 - " + ModelNodeUtils.getPath(n1));
			ModelNodeUtils.applyTo(n1, allDirectDescendants().apply(once(ModelActionWithInputs.of(ModelComponentReference.of(ModelPathComponent.class), ModelComponentReference.of(ModelState.class), ModelComponentReference.of(RelativeConfigurationService.class), (n2, path2, state2, configurer2) -> {
				executionOrder.add("n2 - " + ModelNodeUtils.getPath(n2));
				ModelNodeUtils.applyTo(n2, allDirectDescendants().apply(once(ModelActionWithInputs.of(ModelComponentReference.of(ModelPathComponent.class), ModelComponentReference.of(ModelState.class), (n3, path3, state3) -> executionOrder.add("n3 - " + ModelNodeUtils.getPath(n3))))));
			}))));
		})));

		registerNode("foo");
		assertThat(executionOrder, contains("n1 - <root>", "n1 - foo", "n2 - foo"));
		registerNode("foo.bar");
		assertThat(executionOrder, contains("n1 - <root>", "n1 - foo", "n2 - foo", "n1 - foo.bar", "n2 - foo.bar", "n3 - foo.bar"));
	}

	@Test
	void canRegisterNodeWhileDispatchingConfigurationActions() {
		val paths = new ArrayList<ModelPath>();
		registerNode("foo");
		modelRegistry.configure(ModelActionWithInputs.of(ModelComponentReference.of(ModelPathComponent.class), ModelComponentReference.of(ModelState.IsAtLeastRegistered.class), ModelComponentReference.of(RelativeRegistrationService.class),(node, path, stateTag, registry) -> {
			paths.add(ModelNodeUtils.getPath(node));
			if (ModelNodeUtils.getPath(node).equals(path("foo"))) {
				ModelNodeUtils.register(node, NodeRegistration.of("bar", of(MyType.class)));
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
		modelRegistry.configure(matching(ModelSpecs.of(stateOf(Registered)), ModelActionWithInputs.of(ModelComponentReference.of(ModelPathComponent.class), ModelComponentReference.of(RelativeRegistrationService.class), (node, path, registry) -> {
			paths.add(ModelNodeUtils.getPath(node));
			if (ModelNodeUtils.getPath(node).equals(path("foo"))) {
				ModelNodeUtils.register(node, NodeRegistration.of("bar", of(MyType.class)));
			}
		})));

		System.out.println("Current paths: " + paths);
		assertThat(paths, contains(root(), path("foo"), path("foo.bar"), path("bar")));
	}

	@Test
	void canExecuteActionWithComponentInputs() {
		val result = new ArrayList<ModelPath>();
		modelRegistry.configure(ModelActionWithInputs.of(ModelComponentReference.of(ModelPathComponent.class), ModelComponentReference.of(MyFooComponent.class), (node, path, i) -> {
			result.add(ModelNodeUtils.getPath(node));
		}));
		modelRegistry.register(ModelRegistration.bridgedInstance(ModelIdentifier.of("foo", Object.class), new Object()));
		modelRegistry.register(ModelRegistration.bridgedInstance(ModelIdentifier.of("foo.bar", Object.class), new Object()));
		modelRegistry.register(ModelRegistration.unmanagedInstanceBuilder(ModelIdentifier.of("foo.far", Object.class), Object::new).action(ModelActionWithInputs.of(ModelComponentReference.of(ModelPathComponent.class), (entity, path) -> {
			if (path.get().equals(ModelPath.path("foo.far"))) {
				entity.addComponent(new MyFooComponent());
			}
		})).build());
		assertThat(result, contains(ModelPath.path("foo.far")));
	}

	@Value
	static class MyFooComponent implements ModelComponent {}

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
