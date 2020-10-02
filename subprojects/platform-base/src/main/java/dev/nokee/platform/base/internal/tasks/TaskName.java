package dev.nokee.platform.base.internal.tasks;

import dev.nokee.model.internal.DomainObjectIdentifierInternal;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

@ToString
@EqualsAndHashCode
public final class TaskName {
	private static final TaskName EMPTY_TASK_NAME = new TaskName("", null);
	private final String verb;
	private final String object;

	private TaskName(String verb, String object) {
		this.verb = verb;
		this.object = object;
	}

	public String getVerb() {
		return verb;
	}

	public Optional<String> getObject() {
		return Optional.ofNullable(object);
	}

	/**
	 * Used as part of {@link TaskIdentifier#ofLifecycle(DomainObjectIdentifierInternal)}
	 *
	 * @return an empty task name, never null.
 	 */
	static TaskName empty() {
		return EMPTY_TASK_NAME;
	}

	public static TaskName of(String taskName) {
		requireNonNull(taskName);
		checkArgument(!taskName.isEmpty());
		checkArgument(Character.isLowerCase(taskName.charAt(0)));
		return new TaskName(taskName, null);
	}

	public static TaskName of(String verb, String object) {
		requireNonNull(verb);
		requireNonNull(object);
		checkArgument(!verb.isEmpty());
		checkArgument(!object.isEmpty());
		checkArgument(Character.isLowerCase(verb.charAt(0)));
		checkArgument(Character.isLowerCase(object.charAt(0)));
		return new TaskName(verb, object);
	}
}
