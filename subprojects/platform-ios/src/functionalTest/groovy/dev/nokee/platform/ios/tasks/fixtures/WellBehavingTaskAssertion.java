package dev.nokee.platform.ios.tasks.fixtures;

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
