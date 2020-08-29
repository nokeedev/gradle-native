package dev.nokee.platform.nativebase.internal.dependencies;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.VariantIdentifier;
import lombok.val;
import org.gradle.api.model.ObjectFactory;

@AutoFactory
public final class DefaultNativeLibraryDependenciesBuilder {
	private final NativeLibraryComponentDependenciesFactory dependenciesFactory;
	private final ObjectFactory objectFactory;
	private BuildVariantInternal buildVariant;
	private VariantIdentifier identifier;
	private boolean hasNativeHeaders = false;
	private boolean hasSwiftModules = false;
	private NativeLibraryComponentDependenciesInternal dependencies;

	public DefaultNativeLibraryDependenciesBuilder(@Provided NativeLibraryComponentDependenciesFactory dependenciesFactory, @Provided ObjectFactory objectFactory) {
		this.dependenciesFactory = dependenciesFactory;
		this.objectFactory = objectFactory;
	}

	public DefaultNativeLibraryDependenciesBuilder withVariant(BuildVariantInternal buildVariant) {
		this.buildVariant = buildVariant;
		return this;
	}

	public DefaultNativeLibraryDependenciesBuilder withIdentifier(VariantIdentifier identifier) {
		this.identifier = identifier;
		return this;
	}

	public DefaultNativeLibraryDependenciesBuilder withNativeHeaders() {
		hasNativeHeaders = true;
		return this;
	}

	public DefaultNativeLibraryDependenciesBuilder withSwiftModules() {
		hasSwiftModules = true;
		return this;
	}

	public DefaultNativeLibraryDependenciesBuilder withParentDependencies(NativeLibraryComponentDependenciesInternal dependencies) {
		this.dependencies = dependencies;
		return this;
	}

	public DefaultNativeLibraryDependencies build() {
		val variantDependencies = dependenciesFactory.create(identifier);
		if (!identifier.getUnambiguousName().isEmpty()) {
			variantDependencies.configureEach(variantBucket -> {
				dependencies.findByName(variantBucket.getName()).ifPresent(variantBucket::extendsFrom);
			});
		}

		val incomingDependenciesBuilder = NativeIncomingDependenciesImpl.builder(variantDependencies).withVariant(buildVariant);
		if (hasSwiftModules) {
			incomingDependenciesBuilder.withIncomingSwiftModules();
		} else if (hasNativeHeaders) {
			incomingDependenciesBuilder.withIncomingHeaders();
		}
		val incoming = incomingDependenciesBuilder.buildUsing(objectFactory);

		val outgoingDependenciesBuilder = NativeOutgoingDependenciesImpl.builder(variantDependencies).withVariant(buildVariant).withExportedApi();
		if (hasSwiftModules) {
			outgoingDependenciesBuilder.withOutgoingSwiftModules();
		} else if (hasNativeHeaders) {
			outgoingDependenciesBuilder.withOutgoingHeaders();
		}
		val outgoing = outgoingDependenciesBuilder.buildUsing(objectFactory);

		return new DefaultNativeLibraryDependencies(variantDependencies, incoming, outgoing);
	}
}
