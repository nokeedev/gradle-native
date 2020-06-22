package dev.nokee.platform.nativebase.internal.dependencies;

import dev.nokee.platform.base.internal.BuildVariant;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.nativebase.NativeComponentDependencies;

import javax.inject.Inject;

public abstract class DefaultNativeComponentDependencies extends AbstractNativeComponentDependencies implements NativeComponentDependencies {
	@Inject
	public DefaultNativeComponentDependencies(NamingScheme names) {
		super(names);
	}

	@Override
	public DefaultNativeComponentDependencies extendsWith(NamingScheme names) {
		DefaultNativeComponentDependencies result = getObjects().newInstance(DefaultNativeComponentDependencies.class, names);
		result.getImplementationDependencies().extendsFrom(getImplementationDependencies());
		// HACK: For JNI, fix this
		if (getCompileOnlyDependencies() != null && result.getCompileOnlyDependencies() != null) {
			result.getCompileOnlyDependencies().extendsFrom(getCompileOnlyDependencies());
		}
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
		NativeOutgoingDependencies outgoing = getObjects().newInstance(NativeApplicationOutgoingDependencies.class, names, buildVariant, this);

		return getObjects().newInstance(BinaryAwareNativeComponentDependencies.class, this, incoming, outgoing);
	}

	public DefaultNativeComponentDependencies extendsFrom(DefaultNativeComponentDependencies dependencies) {
		getImplementationDependencies().extendsFrom(dependencies.getImplementationDependencies());
		getCompileOnlyDependencies().extendsFrom(dependencies.getCompileOnlyDependencies());
		getLinkOnlyDependencies().extendsFrom(dependencies.getLinkOnlyDependencies());
		getRuntimeOnlyDependencies().extendsFrom(dependencies.getRuntimeOnlyDependencies());
		return this;
	}
}
