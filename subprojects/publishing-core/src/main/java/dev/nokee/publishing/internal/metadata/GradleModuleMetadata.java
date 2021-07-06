package dev.nokee.publishing.internal.metadata;

import com.google.gson.annotations.SerializedName;
import lombok.*;

import javax.annotation.Nullable;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GradleModuleMetadata {
	@NonNull String formatVersion;
	@Nullable Component component;
	@Nullable CreatedBy createdBy;
	@NonNull List<Variant> variants;

	public Optional<Component> getComponent() {
		return Optional.ofNullable(component);
	}

	public Optional<CreatedBy> getCreatedBy() {
		return Optional.ofNullable(createdBy);
	}

	@Value
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	public static class Component {
		String group;
		String module;
		String version;
		List<Attribute> attributes;

		public static Component ofComponent(String group, String module, String version) {
			return new Component(group, module, version, Collections.emptyList());
		}

		public static Component ofComponent(String group, String module, String version, List<Attribute> attributes) {
			return new Component(group, module, version, attributes);
		}
	}

	@Value
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	public static class CreatedBy {
		@Nullable Gradle gradle;

		public Optional<Gradle> getGradle() {
			return Optional.ofNullable(gradle);
		}

		public static CreatedBy ofGradle(String version, String buildId) {
			return new CreatedBy(Gradle.of(version, buildId));
		}
	}

	@Value(staticConstructor = "of")
	public static class Gradle {
		String version;
		String buildId;
	}

	public interface Variant {}

	@Value
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	public static class LocalVariant implements Variant {
		String name;
		List<Attribute> attributes;
		List<Capability> capabilities;
		List<Dependency> dependencies;
		List<DependencyConstraint> dependencyConstraints;
		List<File> files;

		public static Builder builder() {
			return new Builder();
		}

		public static final class Builder {
			private String name;
			private final List<Attribute> attributes = new ArrayList<>();
			private final List<Capability> capabilities = new ArrayList<>();
			private final List<Dependency> dependencies = new ArrayList<>();
			private final List<DependencyConstraint> dependencyConstraints = new ArrayList<>();
			private final List<File> files = new ArrayList<>();

			public Builder name(String name) {
				this.name = name;
				return this;
			}

			public Builder attribute(Attribute attribute) {
				this.attributes.add(attribute);
				return this;
			}

			public Builder capability(Capability capability) {
				this.capabilities.add(capability);
				return this;
			}

			public Builder dependency(Dependency dependency) {
				this.dependencies.add(dependency);
				return this;
			}

			public Builder dependency(Consumer<? super Dependency.Builder> builderConsumer) {
				val builder = Dependency.builder();
				builderConsumer.accept(builder);
				return dependency(builder.build());
			}

			public Builder dependencyConstraint(DependencyConstraint dependencyConstraint) {
				this.dependencyConstraints.add(dependencyConstraint);
				return this;
			}

			public Builder dependencyConstraint(Consumer<? super DependencyConstraint.Builder> builderConsumer) {
				val builder = DependencyConstraint.builder();
				builderConsumer.accept(builder);
				return dependencyConstraint(builder.build());
			}

			public Builder file(File file) {
				this.files.add(file);
				return this;
			}

			public Builder file(Consumer<? super File.Builder> builderConsumer) {
				val builder = File.builder();
				builderConsumer.accept(builder);
				return file(builder.build());
			}

			public LocalVariant build() {
				return new LocalVariant(name, attributes, capabilities, dependencies, dependencyConstraints, files);
			}
		}
	}

	@Value
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	public static class RemoteVariant implements Variant {
		String name;
		List<Attribute> attributes;
		@SerializedName(value = "available-at")
		@Nullable AvailableAt availableAt;
		List<Capability> capabilities;

		public static Builder builder() {
			return new Builder();
		}

		public static final class Builder {
			private String name;
			private final List<Attribute> attributes = new ArrayList<>();
			private AvailableAt availableAt;
			private final List<Capability> capabilities = new ArrayList<>();

			public Builder name(String name) {
				this.name = name;
				return this;
			}

			public Builder attribute(Attribute attribute) {
				this.attributes.add(attribute);
				return this;
			}

			public Builder availableAt(AvailableAt availableAt) {
				this.availableAt = availableAt;
				return this;
			}

			public Builder availableAt(Consumer<? super AvailableAt.Builder> builderConsumer) {
				val builder = AvailableAt.builder();
				builderConsumer.accept(builder);
				return availableAt(builder.build());
			}

			public Builder capability(Capability capability) {
				this.capabilities.add(capability);
				return this;
			}

			public RemoteVariant build() {
				return new RemoteVariant(name, attributes, availableAt, capabilities);
			}
		}
	}

	@Value
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	public static class Dependency {
		String group;
		String name;
		@Nullable Version version;
		Set<Exclude> excludes;
		@Nullable String reason;
		List<Attribute> attributes;
		List<Capability> requestedCapabilities;

		public Optional<Version> getVersion() {
			return Optional.ofNullable(version);
		}

		public Optional<String> getReason() {
			return Optional.ofNullable(reason);
		}

		public static Builder builder() {
			return new Builder();
		}

		public static final class Builder {
			private String group;
			private String name;
			private Version version;
			private final Set<Exclude> excludes = new HashSet<>();
			private String reason;
			private final List<Attribute> attributes = new ArrayList<>();
			private final List<Capability> requestedCapabilities = new ArrayList<>();

			public Builder group(String group) {
				this.group = group;
				return this;
			}

			public Builder name(String name) {
				this.name = name;
				return this;
			}

			public Builder version(Version version) {
				this.version = version;
				return this;
			}

			public Builder exclude(Exclude exclude) {
				this.excludes.add(exclude);
				return this;
			}

			public Builder reason(String reason) {
				this.reason = reason;
				return this;
			}

			public Builder attribute(Attribute attribute) {
				this.attributes.add(attribute);
				return this;
			}

			public Builder requestedCapability(Capability capability) {
				this.requestedCapabilities.add(capability);
				return this;
			}

			public Dependency build() {
				return new Dependency(group, name, version, excludes, reason, attributes, requestedCapabilities);
			}
		}
	}

	@Value
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	public static class Exclude {
		String group;
		String module;

		public static Exclude ofExclude(String group, String module) {
			return new Exclude(group, module);
		}
	}

	@Value
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	public static class Attribute {
		String name;
		Object value;

		public static Attribute ofAttribute(String name, Object value) {
			return new Attribute(name, value);
		}
	}

	@Value
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	public static class Capability {
		String group;
		String name;
		@Nullable String version;

		public Optional<String> getVersion() {
			return Optional.ofNullable(version);
		}

		public static Capability ofCapability(String group, String name) {
			return new Capability(group, name, null);
		}

		public static Capability ofCapability(String group, String name, String version) {
			return new Capability(group, name, version);
		}
	}

	@Value
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	public static class Version {
		@Nullable String requires;
		@Nullable String strictly;
		@Nullable String preferred;
		List<String> rejects;

		public Optional<String> getRequires() {
			return Optional.ofNullable(requires);
		}

		public Optional<String> getStrictly() {
			return Optional.ofNullable(strictly);
		}

		public Optional<String> getPreferred() {
			return Optional.ofNullable(preferred);
		}

		public static Version requires(String v) {
			return builder().requires(v).build();
		}

		public static Version strictly(String v) {
			return builder().strictly(v).build();
		}

		public static Version preferred(String v) {
			return builder().preferred(v).build();
		}

		public static Version rejects(String... v) {
			return builder().rejects(Arrays.asList(v)).build();
		}

		public static Builder builder() {
			return new Builder();
		}

		public static final class Builder {
			private String requires;
			private String strictly;
			private String preferred;
			private final List<String> rejects = new ArrayList<>();

			public Builder requires(String requires) {
				this.requires = requires;
				return this;
			}

			public Builder strictly(String strictly) {
				this.strictly = strictly;
				return this;
			}

			public Builder preferred(String preferred) {
				this.preferred = preferred;
				return this;
			}

			public Builder reject(String reject) {
				this.rejects.add(reject);
				return this;
			}

			public Builder rejects(List<String> rejects) {
				this.rejects.clear();
				this.rejects.addAll(rejects);
				return this;
			}

			public Version build() {
				return new Version(requires, strictly, preferred, rejects);
			}
		}
	}

	@Value
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	public static class DependencyConstraint {
		String group;
		String module;
		@Nullable Version version;
		List<Attribute> attributes;
		@Nullable String reason;

		public static DependencyConstraint ofDependencyConstraint(String group, String module) {
			return builder().group(group).module(module).build();
		}

		public static Builder builder() {
			return new Builder();
		}

		public static final class Builder {
			private String group;
			private String module;
			private Version version;
			private final List<Attribute> attributes = new ArrayList<>();
			private String reason;

			public Builder group(String group) {
				this.group = group;
				return this;
			}

			public Builder module(String module) {
				this.module = module;
				return this;
			}

			public Builder version(Version version) {
				this.version = version;
				return this;
			}

			public Builder attribute(Attribute attribute) {
				this.attributes.add(attribute);
				return this;
			}

			public Builder reason(String reason) {
				this.reason = reason;
				return this;
			}

			public DependencyConstraint build() {
				return new DependencyConstraint(group, module, version, attributes, reason);
			}
		}
	}


	@Value
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	public static class File {
		String name;
		String url;
		long size;
		String sha1;
		String md5;

		public static Builder builder() {
			return new Builder();
		}

		public static final class Builder {
			private String name;
			private String url;
			private long sizeInByte;
			private String sha1;
			private String md5;

			public Builder name(String name) {
				this.name = name;
				return this;
			}

			public Builder url(String url) {
				this.url = url;
				return this;
			}

			public Builder size(long sizeInByte) {
				this.sizeInByte = sizeInByte;
				return this;
			}

			public Builder sha1(String sha1) {
				this.sha1 = sha1;
				return this;
			}

			public Builder md5(String md5) {
				this.md5 = md5;
				return this;
			}

			public File build() {
				return new File(name, url, sizeInByte, sha1, md5);
			}
		}
	}

	@Value
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	public static class AvailableAt {
		String url;
		String group;
		String module;
		String version;

		public static Builder builder() {
			return new Builder();
		}

		public static final class Builder {
			private String url;
			private String group;
			private String module;
			private String version;

			public Builder url(String url) {
				this.url = requireNonNull(url);
				return this;
			}

			public Builder group(String group) {
				this.group = requireNonNull(group);
				return this;
			}

			public Builder module(String module) {
				this.module = requireNonNull(module);
				return this;
			}

			public Builder version(String version) {
				this.version = requireNonNull(version);
				return this;
			}

			public AvailableAt build() {
				return new AvailableAt(url, group, module, version);
			}
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private String formatVersion;
		private Component component;
		private CreatedBy createdBy;
		private final List<Variant> variants = new ArrayList<>();

		public Builder formatVersion(String formatVersion) {
			this.formatVersion = formatVersion;
			return this;
		}

		public Builder component(Component component) {
			this.component = component;
			return this;
		}

		public Builder createdBy(CreatedBy createdBy) {
			this.createdBy = createdBy;
			return this;
		}

		public Builder variant(Variant variant) {
			this.variants.add(variant);
			return this;
		}

		public Builder localVariant(Consumer<? super LocalVariant.Builder> builderConsumer) {
			val builder = LocalVariant.builder();
			builderConsumer.accept(builder);
			return variant(builder.build());
		}

		public Builder remoteVariant(Consumer<? super RemoteVariant.Builder> builderConsumer) {
			val builder = RemoteVariant.builder();
			builderConsumer.accept(builder);
			return variant(builder.build());
		}

		public GradleModuleMetadata build() {
			return new GradleModuleMetadata(formatVersion, component, createdBy, variants);
		}
	}

	public static GradleModuleMetadataWriter newWriter(java.io.File file) throws FileNotFoundException {
		return new GradleModuleMetadataWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));
	}

	public static void withWriter(java.io.File file, Consumer<? super GradleModuleMetadataWriter> action) throws IOException {
		try (val writer = newWriter(file)) {
			action.accept(writer);
		}
	}
}

