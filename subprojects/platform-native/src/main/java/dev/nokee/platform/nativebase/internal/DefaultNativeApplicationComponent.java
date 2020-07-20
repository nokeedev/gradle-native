package dev.nokee.platform.nativebase.internal;

import com.google.common.collect.ImmutableSet;
import dev.nokee.platform.base.BinaryAwareComponent;
import dev.nokee.platform.base.DependencyAwareComponent;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.AbstractBinaryAwareNativeComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeComponentDependencies;
import dev.nokee.runtime.nativebase.internal.DefaultMachineArchitecture;
import dev.nokee.runtime.nativebase.internal.DefaultOperatingSystemFamily;
import org.gradle.api.Action;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

public abstract class DefaultNativeApplicationComponent extends BaseNativeComponent<DefaultNativeApplicationVariant> implements DependencyAwareComponent<NativeComponentDependencies>, BinaryAwareComponent, Component {
	private final DefaultNativeComponentDependencies dependencies;

	@Inject
	public DefaultNativeApplicationComponent(NamingScheme names) {
		super(names, DefaultNativeApplicationVariant.class);
		this.dependencies = getObjects().newInstance(DefaultNativeComponentDependencies.class, getNames());
		getDimensions().convention(ImmutableSet.of(DefaultBinaryLinkage.DIMENSION_TYPE, DefaultOperatingSystemFamily.DIMENSION_TYPE, DefaultMachineArchitecture.DIMENSION_TYPE));
	}

	@Override
	public DefaultNativeComponentDependencies getDependencies() {
		return dependencies;
	}

	@Override
	public void dependencies(Action<? super NativeComponentDependencies> action) {
		action.execute(dependencies);
	}

	@Override
	protected DefaultNativeApplicationVariant createVariant(String name, BuildVariant buildVariant, AbstractBinaryAwareNativeComponentDependencies variantDependencies) {
		NamingScheme names = getNames().forBuildVariant(buildVariant, getBuildVariants().get());

		DefaultNativeApplicationVariant result = getObjects().newInstance(DefaultNativeApplicationVariant.class, name, names, buildVariant, variantDependencies);
		return result;
	}

	public static DomainObjectFactory<DefaultNativeApplicationComponent> newMain(ObjectFactory objects, NamingSchemeFactory namingSchemeFactory) {
		return new DomainObjectFactory<DefaultNativeApplicationComponent>() {
			@Override
			public DefaultNativeApplicationComponent create() {
				NamingScheme names = namingSchemeFactory.forMainComponent().withComponentDisplayName("main native component");
				return objects.newInstance(DefaultNativeApplicationComponent.class, names);
			}

			@Override
			public Class<DefaultNativeApplicationComponent> getType() {
				return DefaultNativeApplicationComponent.class;
			}

			@Override
			public Class<? extends DefaultNativeApplicationComponent> getImplementationType() {
				return DefaultNativeApplicationComponent.class;
			}

			@Override
			public DomainObjectIdentity getIdentity() {
				return DomainObjectIdentity.named("main");
			}
		};
	}
}
