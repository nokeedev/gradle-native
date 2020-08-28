package dev.nokee.buildadapter.cmake.internal.fileapi;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.With;

import javax.annotation.Nullable;
import java.util.List;

import static java.util.Collections.emptyList;

@Value
@Builder
@AllArgsConstructor
public class CodeModelTarget {
	String id;

	String name;

	// Possible values: STATIC_LIBRARY
	String type;

	boolean isGeneratorProvided;

	@Nullable
	List<Artifact> artifacts;

	@Nullable
	List<CompileGroup> compileGroups;

	@Nullable
	@With List<Dependency> dependencies;

	List<Source> sources;

	public CodeModelTarget(String id, String name, String type) {
		this(id, name, type, false, emptyList(), emptyList(), emptyList(), emptyList());
	}

	public List<Artifact> getArtifacts() {
		return useEmptyListIfNull(artifacts);
	}

	public List<CompileGroup> getCompileGroups() {
		return useEmptyListIfNull(compileGroups);
	}

	public List<Dependency> getDependencies() {
		return useEmptyListIfNull(dependencies);
	}

	public List<Source> getSources() {
		return useEmptyListIfNull(sources);
	}

	private static <T> List<T> useEmptyListIfNull(List<T> list) {
		if (list == null) {
			return emptyList();
		}
		return list;
	}

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
