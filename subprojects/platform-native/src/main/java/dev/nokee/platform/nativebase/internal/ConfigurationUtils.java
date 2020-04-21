package dev.nokee.platform.nativebase.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
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
import static dev.nokee.platform.nativebase.internal.ConfigurationUtils.ConfigurationSpec.Type.INCOMING;
import static dev.nokee.platform.nativebase.internal.ConfigurationUtils.ConfigurationSpec.Type.OUTGOING;
import static java.util.Collections.emptyMap;

public abstract class ConfigurationUtils {
	//region Bucket
	public Action<Configuration> asBucket() {
		return configuration -> configureAsBucket(configuration);
	}

	public Action<Configuration> asBucket(Configuration fromBucket) {
		return configuration -> {
			configureAsBucket(configuration);
			configuration.extendsFrom(fromBucket);
		};
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

	public IncomingConfigurationAction asIncomingHeaderSearchPathFrom(Configuration fromBucket) {
		return getObjects().newInstance(IncomingConfigurationAction.class,
			ConfigurationSpec.asIncoming(fromBucket).withAttributes(
				ImmutableMap.<Attribute<?>, Object>builder()
					.put(Usage.USAGE_ATTRIBUTE, getObjects().named(Usage.class, Usage.C_PLUS_PLUS_API))
					.build()));
	}

	public IncomingConfigurationAction asIncomingLinkLibrariesFrom(Configuration fromBucket) {
		return getObjects().newInstance(IncomingConfigurationAction.class,
			ConfigurationSpec.asIncoming(fromBucket).withAttributes(
				ImmutableMap.<Attribute<?>, Object>builder()
					.put(Usage.USAGE_ATTRIBUTE, getObjects().named(Usage.class, Usage.NATIVE_LINK))
					.build()));
	}

	public IncomingConfigurationAction asIncomingRuntimeLibrariesFrom(Configuration fromBucket) {
		return getObjects().newInstance(IncomingConfigurationAction.class,
			ConfigurationSpec.asIncoming(fromBucket).withAttributes(
				ImmutableMap.<Attribute<?>, Object>builder()
					.put(Usage.USAGE_ATTRIBUTE, getObjects().named(Usage.class, Usage.NATIVE_RUNTIME))
					.build()));
	}
	//endregion

	//region Outgoing
	public VariantAwareOutgoingConfigurationAction asOutgoingHeaderSearchPathFrom(Configuration fromBucket) {
		return getObjects().newInstance(VariantAwareOutgoingConfigurationAction.class,
			ConfigurationSpec.asOutgoing(fromBucket, of(Usage.USAGE_ATTRIBUTE, getObjects().named(Usage.class, Usage.C_PLUS_PLUS_API))));
	}

	public VariantAwareOutgoingConfigurationAction asOutgoingLinkLibrariesFrom(Configuration fromBucket) {
		return getObjects().newInstance(VariantAwareOutgoingConfigurationAction.class,
			ConfigurationSpec.asOutgoing(fromBucket, of(Usage.USAGE_ATTRIBUTE, getObjects().named(Usage.class, Usage.NATIVE_LINK))));
	}

	public VariantAwareOutgoingConfigurationAction asOutgoingRuntimeLibrariesFrom(Configuration fromBucket) {
		return getObjects().newInstance(VariantAwareOutgoingConfigurationAction.class,
			ConfigurationSpec.asOutgoing(fromBucket, of(Usage.USAGE_ATTRIBUTE, getObjects().named(Usage.class, Usage.NATIVE_RUNTIME))));
	}
	//endregion

	private static void configureAsIncoming(Configuration configuration) {
		configuration.setCanBeConsumed(false);
		configuration.setCanBeResolved(true);
	}

	private static void configureAsOutgoing(Configuration configuration) {
		configuration.setCanBeConsumed(true);
		configuration.setCanBeResolved(false);
	}

	private static void configureAsBucket(Configuration configuration) {
		configuration.setCanBeConsumed(false);
		configuration.setCanBeResolved(false);
	}

	@Inject
	protected abstract ObjectFactory getObjects();

	public static abstract class IncomingConfigurationAction implements Action<Configuration> {
		private final ConfigurationSpec spec;

		@Inject
		public IncomingConfigurationAction(ConfigurationSpec spec) {
			this.spec = spec;
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
		public void execute(Configuration configuration) {
			spec.execute(configuration);
		}
	}

	public static abstract class VariantAwareOutgoingConfigurationAction implements Action<Configuration> {
		private final ConfigurationSpec spec;

		@Inject
		public VariantAwareOutgoingConfigurationAction(ConfigurationSpec spec) {
			this.spec = spec;
		}

		@Inject
		protected abstract ObjectFactory getObjects();

		@Override
		public void execute(Configuration configuration) {
			spec.execute(configuration);
		}

		VariantAwareOutgoingConfigurationAction withStaticLinkage() {
			return getObjects().newInstance(VariantAwareOutgoingConfigurationAction.class,
				spec.withAttributes(ImmutableMap.<Attribute<?>, Object>builder()
					.putAll(spec.attributes)
					.put(CppBinary.LINKAGE_ATTRIBUTE, Linkage.STATIC)
					.build()));
		}

		VariantAwareOutgoingConfigurationAction withSharedLinkage() {
			return getObjects().newInstance(VariantAwareOutgoingConfigurationAction.class,
				spec.withAttributes(ImmutableMap.<Attribute<?>, Object>builder()
					.putAll(spec.attributes)
					.put(CppBinary.LINKAGE_ATTRIBUTE, Linkage.SHARED)
					.build()));
		}

		VariantAwareOutgoingConfigurationAction asDebug() {
			return getObjects().newInstance(VariantAwareOutgoingConfigurationAction.class,
				spec.withAttributes(ImmutableMap.<Attribute<?>, Object>builder()
					.putAll(spec.attributes)
					.put(CppBinary.DEBUGGABLE_ATTRIBUTE, Boolean.TRUE)
					.put(CppBinary.OPTIMIZED_ATTRIBUTE, Boolean.FALSE)
					.build()));
		}

		VariantAwareOutgoingConfigurationAction asRelease() {
			return getObjects().newInstance(VariantAwareOutgoingConfigurationAction.class,
				spec.withAttributes(ImmutableMap.<Attribute<?>, Object>builder()
					.putAll(spec.attributes)
					.put(CppBinary.DEBUGGABLE_ATTRIBUTE, Boolean.TRUE)
					.put(CppBinary.OPTIMIZED_ATTRIBUTE, Boolean.TRUE)
					.build()));
		}

		VariantAwareOutgoingConfigurationAction frameworkArtifact(Object notation) {
			return getObjects().newInstance(VariantAwareOutgoingConfigurationAction.class,
				spec.withArtifact(new OutgoingArtifact(ArtifactTypes.FRAMEWORK_TYPE, notation))
					.withAttributes(ImmutableMap.<Attribute<?>, Object>builder()
						.putAll(spec.attributes)
						.put(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, getObjects().named(LibraryElements.class, LibraryElements.FRAMEWORK_BUNDLE))
						.build()));
		}

		VariantAwareOutgoingConfigurationAction headerDirectoryArtifact(Object notation) {
			return getObjects().newInstance(VariantAwareOutgoingConfigurationAction.class,
				spec.withArtifact(new OutgoingArtifact(ArtifactTypes.DIRECTORY_TYPE, notation))
					.withAttributes(ImmutableMap.<Attribute<?>, Object>builder()
						.putAll(spec.attributes).put(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, getObjects().named(LibraryElements.class, LibraryElements.HEADERS_CPLUSPLUS))
						.build()));
		}

		VariantAwareOutgoingConfigurationAction sharedLibraryArtifact(Object notation) {
			return getObjects().newInstance(VariantAwareOutgoingConfigurationAction.class,
				spec.withArtifact(new OutgoingArtifact(null, notation))
					.withAttributes(ImmutableMap.<Attribute<?>, Object>builder()
						.putAll(spec.attributes).put(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, getObjects().named(LibraryElements.class, LibraryElements.DYNAMIC_LIB))
						.build()));
		}

		VariantAwareOutgoingConfigurationAction staticLibraryArtifact(Object notation) {
			return getObjects().newInstance(VariantAwareOutgoingConfigurationAction.class,
				spec.withArtifact(new OutgoingArtifact(null, notation))
					.withAttributes(ImmutableMap.<Attribute<?>, Object>builder()
						.putAll(spec.attributes).put(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, getObjects().named(LibraryElements.class, LibraryElements.LINK_ARCHIVE))
						.build()));
		}

		VariantAwareOutgoingConfigurationAction importLibraryArtifact(Object notation) {
			return getObjects().newInstance(VariantAwareOutgoingConfigurationAction.class,
				spec.withArtifact(new OutgoingArtifact(null, notation))
					.withAttributes(ImmutableMap.<Attribute<?>, Object>builder()
						.putAll(spec.attributes).put(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, getObjects().named(LibraryElements.class, LibraryElements.IMPORT_LIB))
						.build()));
		}

		VariantAwareOutgoingConfigurationAction andThen(Action<Configuration> additionalAction) {
			return getObjects().newInstance(VariantAwareOutgoingConfigurationAction.class, spec.withAdditionalAction(additionalAction));
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

		enum Type {
			INCOMING(ConfigurationUtils::configureAsIncoming), OUTGOING(ConfigurationUtils::configureAsOutgoing);

			private final Consumer<Configuration> action;

			Type(Consumer<Configuration> action) {
				this.action = action;
			}

			void configure(Configuration configuration) {
				action.accept(configuration);
			}
		}

		static ConfigurationSpec asOutgoing(Configuration fromBucket, Map<Attribute<?>, Object> attributes) {
			return new ConfigurationSpec(OUTGOING, ImmutableList.of(fromBucket), attributes, /*emptyList(),*/ null, it -> {});
		}

		static ConfigurationSpec asIncoming() {
			return new ConfigurationSpec(INCOMING, ImmutableList.of(), emptyMap(), /*emptyList(),*/ null, it -> {});
		}

		static ConfigurationSpec asIncoming(Configuration fromBucket) {
			return new ConfigurationSpec(INCOMING, ImmutableList.of(fromBucket), emptyMap(), /*emptyList(),*/ null, it -> {});
		}

		@Override
		public void execute(Configuration configuration) {
			type.configure(configuration);
			configuration.setExtendsFrom(fromBuckets);

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
