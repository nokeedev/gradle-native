package dev.nokee.fixtures.tasks;

public interface WellBehavingTaskTransform {
	default String getDescription() {
		return "[custom transformation]";
	}

	void applyChanges(WellBehavingTaskSpec context);
}
