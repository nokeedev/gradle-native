package dev.nokee.platform.nativebase.internal.dependencies;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.VariantIdentifier;
import lombok.val;
import org.gradle.api.model.ObjectFactory;

@AutoFactory
public final class DefaultNativeApplicationDependenciesBuilder {
	private final NativeApplicationComponentDependenciesFactory dependenciesFactory;
	private final ObjectFactory objectFactory;
	private BuildVariantInternal buildVariant;
	private VariantIdentifier identifier;
	private boolean hasNativeHeaders = false;
	private boolean hasSwiftModules = false;
	private NativeApplicationComponentDependenciesInternal dependencies;

	public DefaultNativeApplicationDependenciesBuilder(@Provided NativeApplicationComponentDependenciesFactory dependenciesFactory, @Provided ObjectFactory objectFactory) {
		this.dependenciesFactory = dependenciesFactory;
		this.objectFactory = objectFactory;
	}

	public DefaultNativeApplicationDependenciesBuilder withVariant(BuildVariantInternal buildVariant) {
		this.buildVariant = buildVariant;
		return this;
	}

	public DefaultNativeApplicationDependenciesBuilder withIdentifier(VariantIdentifier identifier) {
		this.identifier = identifier;
		return this;
	}

	public DefaultNativeApplicationDependenciesBuilder withNativeHeaders() {
		hasNativeHeaders = true;
		return this;
	}

	public DefaultNativeApplicationDependenciesBuilder withSwiftModules() {
		hasSwiftModules = true;
		return this;
	}

	public DefaultNativeApplicationDependenciesBuilder withParentDependencies(NativeApplicationComponentDependenciesInternal dependencies) {
		this.dependencies = dependencies;
		return this;
	}

	public DefaultNativeApplicationDependencies build() {
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
		val outgoing = NativeOutgoingDependenciesImpl.builder(variantDependencies).withVariant(buildVariant).buildUsing(objectFactory);

		return new DefaultNativeApplicationDependencies(variantDependencies, incoming, outgoing);
	}
}
