package dev.nokee.platform.ios.tasks.fixtures;

import lombok.Value;

@Value
public class WellBehavingTaskTestCase {
	String propertyName;
	WellBehavingTaskTransform transform;
	WellBehavingTaskAssertion assertion;

	public String getDescription() {
		return transform.getDescription() + " assert that " + assertion.getDescription() + " [" + propertyName + "]";
	}

	public void applyChanges(WellBehavingTaskSpec context) {
		transform.applyChanges(context);
	}

	public void assertState(WellBehavingTaskSpec context) {
		assertion.assertState(context);
	}
}
