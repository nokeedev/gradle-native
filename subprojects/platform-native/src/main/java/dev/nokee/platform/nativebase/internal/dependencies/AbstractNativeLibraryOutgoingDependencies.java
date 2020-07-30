package dev.nokee.platform.nativebase.internal.dependencies;

import com.google.common.collect.ImmutableList;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.nativebase.StaticLibraryBinary;
import dev.nokee.platform.nativebase.internal.ConfigurationUtils;
import dev.nokee.platform.nativebase.internal.SharedLibraryBinaryInternal;
import dev.nokee.platform.nativebase.tasks.CreateStaticLibrary;
import dev.nokee.platform.nativebase.tasks.LinkSharedLibrary;
import dev.nokee.platform.nativebase.tasks.internal.LinkSharedLibraryTask;
import lombok.val;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.PublishArtifact;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.internal.artifacts.dsl.LazyPublishArtifact;
import org.gradle.api.internal.provider.Providers;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;

import javax.inject.Inject;

public abstract class AbstractNativeLibraryOutgoingDependencies {
	private final ConfigurationUtils builder = getObjects().newInstance(ConfigurationUtils.class);

	@Inject
	public AbstractNativeLibraryOutgoingDependencies(NamingScheme names, BuildVariantInternal buildVariant, DefaultNativeLibraryComponentDependencies dependencies) {

		Configuration linkElements = getConfigurations().create(names.getConfigurationName("linkElements"), builder.asOutgoingLinkLibrariesFrom(dependencies.getApi().getAsConfiguration(), dependencies.getLinkOnly().getAsConfiguration()).withVariant(buildVariant).withDescription(names.getConfigurationDescription("Link elements for %s.")));
		Configuration runtimeElements = getConfigurations().create(names.getConfigurationName("runtimeElements"), builder.asOutgoingRuntimeLibrariesFrom(dependencies.getImplementation().getAsConfiguration(), dependencies.getRuntimeOnly().getAsConfiguration()).withVariant(buildVariant).withDescription(names.getConfigurationDescription("Runtime elements for %s.")));

		linkElements.getOutgoing().artifact(getExportedBinary().flatMap(this::getOutgoingLinkLibrary));

		val artifacts = getObjects().listProperty(PublishArtifact.class);
		artifacts.addAll(getExportedBinary().flatMap(this::getOutgoingRuntimeLibrary));
		runtimeElements.getOutgoing().getArtifacts().addAllLater(artifacts);
//		runtimeElements.getOutgoing().artifact(getExportedBinary().flatMap(this::getOutgoingRuntimeLibrary));
	}

	@Inject
	protected abstract ConfigurationContainer getConfigurations();

	@Inject
	protected abstract ObjectFactory getObjects();

	private Provider<RegularFile> getOutgoingLinkLibrary(Binary binary) {
		if (binary instanceof SharedLibraryBinaryInternal) {
			if (((SharedLibraryBinaryInternal) binary).getTargetMachine().getOperatingSystemFamily().isWindows()) {
				return ((SharedLibraryBinaryInternal) binary).getLinkTask().flatMap(it -> ((LinkSharedLibraryTask) it).getImportLibrary());
			}
			return ((SharedLibraryBinaryInternal) binary).getLinkTask().flatMap(LinkSharedLibrary::getLinkedFile);
		} else if (binary instanceof StaticLibraryBinary) {
			return ((StaticLibraryBinary) binary).getCreateTask().flatMap(CreateStaticLibrary::getOutputFile);
		}
		throw new IllegalArgumentException("Unsupported binary to export");
	}

	private Provider<Iterable<PublishArtifact>> getOutgoingRuntimeLibrary(Binary binary) {
		if (binary instanceof SharedLibraryBinaryInternal) {
			return Providers.of(ImmutableList.of(new LazyPublishArtifact(((SharedLibraryBinaryInternal) binary).getLinkTask().flatMap(LinkSharedLibrary::getLinkedFile))));
		} else if (binary instanceof StaticLibraryBinary) {
			return Providers.of(ImmutableList.of());
		}
		throw new IllegalArgumentException("Unsupported binary to export");
	}

	public abstract DirectoryProperty getExportedHeaders();
	public abstract RegularFileProperty getExportedSwiftModule();
	public abstract Property<Binary> getExportedBinary();
}
