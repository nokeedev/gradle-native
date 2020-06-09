package dev.nokee.fixtures.tasks;

import com.google.common.collect.ImmutableList;
import lombok.Value;

import java.util.List;

public interface WellBehavingTaskTestSuite {
	List<WellBehavingTaskTestCase> getTestCases();

	@Value
	class CacheableTestSuite implements WellBehavingTaskTestSuite {
		List<WellBehavingTaskTestCase> testCases;

		public static CacheableTestSuite empty() {
			return new CacheableTestSuite(ImmutableList.of());
		}
	}

	@Value
	class IncrementalTestSuite implements WellBehavingTaskTestSuite {
		List<WellBehavingTaskTestCase> testCases;

		public static IncrementalTestSuite empty() {
			return new IncrementalTestSuite(ImmutableList.of());
		}
	}

	@Value
	class UpToDateTestSuite implements WellBehavingTaskTestSuite {
		List<WellBehavingTaskTestCase> testCases;

		public static UpToDateTestSuite empty() {
			return new UpToDateTestSuite(ImmutableList.of());
		}
	}
}
