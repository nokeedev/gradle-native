package dev.nokee.platform.nativebase.internal.dependencies;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.VariantIdentifier;
import lombok.val;
import org.gradle.api.model.ObjectFactory;

import java.util.function.Function;

@AutoFactory
public final class DefaultNativeDependenciesBuilder {
	private final NativeComponentDependenciesFactory dependenciesFactory;
	private final ObjectFactory objectFactory;
	private BuildVariantInternal buildVariant = null;
	private VariantIdentifier identifier = null;
	private boolean hasNativeHeaders = false;
	private boolean hasSwiftModules = false;
	private NativeComponentDependenciesInternal dependencies = null;
	private Function<NativeComponentDependenciesInternal, NativeOutgoingDependencies> outgoingDependencies = null;

	public DefaultNativeDependenciesBuilder(@Provided NativeComponentDependenciesFactory dependenciesFactory, @Provided ObjectFactory objectFactory) {
		this.dependenciesFactory = dependenciesFactory;
		this.objectFactory = objectFactory;
	}

	public DefaultNativeDependenciesBuilder withVariant(BuildVariantInternal buildVariant) {
		this.buildVariant = buildVariant;
		return this;
	}

	public DefaultNativeDependenciesBuilder withIdentifier(VariantIdentifier identifier) {
		this.identifier = identifier;
		return this;
	}

	public DefaultNativeDependenciesBuilder withNativeHeaders() {
		hasNativeHeaders = true;
		return this;
	}

	public DefaultNativeDependenciesBuilder withSwiftModules() {
		hasSwiftModules = true;
		return this;
	}

	public DefaultNativeDependenciesBuilder withParentDependencies(NativeComponentDependenciesInternal dependencies) {
		this.dependencies = dependencies;
		return this;
	}

	public DefaultNativeDependenciesBuilder withOutgoingDependencies(Function<NativeComponentDependenciesInternal, NativeOutgoingDependencies> outgoingDependencies) {
		this.outgoingDependencies = outgoingDependencies;
		return this;
	}

	public DefaultNativeDependencies build() {
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

		NativeOutgoingDependencies outgoing = null;
		if (outgoingDependencies == null) {
			outgoing = NativeOutgoingDependenciesImpl.builder(variantDependencies).withVariant(buildVariant).buildUsing(objectFactory);
		} else {
			outgoing = outgoingDependencies.apply(variantDependencies);
		}

		return new DefaultNativeDependencies(variantDependencies, incoming, outgoing);
	}
}
