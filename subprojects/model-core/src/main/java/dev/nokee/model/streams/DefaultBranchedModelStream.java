package dev.nokee.model.streams;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

final class DefaultBranchedModelStream<T> implements BranchedModelStream<T> {
	private final ImmutableMap.Builder<String, ModelStream<T>> builder = ImmutableMap.builder();
	private int i = 0;
	private final ModelStream<T> parentStream;
	private Predicate<T> defaultBranchPredicate = Predicates.alwaysTrue();
	private boolean terminated = false;

	public DefaultBranchedModelStream(ModelStream<T> parentStream) {
		this.parentStream = parentStream;
	}

	@Override
	public BranchedModelStream<T> branch(Predicate<? super T> predicate) {
		return doBranch(predicate, withoutSpec());
	}

	@Override
	public BranchedModelStream<T> branch(Predicate<? super T> predicate, Branched<T> branched) {
		return doBranch(predicate, withSpec(branched));
	}

	@Override
	public Map<String, ModelStream<T>> defaultBranch() {
		return doDefaultBranch(withoutSpec());
	}

	@Override
	public Map<String, ModelStream<T>> defaultBranch(Branched<T> branched) {
		return doDefaultBranch(withSpec(branched));
	}

	private BiConsumer<String, Predicate<? super T>> withoutSpec() {
		return (name, branchPredicate) -> builder.put(name, parentStream.filter(branchPredicate));
	}

	private BiConsumer<String, Predicate<? super T>> withSpec(Branched<T> branched) {
		return (name, branchPredicate) -> ((Branches.AbstractBranched<T>) branched).process(name, parentStream.filter(branchPredicate))
			.ifPresent(builder::put);
	}

	@Override
	public Map<String, ModelStream<T>> noDefaultBranch() {
		return doDefaultBranch(nothing());
	}

	private String nextDefaultName() {
		return String.valueOf(++i);
	}

	private BranchedModelStream<T> doBranch(Predicate<? super T> predicate, BiConsumer<? super String, ? super Predicate<? super T>> action) {
		assertNotTerminated();
		action.accept(nextDefaultName(), defaultBranchPredicate.and(predicate));
		defaultBranchPredicate = defaultBranchPredicate.and(predicate.negate());
		return this;
	}

	private Map<String, ModelStream<T>> doDefaultBranch(BiConsumer<? super String, ? super Predicate<? super T>> action) {
		assertNotTerminated();
		action.accept("0", defaultBranchPredicate);
		terminated = true;
		return builder.build();
	}

	private static <T> BiConsumer<String, Predicate<? super T>> nothing() {
		return (a, b) -> {};
	}

	private void assertNotTerminated() {
		if (terminated) {
			throw new RuntimeException("Branching configuration terminated");
		}
	}
}
