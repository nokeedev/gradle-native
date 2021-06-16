package dev.nokee.model.graphdb;

import dev.nokee.model.graphdb.events.*;
import lombok.val;

import java.util.function.Consumer;

abstract class GraphEventNotifier {
	public abstract void firePropertyChangedEvent(Consumer<? super PropertyChangedEvent.Builder> builderAction);
	public abstract void fireNodeCreatedEvent(Consumer<? super NodeCreatedEvent.Builder> builderAction);
	public abstract void fireRelationshipCreatedEvent(Consumer<? super RelationshipCreatedEvent.Builder> builderAction);
	public abstract void fireLabelAddedEvent(Consumer<? super LabelAddedEvent.Builder> builderAction);

	public static GraphEventNotifier noOpNotifier() {
		return new GraphEventNotifier() {
			@Override
			public void firePropertyChangedEvent(Consumer<? super PropertyChangedEvent.Builder> builderAction) {

			}

			@Override
			public void fireNodeCreatedEvent(Consumer<? super NodeCreatedEvent.Builder> builderAction) {

			}

			@Override
			public void fireRelationshipCreatedEvent(Consumer<? super RelationshipCreatedEvent.Builder> builderAction) {

			}

			@Override
			public void fireLabelAddedEvent(Consumer<? super LabelAddedEvent.Builder> builderAction) {

			}
		};
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private Graph graph;
		private EventListener listener;

		public Builder graph(Graph graph) {
			this.graph = graph;
			return this;
		}

		public Builder listener(EventListener listener) {
			this.listener = listener;
			return this;
		}

		public GraphEventNotifier build() {
			return new GraphEventNotifier() {
				@Override
				public void firePropertyChangedEvent(Consumer<? super PropertyChangedEvent.Builder> builderAction) {
					val builder = PropertyChangedEvent.builder();
					builderAction.accept(builder);
					listener.propertyChanged(builder.graph(graph).build());
				}

				@Override
				public void fireNodeCreatedEvent(Consumer<? super NodeCreatedEvent.Builder> builderAction) {
					val builder = NodeCreatedEvent.builder();
					builderAction.accept(builder);
					listener.nodeCreated(builder.graph(graph).build());
				}

				@Override
				public void fireRelationshipCreatedEvent(Consumer<? super RelationshipCreatedEvent.Builder> builderAction) {
					val builder = RelationshipCreatedEvent.builder();
					builderAction.accept(builder);
					listener.relationshipCreated(builder.graph(graph).build());
				}

				@Override
				public void fireLabelAddedEvent(Consumer<? super LabelAddedEvent.Builder> builderAction) {
					val builder = LabelAddedEvent.builder();
					builderAction.accept(builder);
					listener.labelAdded(builder.graph(graph).build());
				}
			};
		}
	}
}
