package dev.nokee.buildadapter.cmake.internal.fileapi;

import lombok.Value;

import javax.annotation.Nullable;
import java.util.List;

@Value
public class CodeModelTarget {
	String id;

	String name;

	// Possible values: STATIC_LIBRARY
	String type;

	@Nullable
	List<Artifact> artifacts;

	@Nullable
	List<CompileGroup> compileGroups;

	@Nullable
	List<Dependency> dependencies;

	List<Source> sources;

	@Value
	public static class Artifact {
		String path;
	}

	@Value
	public static class CompileGroup {
		@Nullable
		List<Include> includes;

		@Value
		public static class Include {
			String path;
		}
	}

	@Value
	public static class Dependency {
		String id;
	}

	@Value
	public static class Source {
		String path;
	}
}
