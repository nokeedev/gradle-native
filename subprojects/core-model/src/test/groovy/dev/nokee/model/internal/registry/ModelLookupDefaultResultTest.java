package dev.nokee.model.internal.registry;

import com.google.common.collect.ImmutableList;
import dev.nokee.model.internal.core.ModelNode;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import spock.lang.Subject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static dev.nokee.model.internal.core.ModelPath.path;
import static dev.nokee.model.internal.core.ModelTestUtils.node;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

@Subject(ModelLookupDefaultResult.class)
class ModelLookupDefaultResultTest {
	private final ModelNode n0 = node("n0");
	private final ModelNode n1 = node("n1");
	private final ModelNode n2 = node("n2");
	private final List<ModelNode> result = new ArrayList<>(ImmutableList.of(n0, n1, n2));
	private final ModelLookup.Result subject = new ModelLookupDefaultResult(result);

	@Test
	void canGetTheValues() {
		assertThat(subject.get(), contains(n0, n1, n2));
	}

	@Test
	void canMapTheResult() {
		assertThat(subject.map(ModelNode::getPath), contains(path("n0"), path("n1"), path("n2")));
	}

	@Test
	void canIterateThroughTheResult() {
		val captor = ArgumentCaptor.forClass(ModelNode.class);
		@SuppressWarnings("unchecked") val action = (Consumer<ModelNode>) mock(Consumer.class);
		doNothing().when(action).accept(captor.capture());
		subject.forEach(action);
		assertThat(captor.getAllValues(), contains(n0, n1, n2));
	}

	@Test
	void isolateTheResults() {
		val n3 = node("n3");
		result.add(n3);
		assertThat(subject.get(), not(hasItem(n3)));
	}
}
