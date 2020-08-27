package dev.nokee.buildadapter.cmake.internal.fileapi;

import lombok.Value;

import java.util.List;

@Value
public class CodeModel {
	List<Configuration> configurations;

	@Value
	public static class Configuration {
		String name;
		List<Target> targets;

		@Value
		public static class Target {
			String jsonFile;
			String name;
		}
	}
}
