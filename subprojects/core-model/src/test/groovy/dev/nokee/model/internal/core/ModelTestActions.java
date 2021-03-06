package dev.nokee.model.internal.core;

import lombok.EqualsAndHashCode;
import lombok.Value;

import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import static dev.nokee.model.internal.core.ModelNode.State.Realized;
import static dev.nokee.model.internal.core.ModelNode.State.Registered;
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

	public static class CaptureNodeTransitionAction implements ModelAction {
		private final List<NodeStateTransition> values = new ArrayList<>();

		public List<NodeStateTransition> getAllTransitions() {
			return values;
		}

		@Override
		public void execute(ModelNode node) {
			values.add(new CaptureNodeTransitionAction.NodeStateTransition(node.getPath(), node.getState()));
		}

		public static CaptureNodeTransitionAction.NodeStateTransition realized(Object path) {
			return new CaptureNodeTransitionAction.NodeStateTransition(asPath(path), Realized);
		}

		public static CaptureNodeTransitionAction.NodeStateTransition registered(Object path) {
			return new CaptureNodeTransitionAction.NodeStateTransition(asPath(path), Registered);
		}

		public static CaptureNodeTransitionAction.NodeStateTransition initialized(Object path) {
			return new CaptureNodeTransitionAction.NodeStateTransition(asPath(path), ModelNode.State.Initialized);
		}

		public static CaptureNodeTransitionAction.NodeStateTransition created(Object path) {
			return new CaptureNodeTransitionAction.NodeStateTransition(asPath(path), ModelNode.State.Created);
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
			ModelNode.State state;
		}
	}
}
