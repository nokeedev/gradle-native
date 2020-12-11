package dev.nokee.model.internal.core;

import dev.nokee.model.internal.type.ModelType;
import lombok.EqualsAndHashCode;
import org.gradle.api.Action;

import java.util.Objects;

public final class ModelActions {
	private ModelActions() {}

	public static ModelAction doNothing() {
		return ModelNodeAction.DO_NOTHING;
	}

	private enum ModelNodeAction implements ModelAction {
		DO_NOTHING {
			@Override
			public void execute(ModelNode node) {
				// do nothing.
			}

			@Override
			public String toString() {
				return "ModelActions.doNothing()";
			}
		}
	}

	public static <T> ModelAction executeUsingProjection(ModelType<T> type, Action<? super T> action) {
		return new ExecuteUsingProjectionModelAction<>(type, action);
	}

	@EqualsAndHashCode
	private static final class ExecuteUsingProjectionModelAction<T> implements ModelAction {
		private final ModelType<T> type;
		private final Action<? super T> action;

		private ExecuteUsingProjectionModelAction(ModelType<T> type, Action<? super T> action) {
			this.type = Objects.requireNonNull(type);
			this.action = Objects.requireNonNull(action);
		}

		@Override
		public void execute(ModelNode node) {
			action.execute(node.get(type));
		}

		@Override
		public String toString() {
			return "ModelActions.executeUsingProjection(" + type + ", " + action + ")";
		}
	}

	/**
	 * Returns an action that will only be executed once regardless of the node.
	 *
	 * @param action  the action to execute only once
	 * @return an action that will only execute once regardless of the node, never null.
	 */
	public static ModelAction once(ModelAction action) {
		return new OnceModelAction(action);
	}

	@EqualsAndHashCode
	private static final class OnceModelAction implements ModelAction {
		private final ModelAction action;
		@EqualsAndHashCode.Exclude private boolean executed = false;

		public OnceModelAction(ModelAction action) {
			this.action = Objects.requireNonNull(action);
		}

		@Override
		public void execute(ModelNode node) {
			if (!executed) {
				try {
					action.execute(node);
				} finally {
					executed = true;
				}
			}
		}

		@Override
		public String toString() {
			return "ModelActions.once(" + action + ")";
		}
	}

	/**
	 * Returns an action that will register the specified registration on the node.
	 *
	 * @param registration  the node to register
	 * @return an action that will register a child node, never null.
	 */
	public static ModelAction register(NodeRegistration<?> registration) {
		return new RegisterModelAction(registration);
	}

	@EqualsAndHashCode
	private static final class RegisterModelAction implements ModelAction {
		private final NodeRegistration<?> registration;

		public RegisterModelAction(NodeRegistration<?> registration) {
			this.registration = Objects.requireNonNull(registration);
		}

		@Override
		public void execute(ModelNode node) {
			node.register(registration);
		}

		@Override
		public String toString() {
			return "ModelActions.register(" + registration + ")";
		}
	}
}
