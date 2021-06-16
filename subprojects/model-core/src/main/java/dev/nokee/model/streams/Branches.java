package dev.nokee.model.streams;

import javax.annotation.Nullable;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

final class Branches {
	private Branches() {}

	static final class Named<T> extends AbstractBranched<T> {
		public Named(String name) {
			super(name);
		}

		@Override
		Optional<Map.Entry<String, ModelStream<T>>> process(String defaultName, ModelStream<T> stream) {
			return Optional.of(new AbstractMap.SimpleImmutableEntry<>(getName().orElse(defaultName), stream));
		}
	}

	static final class Consumed<T> extends AbstractBranched<T> {
		private final Consumer<? super ModelStream<T>> chain;

		public Consumed(Consumer<? super ModelStream<T>> chain) {
			this.chain = requireNonNull(chain);
		}

		@Override
		Optional<Map.Entry<String, ModelStream<T>>> process(String defaultName, ModelStream<T> stream) {
			chain.accept(stream);
			return Optional.of(new AbstractMap.SimpleImmutableEntry<>(getName().orElse(defaultName), stream));
		}
	}

	static final class Transformed<T> extends AbstractBranched<T> {
		private final Function<? super ModelStream<T>, ? extends ModelStream<T>> chain;

		public Transformed(Function<? super ModelStream<T>, ? extends ModelStream<T>> chain) {
			this.chain = requireNonNull(chain);
		}

		@Override
		Optional<Map.Entry<String, ModelStream<T>>> process(String defaultName, ModelStream<T> stream) {
			return Optional.ofNullable(chain.apply(stream)).map(it -> new AbstractMap.SimpleImmutableEntry<>(getName().orElse(defaultName), stream));
		}
	}

	abstract static class AbstractBranched<T> implements Branched<T> {
		@Nullable
		private String name;

		protected AbstractBranched() {
			this.name = null;
		}

		protected AbstractBranched(String name) {
			this.name = requireNonNull(name);
		}

		@Override
		public AbstractBranched<T> withName(String name) {
			this.name = requireNonNull(name);
			return this;
		}

		protected Optional<String> getName() {
			return Optional.ofNullable(name);
		}

		abstract Optional<Map.Entry<String, ModelStream<T>>> process(String defaultName, ModelStream<T> stream);
	}
}
