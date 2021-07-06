package dev.nokee.model.core;

class CustomNodePredicateTest implements NodePredicateAllTester {
	@Override
	public NodePredicate<Object> createSubject() {
		return new TestPredicate<>();
	}

	@Override
	public <T> NodePredicate<T> createSubject(ModelSpec<T> spec) {
		return new TestPredicateSpec<>(spec);
	}

	// By default NodePredicate implementation behave like NodePredicates.all()
	static final class TestPredicate<T> implements NodePredicate<T> {}

	// By default, if a NodePredicate implementation is a ModelSpec, it behave like NodePredicates.all(this)
	static final class TestPredicateSpec<T> implements NodePredicate<T>, ModelSpec<T> {
		private final ModelSpec<T> delegate;

		TestPredicateSpec(ModelSpec<T> delegate) {
			this.delegate = delegate;
		}

		@Override
		public boolean isSatisfiedBy(ModelProjection node) {
			return delegate.isSatisfiedBy(node);
		}

		@Override
		public Class<T> getProjectionType() {
			return delegate.getProjectionType();
		}
	}
}
