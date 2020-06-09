package dev.nokee.fixtures.tasks;

import lombok.Value;
import lombok.experimental.NonFinal;

@Value
@NonFinal// TODO: correctly ignore when no test case
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

	public static WellBehavingTaskTestCase ignore() {
		return new SkippingTestCase(null, null, null);
	}

	public static class SkippingTestCase extends WellBehavingTaskTestCase {
		public SkippingTestCase(String propertyName, WellBehavingTaskTransform transform, WellBehavingTaskAssertion assertion) {
			super(propertyName, transform, assertion);
		}
	}
}
