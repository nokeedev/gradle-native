package dev.nokee.buildadapter.cmake.internal.fileapi;

import lombok.Value;

import java.util.List;

@Value
public class CodeModel {
	List<Configuration> configurations;

	@Value
	public static class Configuration {
		String name;
		List<Project> projects;
		List<TargetReference> targets;

		@Value
		public static class Project {
			String name;
			List<Integer> targetIndexes;
		}

		@Value
		public static class TargetReference {
			String jsonFile;
			String name;
		}
	}
}
