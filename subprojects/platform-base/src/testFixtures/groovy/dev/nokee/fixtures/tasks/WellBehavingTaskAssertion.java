package dev.nokee.fixtures.tasks;

public interface WellBehavingTaskAssertion {
	default String getDescription() {
		return "[custom assertion]";
	}

	void assertState(WellBehavingTaskSpec context);

	default WellBehavingTaskAssertion and(WellBehavingTaskAssertion assertion) {
		return new WellBehavingTaskAssertion() {
			@Override
			public String getDescription() {
				return WellBehavingTaskAssertion.this.getDescription() + " and " + assertion.getDescription();
			}

			@Override
			public void assertState(WellBehavingTaskSpec context) {
				WellBehavingTaskAssertion.this.assertState(context);
				assertion.assertState(context);
			}
		};
	}
}
