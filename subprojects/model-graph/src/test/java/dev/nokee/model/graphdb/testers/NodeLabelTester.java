package dev.nokee.model.graphdb.testers;

import dev.nokee.model.graphdb.Node;
import lombok.val;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import static dev.nokee.model.graphdb.Label.label;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;

public interface NodeLabelTester {
	Node createSubject();

	@Test
	default void newNodeHasNoLabel() {
		assertThat(createSubject().getLabels().count(), is(0));
	}

	@Test
	default void canAddLabel() {
		val label = label("foo");
		val subject = createSubject();
		subject.addLabel(label);
		assertThat(subject.getLabels().collect(toList()), contains(label));
	}

	@Test
	default void canCheckNodeHasLabel() {
		val subject = createSubject();
		subject.addLabel(label("bar"));
		assertAll(
			() -> assertThat(subject.hasLabel(label("bar")), is(true)),
			() -> assertThat(subject.hasLabel(label("foo")), is(false))
		);
	}

	@Test
	default void canOnlyAddLabelOnce() {
		val label = label("bar");
		val subject = createSubject();
		subject.addLabel(label);
		subject.addLabel(label);
		assertThat(subject.getLabels().collect(toList()), contains(label));
	}

	@Test
	default void accessLabelInOrderTheyWereAdded() {
		val a = label("aaa");
		val b = label("bbb");
		val subject = createSubject();
		subject.addLabel(b);
		subject.addLabel(a);
		assertThat(subject.getLabels().collect(toList()), contains(b, a));
	}

	@Test
	default void returnsNodeInstanceWhenAddingLabel() {
		val subject = createSubject();
		assertThat(subject.addLabel(label("far")), is(subject));
	}
}
