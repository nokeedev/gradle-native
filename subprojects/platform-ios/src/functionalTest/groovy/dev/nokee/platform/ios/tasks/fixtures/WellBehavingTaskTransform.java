package dev.nokee.platform.ios.tasks.fixtures;

public interface WellBehavingTaskTransform {
	default String getDescription() {
		return "[custom transformation]";
	}

	void applyChanges(WellBehavingTaskSpec context);
}
