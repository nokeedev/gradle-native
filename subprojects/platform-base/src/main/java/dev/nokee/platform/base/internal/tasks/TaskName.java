package dev.nokee.platform.base.internal.tasks;

import lombok.Value;

import javax.annotation.Nullable;
import java.util.Optional;

@Value
public class TaskName {
	String verb;
	@Nullable String object;

	public Optional<String> getObject() {
		return Optional.ofNullable(object);
	}

	public static TaskName of(String taskName) {
		assert taskName != null;
		assert !taskName.isEmpty();
		assert Character.isLowerCase(taskName.charAt(0));
		return new TaskName(taskName, null);
	}

	public static TaskName of(String verb, String object) {
		assert verb != null;
		assert object != null;
		assert !verb.isEmpty();
		assert !object.isEmpty();
		assert Character.isLowerCase(verb.charAt(0));
		assert Character.isLowerCase(object.charAt(0));
		return new TaskName(verb, object);
	}
}
