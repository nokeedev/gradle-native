package dev.nokee.model.graphdb;

import lombok.val;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

final class NodeLabels {
	private final Set<Label> labelList = new LinkedHashSet<>();
	private final long id;
	private final GraphEventNotifier eventNotifier;

	public NodeLabels(long id, GraphEventNotifier eventNotifier) {
		this.id = id;
		this.eventNotifier = eventNotifier;
	}

	public void add(Label label) {
		requireNonNull(label);
		labelList.add(label);
		eventNotifier.fireLabelAddedEvent(builder -> builder.nodeId(id).label(label));
	}

	public boolean has(Label label) {
		requireNonNull(label);
		return labelList.contains(label);
	}

	public Stream<Label> stream() {
		return labelList.stream();
	}
}
