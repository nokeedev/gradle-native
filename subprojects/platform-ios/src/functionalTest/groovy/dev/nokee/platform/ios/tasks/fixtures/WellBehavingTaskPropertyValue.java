package dev.nokee.platform.ios.tasks.fixtures;

import lombok.Value;

public interface WellBehavingTaskPropertyValue {

	@Value(staticConstructor = "of")
	class File implements WellBehavingTaskPropertyValue {
		String value;

		@Override
		public String toString() {
			return "file('" + value + "')";
		}
	}

	@Value(staticConstructor = "of")
	class Quoted implements WellBehavingTaskPropertyValue {
		String value;

		@Override
		public String toString() {
			return "'" + value + "'";
		}
	}

	@Value(staticConstructor = "of")
	class GroovyDslExpression implements WellBehavingTaskPropertyValue {
		String value;

		@Override
		public String toString() {
			return value;
		}
	}
}
