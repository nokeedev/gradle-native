package dev.gradleplugins.documentationkit;

import lombok.Data;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.Registry;
import org.simpleframework.xml.convert.RegistryStrategy;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.Strategy;
import org.simpleframework.xml.stream.CamelCaseStyle;
import org.simpleframework.xml.stream.Format;
import org.simpleframework.xml.stream.Style;

import javax.annotation.Nullable;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public final class DependencySerializer {
	private final DependencyHandler dependencyFactory;

	public DependencySerializer(DependencyHandler dependencyFactory) {
		this.dependencyFactory = dependencyFactory;
	}

	public void serialize(List<Dependency> dependencies, File outputFile) throws Exception {
		Style style = new CamelCaseStyle(false, false);
		Format format = new Format(3, "<?xml version=\"1.0\" encoding=\"utf-8\"?>", style);
		Registry registry = new Registry();
		Strategy strategy = new RegistryStrategy(registry);
		Serializer serializer = new Persister(strategy, format);
		serializer.write(Dependencies.of(dependencies), outputFile);
	}

	public List<Dependency> deserialize(File inputFile) throws Exception {
		Style style = new CamelCaseStyle(false, false);
		Format format = new Format(3, "<?xml version=\"1.0\" encoding=\"utf-8\"?>", style);
		Registry registry = new Registry();
		Strategy strategy = new RegistryStrategy(registry);
		Serializer serializer = new Persister(strategy, format);

		return serializer.read(Dependencies.class, inputFile).get().stream()
			.map(it -> dependencyFactory.create(String.format("%s:%s:%s", it.getGroupId(), it.getArtifactId(), it.getVersion())))
			.collect(Collectors.toList());
	}

	@Root
	public static final class Dependencies {
		@ElementList(inline = true, type = Dependency.class)
		private List<Dependency> dependencies;

		public Dependencies() {}

		public Dependencies(List<Dependency> dependencies) {
			this.dependencies = dependencies;
		}

		public static Dependencies of(List<org.gradle.api.artifacts.Dependency> dependencies) {
			return new Dependencies(dependencies.stream().map(it -> new Dependency(it.getGroup(), it.getName(), it.getVersion())).collect(Collectors.toList()));
		}

		public List<Dependency> get() {
			return dependencies;
		}

		@Root
		@Data
		public static final class Dependency {
			@Element
			private String groupId;
			@Element
			private String artifactId;

			@Element
			@Nullable
			private String version;

			public Dependency() {

			}

			public Dependency(String groupId, String artifactId, @Nullable String version) {
				this.groupId = groupId;
				this.artifactId = artifactId;
				this.version = version;
			}

//			public String getGroupId() {
//				return groupId;
//			}
//
//			public String getArtifactId() {
//				return artifactId;
//			}
//
//			@Nullable
//			public String getVersion() {
//				return version;
//			}
		}
	}
}
