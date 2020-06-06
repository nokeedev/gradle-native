package dev.nokee.platform.nativebase.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import dev.nokee.runtime.nativebase.internal.ArtifactTypes;
import dev.nokee.runtime.nativebase.internal.DefaultTargetMachine;
import dev.nokee.runtime.nativebase.internal.LibraryElements;
import lombok.Value;
import lombok.With;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.Attribute;
import org.gradle.api.attributes.Usage;
import org.gradle.api.model.ObjectFactory;
import org.gradle.internal.Cast;
import org.gradle.language.cpp.CppBinary;
import org.gradle.nativeplatform.Linkage;
import org.gradle.nativeplatform.MachineArchitecture;
import org.gradle.nativeplatform.OperatingSystemFamily;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.google.common.collect.ImmutableMap.of;
import static dev.nokee.platform.nativebase.internal.ConfigurationUtils.ConfigurationSpec.Type.*;
import static java.util.Collections.emptyMap;

public abstract class ConfigurationUtils {
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

	@Inject
	protected abstract ObjectFactory getObjects();

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

	public static abstract class IncomingConfigurationAction extends DescribableConfigurationAction {
		@Inject
		public IncomingConfigurationAction(ConfigurationSpec spec) {
			super(spec);
		}

		@Inject
		protected abstract ObjectFactory getObjects();

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

		public IncomingConfigurationAction forTargetMachine(DefaultTargetMachine targetMachine) {
			return getObjects().newInstance(IncomingConfigurationAction.class,
				spec.withAttributes(ImmutableMap.<Attribute<?>, Object>builder()
					.putAll(spec.attributes)
					.put(OperatingSystemFamily.OPERATING_SYSTEM_ATTRIBUTE, getObjects().named(OperatingSystemFamily.class, targetMachine.getOperatingSystemFamily().getName()))
					.put(MachineArchitecture.ARCHITECTURE_ATTRIBUTE, getObjects().named(MachineArchitecture.class, targetMachine.getArchitecture().getName()))
					.build()));
		}

		@Override
		public IncomingConfigurationAction withDescription(String description) {
			return getObjects().newInstance(IncomingConfigurationAction.class, spec.withDescription(description));
		}
	}

	public static abstract class VariantAwareOutgoingConfigurationAction extends DescribableConfigurationAction {

		@Inject
		public VariantAwareOutgoingConfigurationAction(ConfigurationSpec spec) {
			super(spec);
		}

		@Inject
		protected abstract ObjectFactory getObjects();

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
				spec.withArtifact(new OutgoingArtifact(ArtifactTypes.FRAMEWORK_TYPE, notation))
					.withAttributes(ImmutableMap.<Attribute<?>, Object>builder()
						.putAll(spec.attributes)
						.put(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, getObjects().named(LibraryElements.class, LibraryElements.FRAMEWORK_BUNDLE))
						.build()));
		}

		public VariantAwareOutgoingConfigurationAction headerDirectoryArtifact(Object notation) {
			return getObjects().newInstance(VariantAwareOutgoingConfigurationAction.class,
				spec.withArtifact(new OutgoingArtifact(ArtifactTypes.DIRECTORY_TYPE, notation))
					.withAttributes(ImmutableMap.<Attribute<?>, Object>builder()
						.putAll(spec.attributes).put(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, getObjects().named(LibraryElements.class, LibraryElements.HEADERS_CPLUSPLUS))
						.build()));
		}

		public VariantAwareOutgoingConfigurationAction sharedLibraryArtifact(Object notation) {
			return getObjects().newInstance(VariantAwareOutgoingConfigurationAction.class,
				spec.withArtifact(new OutgoingArtifact(null, notation))
					.withAttributes(ImmutableMap.<Attribute<?>, Object>builder()
						.putAll(spec.attributes).put(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, getObjects().named(LibraryElements.class, LibraryElements.DYNAMIC_LIB))
						.build()));
		}

		public VariantAwareOutgoingConfigurationAction staticLibraryArtifact(Object notation) {
			return getObjects().newInstance(VariantAwareOutgoingConfigurationAction.class,
				spec.withArtifact(new OutgoingArtifact(null, notation))
					.withAttributes(ImmutableMap.<Attribute<?>, Object>builder()
						.putAll(spec.attributes).put(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, getObjects().named(LibraryElements.class, LibraryElements.LINK_ARCHIVE))
						.build()));
		}

		public VariantAwareOutgoingConfigurationAction importLibraryArtifact(Object notation) {
			return getObjects().newInstance(VariantAwareOutgoingConfigurationAction.class,
				spec.withArtifact(new OutgoingArtifact(null, notation))
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

		public VariantAwareOutgoingConfigurationAction forTargetMachine(DefaultTargetMachine targetMachine) {
			return getObjects().newInstance(VariantAwareOutgoingConfigurationAction.class,
				spec.withAttributes(ImmutableMap.<Attribute<?>, Object>builder()
					.putAll(spec.attributes)
					.put(OperatingSystemFamily.OPERATING_SYSTEM_ATTRIBUTE, getObjects().named(OperatingSystemFamily.class, targetMachine.getOperatingSystemFamily().getName()))
					.put(MachineArchitecture.ARCHITECTURE_ATTRIBUTE, getObjects().named(MachineArchitecture.class, targetMachine.getArchitecture().getName()))
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
		@With OutgoingArtifact artifact;
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
			return new ConfigurationSpec(BUCKET, ImmutableList.of(), emptyMap(), null, it -> {}, null);
		}

		static ConfigurationSpec asBucket(Configuration fromBucket) {
			return new ConfigurationSpec(BUCKET, ImmutableList.of(fromBucket), emptyMap(), null, it -> {}, null);
		}

		static ConfigurationSpec asOutgoing(Configuration... fromBuckets) {
			return new ConfigurationSpec(OUTGOING, ImmutableList.copyOf(fromBuckets), emptyMap(), null, it -> {}, null);
		}

		static ConfigurationSpec asIncoming() {
			return new ConfigurationSpec(INCOMING, ImmutableList.of(), emptyMap(),null, it -> {}, null);
		}

		static ConfigurationSpec asIncoming(Configuration... fromBucket) {
			return new ConfigurationSpec(INCOMING, ImmutableList.copyOf(fromBucket), emptyMap(), null, it -> {}, null);
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
			if (artifact != null) {
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
