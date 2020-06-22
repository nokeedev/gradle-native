package dev.nokee.platform.nativebase.internal.dependencies;

import dev.nokee.platform.base.internal.BuildVariant;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.nativebase.NativeLibraryDependencies;
import dev.nokee.platform.nativebase.internal.ConfigurationUtils;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ModuleDependency;

import javax.inject.Inject;

public abstract class DefaultNativeLibraryDependencies extends AbstractNativeComponentDependencies implements NativeLibraryDependencies {
	private final DependencyBucket api;

	@Inject
	public DefaultNativeLibraryDependencies(NamingScheme names) {
		super(names);
		this.api = getObjects().newInstance(NativeDependencyBucket.class, getConfigurations().create(names.getConfigurationName("api"), ConfigurationUtils::configureAsBucket));

		getImplementationDependencies().extendsFrom(getApiDependencies());
	}

	@Override
	public void api(Object notation) {
		api.addDependency(notation);
	}

	@Override
	public void api(Object notation, Action<? super ModuleDependency> action) {
		api.addDependency(notation, action);
	}

	public Configuration getApiDependencies() {
		return api.getAsConfiguration();
	}

	@Override
	public DefaultNativeLibraryDependencies extendsWith(NamingScheme names) {
		DefaultNativeLibraryDependencies result = getObjects().newInstance(DefaultNativeLibraryDependencies.class, names);
		result.getApiDependencies().extendsFrom(getApiDependencies());
		result.getImplementationDependencies().extendsFrom(getImplementationDependencies());
		result.getCompileOnlyDependencies().extendsFrom(getCompileOnlyDependencies());
		result.getLinkOnlyDependencies().extendsFrom(getLinkOnlyDependencies());
		result.getRuntimeOnlyDependencies().extendsFrom(getRuntimeOnlyDependencies());
		return result;
	}

	@Override
	public AbstractBinaryAwareNativeComponentDependencies newVariantDependency(NamingScheme names, BuildVariant buildVariant, boolean hasSwift) {
		SwiftModuleIncomingDependencies incomingSwiftDependencies = null;
		HeaderIncomingDependencies incomingHeaderDependencies = null;
		if (hasSwift) {
			incomingSwiftDependencies = getObjects().newInstance(DefaultSwiftModuleIncomingDependencies.class, names, this);
			incomingHeaderDependencies = getObjects().newInstance(NoHeaderIncomingDependencies.class);
		} else {
			incomingHeaderDependencies = getObjects().newInstance(DefaultHeaderIncomingDependencies.class, names, this, buildVariant);
			incomingSwiftDependencies = getObjects().newInstance(NoSwiftModuleIncomingDependencies.class);
		}

		NativeIncomingDependencies incoming = getObjects().newInstance(NativeIncomingDependencies.class, names, buildVariant, this, incomingSwiftDependencies, incomingHeaderDependencies);
		NativeOutgoingDependencies outgoing = null;
		if (hasSwift) {
			outgoing = getObjects().newInstance(SwiftLibraryOutgoingDependencies.class, names, buildVariant, this);
		} else {
			outgoing = getObjects().newInstance(NativeLibraryOutgoingDependencies.class, names, buildVariant, this);
		}

		return getObjects().newInstance(BinaryAwareNativeLibraryDependencies.class, this, incoming, outgoing);
	}

	public DefaultNativeLibraryDependencies extendsFrom(DefaultNativeLibraryDependencies dependencies) {
		getApiDependencies().extendsFrom(dependencies.getApiDependencies());
		getImplementationDependencies().extendsFrom(dependencies.getImplementationDependencies());
		getCompileOnlyDependencies().extendsFrom(dependencies.getCompileOnlyDependencies());
		getLinkOnlyDependencies().extendsFrom(dependencies.getLinkOnlyDependencies());
		getRuntimeOnlyDependencies().extendsFrom(dependencies.getRuntimeOnlyDependencies());
		return this;
	}
}
