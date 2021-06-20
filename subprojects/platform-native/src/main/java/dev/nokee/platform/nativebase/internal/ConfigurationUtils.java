package dev.nokee.platform.nativebase.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.runtime.nativebase.BinaryLinkage;
import dev.nokee.runtime.nativebase.BuildType;
import dev.nokee.runtime.nativebase.MachineArchitecture;
import dev.nokee.runtime.nativebase.TargetMachine;
import dev.nokee.runtime.nativebase.internal.ArtifactTypes;
import dev.nokee.runtime.nativebase.internal.LibraryElements;
import lombok.*;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.Attribute;
import org.gradle.api.attributes.Usage;
import org.gradle.api.model.ObjectFactory;
import org.gradle.internal.Cast;
import org.gradle.language.cpp.CppBinary;
import org.gradle.nativeplatform.Linkage;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.google.common.collect.ImmutableMap.of;
import static dev.nokee.platform.nativebase.internal.ConfigurationUtils.ConfigurationSpec.Type.*;
import static dev.nokee.runtime.nativebase.MachineArchitecture.ARCHITECTURE_ATTRIBUTE;
import static dev.nokee.runtime.nativebase.OperatingSystemFamily.OPERATING_SYSTEM_ATTRIBUTE;
import static dev.nokee.runtime.nativebase.OperatingSystemFamily.OPERATING_SYSTEM_COORDINATE_AXIS;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

public class ConfigurationUtils {
	@Getter(AccessLevel.PROTECTED) private final ObjectFactory objects;

	@Inject
	public ConfigurationUtils(ObjectFactory objects) {
		this.objects = objects;
	}

	//region Bucket
	public DescribableConfigurationAction asBucket() {
		return new DescribableConfigurationAction(ConfigurationSpec.asBucket());
	}

	public DescribableConfigurationAction asBucket(Configuration fromBucket) {
		return new DescribableConfigurationAction(ConfigurationSpec.asBucket(fromBucket));
	}
	//endregion

	//region Incoming
	public IncomingConfigurationAction asIncomingHeaderSearchPath() {
		return getObjects().newInstance(IncomingConfigurationAction.class,
			ConfigurationSpec.asIncoming().withAttributes(
				ImmutableMap.<Attribute<?>, Object>builder()
					.put(Usage.USAGE_ATTRIBUTE, getObjects().named(Usage.class, Usage.C_PLUS_PLUS_API))
					.build()));
	}

	public IncomingConfigurationAction asIncomingHeaderSearchPathFrom(Configuration... fromBuckets) {
		return getObjects().newInstance(IncomingConfigurationAction.class,
			ConfigurationSpec.asIncoming(fromBuckets).withAttributes(
				ImmutableMap.<Attribute<?>, Object>builder()
					.put(Usage.USAGE_ATTRIBUTE, getObjects().named(Usage.class, Usage.C_PLUS_PLUS_API))
					.build()));
	}

	public IncomingConfigurationAction asIncomingSwiftModuleFrom(Configuration... fromBuckets) {
		return getObjects().newInstance(IncomingConfigurationAction.class,
			ConfigurationSpec.asIncoming(fromBuckets).withAttributes(
				ImmutableMap.<Attribute<?>, Object>builder()
					.put(Usage.USAGE_ATTRIBUTE, getObjects().named(Usage.class, Usage.SWIFT_API))
					.build()));
	}

	public IncomingConfigurationAction asIncomingLinkLibrariesFrom(Configuration... fromBuckets) {
		return getObjects().newInstance(IncomingConfigurationAction.class,
			ConfigurationSpec.asIncoming(fromBuckets).withAttributes(
				ImmutableMap.<Attribute<?>, Object>builder()
					.put(Usage.USAGE_ATTRIBUTE, getObjects().named(Usage.class, Usage.NATIVE_LINK))
					.build()));
	}

	public IncomingConfigurationAction asIncomingRuntimeLibrariesFrom(Configuration... fromBucket) {
		return getObjects().newInstance(IncomingConfigurationAction.class,
			ConfigurationSpec.asIncoming(fromBucket).withAttributes(
				ImmutableMap.<Attribute<?>, Object>builder()
					.put(Usage.USAGE_ATTRIBUTE, getObjects().named(Usage.class, Usage.NATIVE_RUNTIME))
					.build()));
	}
	//endregion

	//region Outgoing
	public VariantAwareOutgoingConfigurationAction asOutgoingHeaderSearchPathFrom(Configuration... fromBuckets) {
		return getObjects().newInstance(VariantAwareOutgoingConfigurationAction.class,
			ConfigurationSpec.asOutgoing(fromBuckets).withAttributes(of(Usage.USAGE_ATTRIBUTE, getObjects().named(Usage.class, Usage.C_PLUS_PLUS_API))));
	}

	public VariantAwareOutgoingConfigurationAction asOutgoingSwiftModuleFrom(Configuration... fromBuckets) {
		return getObjects().newInstance(VariantAwareOutgoingConfigurationAction.class,
			ConfigurationSpec.asOutgoing(fromBuckets).withAttributes(of(Usage.USAGE_ATTRIBUTE, getObjects().named(Usage.class, Usage.SWIFT_API))));
	}

	public VariantAwareOutgoingConfigurationAction asOutgoingLinkLibrariesFrom(Configuration... fromBuckets) {
		return getObjects().newInstance(VariantAwareOutgoingConfigurationAction.class,
			ConfigurationSpec.asOutgoing(fromBuckets).withAttributes(of(Usage.USAGE_ATTRIBUTE, getObjects().named(Usage.class, Usage.NATIVE_LINK))));
	}

	public VariantAwareOutgoingConfigurationAction asOutgoingRuntimeLibrariesFrom(Configuration... fromBuckets) {
		return getObjects().newInstance(VariantAwareOutgoingConfigurationAction.class,
			ConfigurationSpec.asOutgoing(fromBuckets).withAttributes(of(Usage.USAGE_ATTRIBUTE, getObjects().named(Usage.class, Usage.NATIVE_RUNTIME))));
	}
	//endregion

	public static void configureAsIncoming(Configuration configuration) {
		configuration.setCanBeConsumed(false);
		configuration.setCanBeResolved(true);
	}

	public static void configureAsOutgoing(Configuration configuration) {
		configuration.setCanBeConsumed(true);
		configuration.setCanBeResolved(false);
	}

	public static void configureAsBucket(Configuration configuration) {
		configuration.setCanBeConsumed(false);
		configuration.setCanBeResolved(false);
	}

	public static class DescribableConfigurationAction implements Action<Configuration> {
		protected final ConfigurationSpec spec;

		public DescribableConfigurationAction(ConfigurationSpec spec) {
			this.spec = spec;
		}

		public DescribableConfigurationAction withDescription(String description) {
			return new DescribableConfigurationAction(spec.withDescription(description));
		}

		@Override
		public void execute(Configuration configuration) {
			spec.execute(configuration);
		}
	}

	public static class IncomingConfigurationAction extends DescribableConfigurationAction {
		@Getter(AccessLevel.PROTECTED) private final ObjectFactory objects;

		@Inject
		public IncomingConfigurationAction(ConfigurationSpec spec, ObjectFactory objects) {
			super(spec);
			this.objects = objects;
		}

		public IncomingConfigurationAction asDebug() {
			return getObjects().newInstance(IncomingConfigurationAction.class,
				spec.withAttributes(ImmutableMap.<Attribute<?>, Object>builder()
					.putAll(spec.attributes)
					.put(CppBinary.DEBUGGABLE_ATTRIBUTE, Boolean.TRUE)
					.put(CppBinary.OPTIMIZED_ATTRIBUTE, Boolean.FALSE)
					.build()));
		}

		public IncomingConfigurationAction asRelease() {
			return getObjects().newInstance(IncomingConfigurationAction.class,
				spec.withAttributes(ImmutableMap.<Attribute<?>, Object>builder()
					.putAll(spec.attributes)
					.put(CppBinary.DEBUGGABLE_ATTRIBUTE, Boolean.TRUE)
					.put(CppBinary.OPTIMIZED_ATTRIBUTE, Boolean.TRUE)
					.build()));
		}

		public IncomingConfigurationAction forTargetMachine(TargetMachine targetMachine) {
			return getObjects().newInstance(IncomingConfigurationAction.class,
				spec.withAttributes(ImmutableMap.<Attribute<?>, Object>builder()
					.putAll(spec.attributes)
					.put(OPERATING_SYSTEM_ATTRIBUTE, targetMachine.getOperatingSystemFamily())
					.put(ARCHITECTURE_ATTRIBUTE, targetMachine.getArchitecture())
					.build()));
		}

		@Override
		public IncomingConfigurationAction withDescription(String description) {
			return getObjects().newInstance(IncomingConfigurationAction.class, spec.withDescription(description));
		}
	}

	public static class VariantAwareOutgoingConfigurationAction extends DescribableConfigurationAction {
		@Getter(AccessLevel.PROTECTED) private final ObjectFactory objects;

		@Inject
		public VariantAwareOutgoingConfigurationAction(ConfigurationSpec spec, ObjectFactory objects) {
			super(spec);
			this.objects = objects;
		}

		public VariantAwareOutgoingConfigurationAction withVariant(BuildVariantInternal variant) {
			val attributes = ImmutableMap.<Attribute<?>, Object>builder().putAll(spec.attributes);

			variant.getDimensions().forEach(it -> {
				if (it.getAxis().equals(OPERATING_SYSTEM_COORDINATE_AXIS)) {
					attributes.put(OPERATING_SYSTEM_ATTRIBUTE, it.getValue());
				} else if (it.getAxis().equals(MachineArchitecture.ARCHITECTURE_COORDINATE_AXIS)) {
					attributes.put(ARCHITECTURE_ATTRIBUTE, it.getValue());
				} else if (it.getAxis().equals(BinaryLinkage.BINARY_LINKAGE_COORDINATE_AXIS)) {
					attributes.put(BinaryLinkage.BINARY_LINKAGE_ATTRIBUTE, it.getValue());
				} else if (it.getAxis().equals(BuildType.BUILD_TYPE_COORDINATE_AXIS)) {
					attributes.put(BuildType.BUILD_TYPE_ATTRIBUTE, it.getValue());
				} else {
					throw new IllegalArgumentException(String.format("Unknown dimension variant '%s'", it.toString()));
				}
			});

			return getObjects().newInstance(VariantAwareOutgoingConfigurationAction.class, spec.withAttributes(attributes.build()));
		}

		public VariantAwareOutgoingConfigurationAction withStaticLinkage() {
			return getObjects().newInstance(VariantAwareOutgoingConfigurationAction.class,
				spec.withAttributes(ImmutableMap.<Attribute<?>, Object>builder()
					.putAll(spec.attributes)
					.put(CppBinary.LINKAGE_ATTRIBUTE, Linkage.STATIC)
					.build()));
		}

		public VariantAwareOutgoingConfigurationAction withSharedLinkage() {
			return getObjects().newInstance(VariantAwareOutgoingConfigurationAction.class,
				spec.withAttributes(ImmutableMap.<Attribute<?>, Object>builder()
					.putAll(spec.attributes)
					.put(CppBinary.LINKAGE_ATTRIBUTE, Linkage.SHARED)
					.build()));
		}

		public VariantAwareOutgoingConfigurationAction asDebug() {
			return getObjects().newInstance(VariantAwareOutgoingConfigurationAction.class,
				spec.withAttributes(ImmutableMap.<Attribute<?>, Object>builder()
					.putAll(spec.attributes)
					.put(CppBinary.DEBUGGABLE_ATTRIBUTE, Boolean.TRUE)
					.put(CppBinary.OPTIMIZED_ATTRIBUTE, Boolean.FALSE)
					.build()));
		}

		public VariantAwareOutgoingConfigurationAction asRelease() {
			return getObjects().newInstance(VariantAwareOutgoingConfigurationAction.class,
				spec.withAttributes(ImmutableMap.<Attribute<?>, Object>builder()
					.putAll(spec.attributes)
					.put(CppBinary.DEBUGGABLE_ATTRIBUTE, Boolean.TRUE)
					.put(CppBinary.OPTIMIZED_ATTRIBUTE, Boolean.TRUE)
					.build()));
		}

		public VariantAwareOutgoingConfigurationAction frameworkArtifact(Object notation) {
			return getObjects().newInstance(VariantAwareOutgoingConfigurationAction.class,
				spec.withArtifacts(ImmutableList.<OutgoingArtifact>builder().
					addAll(spec.artifacts)
					.add(new OutgoingArtifact(ArtifactTypes.FRAMEWORK_TYPE, notation))
					.build())
					.withAttributes(ImmutableMap.<Attribute<?>, Object>builder()
						.putAll(spec.attributes)
						.put(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, getObjects().named(LibraryElements.class, LibraryElements.FRAMEWORK_BUNDLE))
						.build()));
		}

		public VariantAwareOutgoingConfigurationAction headerDirectoryArtifact(Object notation) {
			return getObjects().newInstance(VariantAwareOutgoingConfigurationAction.class,
				spec.withArtifacts(ImmutableList.<OutgoingArtifact>builder().
					addAll(spec.artifacts)
					.add(new OutgoingArtifact(ArtifactTypes.DIRECTORY_TYPE, notation))
					.build())
					.withAttributes(ImmutableMap.<Attribute<?>, Object>builder()
						.putAll(spec.attributes).put(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, getObjects().named(LibraryElements.class, LibraryElements.HEADERS_CPLUSPLUS))
						.build()));
		}

		public VariantAwareOutgoingConfigurationAction headerDirectoryArtifacts(Iterable<Object> notations) {
			val artifacts = ImmutableList.<OutgoingArtifact>builder().addAll(spec.artifacts);
			notations.forEach(notation -> artifacts.add(new OutgoingArtifact(ArtifactTypes.DIRECTORY_TYPE, notation)));

			return getObjects().newInstance(VariantAwareOutgoingConfigurationAction.class, spec.withArtifacts(artifacts.build())
				.withAttributes(ImmutableMap.<Attribute<?>, Object>builder()
					.putAll(spec.attributes).put(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, getObjects().named(LibraryElements.class, LibraryElements.HEADERS_CPLUSPLUS))
					.build()));
		}

		public VariantAwareOutgoingConfigurationAction sharedLibraryArtifact(Object notation) {
			return getObjects().newInstance(VariantAwareOutgoingConfigurationAction.class,
				spec.withArtifacts(ImmutableList.<OutgoingArtifact>builder().
					addAll(spec.artifacts)
					.add(new OutgoingArtifact(null, notation))
					.build()).withAttributes(ImmutableMap.<Attribute<?>, Object>builder()
						.putAll(spec.attributes).put(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, getObjects().named(LibraryElements.class, LibraryElements.DYNAMIC_LIB))
						.build()));
		}

		public VariantAwareOutgoingConfigurationAction staticLibraryArtifact(Object notation) {
			return getObjects().newInstance(VariantAwareOutgoingConfigurationAction.class,
				spec.withArtifacts(ImmutableList.<OutgoingArtifact>builder().
					addAll(spec.artifacts)
					.add(new OutgoingArtifact(null, notation))
					.build())
					.withAttributes(ImmutableMap.<Attribute<?>, Object>builder()
						.putAll(spec.attributes).put(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, getObjects().named(LibraryElements.class, LibraryElements.LINK_ARCHIVE))
						.build()));
		}

		public VariantAwareOutgoingConfigurationAction importLibraryArtifact(Object notation) {
			return getObjects().newInstance(VariantAwareOutgoingConfigurationAction.class,
				spec.withArtifacts(ImmutableList.<OutgoingArtifact>builder().
					addAll(spec.artifacts)
					.add(new OutgoingArtifact(null, notation))
					.build())
					.withAttributes(ImmutableMap.<Attribute<?>, Object>builder()
						.putAll(spec.attributes).put(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, getObjects().named(LibraryElements.class, LibraryElements.IMPORT_LIB))
						.build()));
		}

		public VariantAwareOutgoingConfigurationAction andThen(Action<Configuration> additionalAction) {
			return getObjects().newInstance(VariantAwareOutgoingConfigurationAction.class, spec.withAdditionalAction(additionalAction));
		}

		@Override
		public VariantAwareOutgoingConfigurationAction withDescription(String description) {
			return getObjects().newInstance(VariantAwareOutgoingConfigurationAction.class, spec.withDescription(description));
		}

		public VariantAwareOutgoingConfigurationAction forTargetMachine(TargetMachine targetMachine) {
			return getObjects().newInstance(VariantAwareOutgoingConfigurationAction.class,
				spec.withAttributes(ImmutableMap.<Attribute<?>, Object>builder()
					.putAll(spec.attributes)
					.put(OPERATING_SYSTEM_ATTRIBUTE, targetMachine.getOperatingSystemFamily())
					.put(ARCHITECTURE_ATTRIBUTE, targetMachine.getArchitecture())
					.build()));
		}
	}

	@Value
	public static class OutgoingArtifact {
		String type;
		Object notation;
	}

	@Value
	public static class ConfigurationSpec implements Action<Configuration> {
		Type type;
		List<Configuration> fromBuckets;
		@With Map<Attribute<?>, Object> attributes;
		@With List<OutgoingArtifact> artifacts;
		@With Action<Configuration> additionalAction;
		@With String description;

		enum Type {
			BUCKET(ConfigurationUtils::configureAsBucket), INCOMING(ConfigurationUtils::configureAsIncoming), OUTGOING(ConfigurationUtils::configureAsOutgoing);

			private final Consumer<Configuration> action;

			Type(Consumer<Configuration> action) {
				this.action = action;
			}

			void configure(Configuration configuration) {
				action.accept(configuration);
			}
		}

		static ConfigurationSpec asBucket() {
			return new ConfigurationSpec(BUCKET, ImmutableList.of(), emptyMap(), emptyList(), it -> {}, null);
		}

		static ConfigurationSpec asBucket(Configuration fromBucket) {
			return new ConfigurationSpec(BUCKET, ImmutableList.of(fromBucket), emptyMap(), emptyList(), it -> {}, null);
		}

		static ConfigurationSpec asOutgoing(Configuration... fromBuckets) {
			return new ConfigurationSpec(OUTGOING, ImmutableList.copyOf(fromBuckets), emptyMap(), emptyList(), it -> {}, null);
		}

		static ConfigurationSpec asIncoming() {
			return new ConfigurationSpec(INCOMING, ImmutableList.of(), emptyMap(),emptyList(), it -> {}, null);
		}

		static ConfigurationSpec asIncoming(Configuration... fromBucket) {
			return new ConfigurationSpec(INCOMING, ImmutableList.copyOf(fromBucket), emptyMap(), emptyList(), it -> {}, null);
		}

		@Override
		public void execute(Configuration configuration) {
			type.configure(configuration);
			configuration.setExtendsFrom(fromBuckets);

			if (description != null) {
				configuration.setDescription(description);
			}

			attributes.forEach((key, value) -> configuration.getAttributes().attribute(Cast.uncheckedNonnullCast(key), Cast.uncheckedNonnullCast(value)));

			// TODO: Remove these ifs with better modeling
			for (OutgoingArtifact artifact : artifacts) {
				configuration.getOutgoing().artifact(artifact.notation, it -> {
					if (artifact.type != null) {
						it.setType(artifact.type);
					}
				});
			}

			additionalAction.execute(configuration);
		}
	}
}
