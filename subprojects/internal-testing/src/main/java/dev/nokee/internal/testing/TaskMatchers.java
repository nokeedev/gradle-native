package dev.nokee.internal.testing;

import dev.nokee.utils.DeferredUtils;
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
		return group(equalTo(group));
	}

	public static Matcher<Task> group(Matcher<String> matcher) {
		return new FeatureMatcher<Task, String>(matcher, "a task with group", "task group") {
			@Override
			protected String featureValueOf(Task actual) {
				return actual.getGroup();
			}
		};
	}

	public static Matcher<Task> description(String description) {
		return description(equalTo(description));
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

	public static Matcher<Task> dependsOn(Matcher<? super Iterable<Object>> matcher) {
		return new FeatureMatcher<Task, Iterable<Object>>(matcher, "a task with dependency on", "task dependency") {
			@Override
			protected Iterable<Object> featureValueOf(Task actual) {
				return DeferredUtils.flatUnpackWhile(actual.getDependsOn(), DeferredUtils::isDeferred);
			}
		};
	}
}
