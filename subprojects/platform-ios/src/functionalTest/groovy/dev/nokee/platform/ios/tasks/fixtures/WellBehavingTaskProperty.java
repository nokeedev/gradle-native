package dev.nokee.platform.ios.tasks.fixtures;

import lombok.Value;

@Value
public class WellBehavingTaskProperty {
	String name;
	WellBehavingTaskTestSuite.UpToDateTestSuite upToDateChecks;
	WellBehavingTaskTestSuite.CacheableTestSuite cachingChecks;
	WellBehavingTaskTestSuite.IncrementalTestSuite incrementalChecks;
	String initialState;

	String configure() {
		return initialState;
	}

	public static WellBehavingTaskProperty ignored(String propertyName, String initialState) {
		return new WellBehavingTaskProperty(propertyName, WellBehavingTaskTestSuite.UpToDateTestSuite.empty(), WellBehavingTaskTestSuite.CacheableTestSuite.empty(), WellBehavingTaskTestSuite.IncrementalTestSuite.empty(), initialState);
	}
}
