package dev.nokee.platform.nativebase.internal.dependencies;

import com.google.common.collect.ImmutableList;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.nativebase.StaticLibraryBinary;
import dev.nokee.platform.nativebase.internal.ExecutableBinaryInternal;
import dev.nokee.platform.nativebase.internal.SharedLibraryBinaryInternal;
import dev.nokee.platform.nativebase.tasks.LinkExecutable;
import dev.nokee.platform.nativebase.tasks.LinkSharedLibrary;
import lombok.val;
import org.gradle.api.artifacts.PublishArtifact;
import org.gradle.api.attributes.Usage;
import org.gradle.api.internal.artifacts.dsl.LazyPublishArtifact;
import org.gradle.api.internal.provider.Providers;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;

import javax.inject.Inject;
import java.util.List;

public final class ConsumableRuntimeLibraries extends BaseNativeConsumableDependencyBucket {
	private final ObjectFactory objectFactory;

	@Inject
	public ConsumableRuntimeLibraries(ObjectFactory objectFactory) {
		super(objectFactory);
		this.objectFactory = objectFactory;
		getAttributes().attribute(Usage.USAGE_ATTRIBUTE, objectFactory.named(Usage.class, Usage.NATIVE_RUNTIME));
	}

	public void artifact(Object notation) {
		getOutgoing().artifact(notation);
	}

	public void artifact(Provider<List<PublishArtifact>> artifacts) {
		getOutgoing().getArtifacts().addAllLater(artifacts);
	}

	public void binary(Provider<Binary> binary) {
		// HACK: Adding outgoing artifact lazily...
		val artifacts = objectFactory.listProperty(PublishArtifact.class);
		artifacts.addAll(binary.flatMap(this::getOutgoingRuntimeLibrary));
		getOutgoing().getArtifacts().addAllLater(artifacts);
	}

	private Provider<Iterable<PublishArtifact>> getOutgoingRuntimeLibrary(Binary binary) {
		if (binary instanceof SharedLibraryBinaryInternal) {
			return Providers.of(ImmutableList.of(new LazyPublishArtifact(((SharedLibraryBinaryInternal) binary).getLinkTask().flatMap(LinkSharedLibrary::getLinkedFile))));
		} else if (binary instanceof StaticLibraryBinary) {
			return Providers.of(ImmutableList.of());
		} else if (binary instanceof ExecutableBinaryInternal) {
			return Providers.of(ImmutableList.of(new LazyPublishArtifact(((ExecutableBinaryInternal) binary).getLinkTask().flatMap(LinkExecutable::getLinkedFile))));
		}
		throw new IllegalArgumentException("Unsupported binary to export");
	}
}
