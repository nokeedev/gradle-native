package dev.nokee.utils;

import lombok.val;
import org.gradle.api.Action;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.ImmutableList.of;
import static dev.nokee.utils.ActionUtils.composite;
import static dev.nokee.utils.ActionUtils.doNothing;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

@Subject(ActionUtils.class)
class ActionUtils_CompositeTest {
	@Test
	void canComposeActionsUsingVarargs() {
		val execution = new ArrayList<String>();
		composite(add("first"), add("second")).execute(execution);
		assertThat(execution, contains("first", "second"));
	}

	@Test
	void canComposeActionsUsingList() {
		val execution = new ArrayList<String>();
		composite(of(add("first"), add("second"))).execute(execution);
		assertThat(execution, contains("first", "second"));
	}

	@Test
	void returnsSpecifiedActionWhenComposingOnlyOneAction() {
		Action<String> action = t -> {};
		assertThat(composite(action), equalTo(action));
		assertThat(composite(of(action)), equalTo(action));
	}

	@Test
	void ignoresActionsThatDoNothing() {
		Action<String> action = t -> {};
		assertThat(composite(doNothing()), equalTo(doNothing()));
		assertThat(composite(doNothing(), doNothing()), equalTo(doNothing()));
		assertThat(composite(action, doNothing()), equalTo(action));
		assertThat(composite(doNothing(), action), equalTo(action));
	}

	private static Action<List<String>> add(String element) {
		return t -> t.add(element);
	}
}
