package dev.nokee.internal.testing;

import org.gradle.api.Task;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

import static org.hamcrest.Matchers.equalTo;

public final class TaskMatchers {
	/**
	 * Matches a task group with the specified value.
	 *
	 * @param group  a task group to match, must not be null
	 * @return a task matcher, never null
	 */
	public static Matcher<Task> group(String group) {
		return new FeatureMatcher<Task, String>(equalTo(group), "a task with group", "task group") {
			@Override
			protected String featureValueOf(Task actual) {
				return actual.getGroup();
			}
		};
	}

	/**
	 * Matches a task description using the specified matcher.
	 *
	 * @param matcher  a task description to matcher, must not be null
	 * @return a task matcher, never null
	 */
	public static Matcher<Task> description(Matcher<? super String> matcher) {
		return new FeatureMatcher<Task, String>(matcher, "a task with description of", "task description") {
			@Override
			protected String featureValueOf(Task actual) {
				return actual.getDescription();
			}
		};
	}
}
