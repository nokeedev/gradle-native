/*
 * Copyright 2020 the original author or authors.
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
package dev.nokee.model.internal.core;

import com.google.common.collect.ImmutableList;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.model.internal.type.ModelType;
import lombok.EqualsAndHashCode;
import lombok.Value;

import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import static dev.nokee.model.internal.state.ModelState.Realized;
import static dev.nokee.model.internal.state.ModelState.Registered;
import static java.util.Objects.requireNonNull;

public final class ModelTestActions {
	private ModelTestActions() {}

	/**
	 * Returns an action that do something meaningless.
	 * All instance created are equal to each other.
	 * <p>
	 * Why not use {@link ModelActions#doNothing()}?
	 * Because the implementation here should not be considered a no-op but rather some work that we don't really care for the purpose of the test.
	 *
	 * @return a model action that does something meaningless, never null.
	 */
	public static ModelAction doSomething() {
		return new DoSomethingAction();
	}

	@EqualsAndHashCode
	private static final class DoSomethingAction implements ModelAction {
		@Override
		public void execute(ModelNode node) {
			// doing something meaningless
		}

		@Override
		public String toString() {
			return "ModelTestActions.doSomething()";
		}
	}

	/**
	 * Returns an action that do something meaningless different than {@link #doSomething()} and {@link #doSomethingElse(Object)}.
	 * All instance created are equal to each other.
	 * <p>
	 * Why not use {@link ModelActions#doNothing()}?
	 * Because the implementation here should not be considered a no-op but rather some work that we don't really care for the purpose of the test.
	 * <p>
	 * Why not use {@link #doSomething()}?
	 * Because the implementation here convey that it's some work that is different than its counterpart.
	 * <p>
	 * Why not use {@link #doSomethingElse(Object)}?
	 * Because the implementation here convey that it's some work that is different but not specific on what is different.
	 *
	 * @return a model action that does something else meaningless than {@link #doSomething()}, never null.
	 */
	public static ModelAction doSomethingElse() {
		return new DoSomethingElseAction(null);
	}

	/**
	 * Returns an action that do something meaningless distinguisable from {@link #doSomething()} and {@link #doSomethingElse()}.
	 * All instance created with the same {@literal what} are equal to each other.
	 * <p>
	 * Why not use {@link ModelActions#doNothing()}?
	 * Because the implementation here should not be considered a no-op but rather some work that we don't really care for the purpose of the test.
	 * <p>
	 * Why not use {@link #doSomething()} or {@link #doSomethingElse()}?
	 * Because this implementation here convey "what" is different than its counterpart.
	 *
	 * @param what  the differentiator for the work to be done
	 * @return a model action that does something else meaningless than {@link #doSomething()} and {@link #doSomethingElse()}, never null.
	 */
	public static ModelAction doSomethingElse(Object what) {
		return new DoSomethingElseAction(requireNonNull(what));
	}

	@EqualsAndHashCode
	private static final class DoSomethingElseAction implements ModelAction {
		@Nullable private final Object what;

		public DoSomethingElseAction(@Nullable Object what) {
			this.what = what;
		}

		@Override
		public void execute(ModelNode node) {
			// doing something else meaningless
		}

		@Override
		public String toString() {
			return "ModelTestActions.doSomethingElse(" + (what == null ? "" : what) + ")";
		}
	}

	public static class CaptureNodeTransitionAction extends ModelActionWithInputs {
		private final List<NodeStateTransition> values = new ArrayList<>();
		private final List<ModelComponentReference<?>> inputs;
		private final Bits inputBits;

		public CaptureNodeTransitionAction() {
			this.inputs = ImmutableList.of(ModelComponentReference.of(ModelPathComponent.class), ModelComponentReference.of(ModelState.class));
			this.inputBits = inputs.stream().map(ModelComponentReference::componentBits).reduce(Bits.empty(), Bits::or);
		}

		public List<NodeStateTransition> getAllTransitions() {
			return values;
		}

		@Override
		public void execute(ModelNode node, List<?> inputs) {
			values.add(new CaptureNodeTransitionAction.NodeStateTransition(ModelNodeUtils.getPath(node), ModelStates.getState(node)));
		}

		@Override
		public List<? extends ModelComponentReference<?>> getInputs() {
			return inputs;
		}

		@Override
		public Bits getInputBits() {
			return inputBits;
		}

		public static CaptureNodeTransitionAction.NodeStateTransition realized(Object path) {
			return new CaptureNodeTransitionAction.NodeStateTransition(asPath(path), Realized);
		}

		public static CaptureNodeTransitionAction.NodeStateTransition registered(Object path) {
			return new CaptureNodeTransitionAction.NodeStateTransition(asPath(path), Registered);
		}

		public static CaptureNodeTransitionAction.NodeStateTransition initialized(Object path) {
			return new CaptureNodeTransitionAction.NodeStateTransition(asPath(path), ModelState.Initialized);
		}

		public static CaptureNodeTransitionAction.NodeStateTransition created(Object path) {
			return new CaptureNodeTransitionAction.NodeStateTransition(asPath(path), ModelState.Created);
		}

		private static ModelPath asPath(Object path) {
			if (path instanceof ModelPath) {
				return (ModelPath) path;
			} else if (path instanceof String) {
				return ModelPath.path((String) path);
			}
			throw new IllegalArgumentException("Invalid path '" + path + "'");
		}

		@Value
		public static class NodeStateTransition {
			ModelPath path;
			ModelState state;
		}
	}
}
