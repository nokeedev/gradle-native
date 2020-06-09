package dev.nokee.fixtures.tasks;

import lombok.Value;

public interface WellBehavingTaskPropertyMutator {
	String configureWith(WellBehavingTaskPropertyValue value);

	@Value
	class Property implements WellBehavingTaskPropertyMutator {
		String propertyName;

		@Override
		public String configureWith(WellBehavingTaskPropertyValue value) {
			return propertyName + " = " + value.toString();
		}
	}

	@Value
	class FileCollection implements WellBehavingTaskPropertyMutator {
		String propertyName;

		@Override
		public String configureWith(WellBehavingTaskPropertyValue value) {
			return propertyName + ".from(" + value.toString() + ")";
		}
	}

}
