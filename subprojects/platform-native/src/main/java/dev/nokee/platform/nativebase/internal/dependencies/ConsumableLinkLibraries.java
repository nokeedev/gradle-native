package dev.nokee.platform.nativebase.internal.dependencies;

import dev.nokee.platform.base.Binary;
import dev.nokee.platform.nativebase.StaticLibraryBinary;
import dev.nokee.platform.nativebase.internal.SharedLibraryBinaryInternal;
import dev.nokee.platform.nativebase.tasks.CreateStaticLibrary;
import dev.nokee.platform.nativebase.tasks.LinkSharedLibrary;
import dev.nokee.platform.nativebase.tasks.internal.LinkSharedLibraryTask;
import org.gradle.api.attributes.Usage;
import org.gradle.api.file.RegularFile;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;

import javax.inject.Inject;

public final class ConsumableLinkLibraries extends BaseNativeConsumableDependencyBucket {
	@Inject
	public ConsumableLinkLibraries(ObjectFactory objectFactory) {
		super(objectFactory);
		getAttributes().attribute(Usage.USAGE_ATTRIBUTE, objectFactory.named(Usage.class, Usage.NATIVE_LINK));
	}

	public ConsumableLinkLibraries staticLibraryArtifact(Object notation) {
		getOutgoing().artifact(notation);
		return this;
	}

	public void binary(Provider<Binary> binary) {
		getOutgoing().artifact(binary.flatMap(this::getOutgoingLinkLibrary));
	}

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
}
