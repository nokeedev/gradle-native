package dev.nokee.platform.nativebase.internal;

import com.google.common.collect.ImmutableSet;
import dev.nokee.platform.base.BinaryAwareComponent;
import dev.nokee.platform.base.DependencyAwareComponent;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.nativebase.NativeLibraryDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.AbstractBinaryAwareNativeComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeLibraryDependencies;
import dev.nokee.runtime.nativebase.internal.DefaultMachineArchitecture;
import dev.nokee.runtime.nativebase.internal.DefaultOperatingSystemFamily;
import org.gradle.api.Action;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

public abstract class DefaultNativeLibraryComponent extends BaseNativeComponent<DefaultNativeLibraryVariant> implements DependencyAwareComponent<NativeLibraryDependencies>, BinaryAwareComponent, Component {
	private final DefaultNativeLibraryDependencies dependencies;

	@Inject
	public DefaultNativeLibraryComponent(NamingScheme names) {
		super(names, DefaultNativeLibraryVariant.class);
		this.dependencies = getObjects().newInstance(DefaultNativeLibraryDependencies.class, getNames());
		getDimensions().convention(ImmutableSet.of(DefaultBinaryLinkage.DIMENSION_TYPE, DefaultOperatingSystemFamily.DIMENSION_TYPE, DefaultMachineArchitecture.DIMENSION_TYPE));
	}

	@Override
	public DefaultNativeLibraryDependencies getDependencies() {
		return dependencies;
	}

	public void dependencies(Action<? super NativeLibraryDependencies> action) {
		action.execute(dependencies);
	}

	@Override
	protected DefaultNativeLibraryVariant createVariant(String name, BuildVariant buildVariant, AbstractBinaryAwareNativeComponentDependencies variantDependencies) {
		NamingScheme names = getNames().forBuildVariant(buildVariant, getBuildVariants().get());

		DefaultNativeLibraryVariant result = getObjects().newInstance(DefaultNativeLibraryVariant.class, name, names, buildVariant, variantDependencies);
		return result;
	}

	public static DomainObjectFactory<DefaultNativeLibraryComponent> newMain(ObjectFactory objects, NamingSchemeFactory namingSchemeFactory) {
		return new DomainObjectFactory<DefaultNativeLibraryComponent>() {
			@Override
			public DefaultNativeLibraryComponent create() {
				NamingScheme names = namingSchemeFactory.forMainComponent().withComponentDisplayName("main native component");
				return objects.newInstance(DefaultNativeLibraryComponent.class, names);
			}

			@Override
			public Class<DefaultNativeLibraryComponent> getType() {
				return DefaultNativeLibraryComponent.class;
			}

			@Override
			public Class<? extends DefaultNativeLibraryComponent> getImplementationType() {
				return DefaultNativeLibraryComponent.class;
			}

			@Override
			public DomainObjectIdentity getIdentity() {
				return DomainObjectIdentity.named("main");
			}
		};
	}
}
