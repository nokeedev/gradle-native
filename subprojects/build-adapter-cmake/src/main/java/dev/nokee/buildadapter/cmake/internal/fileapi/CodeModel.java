package dev.nokee.buildadapter.cmake.internal.fileapi;

import lombok.Value;
import lombok.With;

import java.util.List;
import java.util.stream.Collectors;

@Value
public class CodeModel {
	@With List<Configuration> configurations;

	@Value
	public static class Configuration {
		String name;
		List<Project> projects;
		List<TargetReference> targets;

		@Value
		public static class Project {
			String name;
			@With List<Integer> targetIndexes;

			public List<TargetReference> getTargets(Configuration configuration) {
				return targetIndexes.stream().map(configuration.targets::get).collect(Collectors.toList());
			}
		}

		@Value
		public static class TargetReference {
			String jsonFile;
			String name;
			String id;
			@With Integer projectIndex;

			public Project getProject(Configuration configuration) {
				return configuration.projects.get(projectIndex);
			}
		}
	}
}
