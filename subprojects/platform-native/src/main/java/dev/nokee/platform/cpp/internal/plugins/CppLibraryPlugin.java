package dev.nokee.platform.cpp.internal.plugins;

import dagger.BindsInstance;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dev.nokee.gradle.internal.GradleModule;
import dev.nokee.platform.base.DomainObjectElement;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.DomainObjectIdentity;
import dev.nokee.platform.base.internal.DomainObjectStore;
import dev.nokee.platform.base.internal.ProjectIdentifier;
import dev.nokee.platform.base.internal.plugins.ProjectStorePlugin;
import dev.nokee.platform.cpp.CppLibraryExtension;
import dev.nokee.platform.cpp.internal.DaggerPlatformCppComponents;
import dev.nokee.platform.cpp.internal.DefaultCppLibraryExtension;
import dev.nokee.platform.cpp.internal.DefaultCppLibraryExtensionFactory;
import dev.nokee.platform.nativebase.internal.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.nativeplatform.toolchain.internal.plugins.StandardToolChainsPlugin;

import javax.inject.Inject;

public class CppLibraryPlugin implements Plugin<Project> {
	private static final String EXTENSION_NAME = "library";
	@Getter(AccessLevel.PROTECTED) private final ObjectFactory objects;

	@Inject
	public CppLibraryPlugin(ObjectFactory objects) {
		this.objects = objects;
	}

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(StandardToolChainsPlugin.class);
		project.getPluginManager().apply(ProjectStorePlugin.class);

		val store = project.getExtensions().getByType(DomainObjectStore.class);
		val extension = DaggerPlatformCppComponents.factory().create(project).cppLibraryFactory().create(new ComponentIdentifier("main", "main native component", ProjectIdentifier.of(project)));
		val component = store.add(new DomainObjectElement<DefaultNativeLibraryComponent>() {
			@Override
			public DefaultNativeLibraryComponent get() {
				return extension.getComponent();
			}

			@Override
			public Class<DefaultNativeLibraryComponent> getType() {
				return DefaultNativeLibraryComponent.class;
			}

			@Override
			public DomainObjectIdentity getIdentity() {
				return DomainObjectIdentity.named("main");
			}
		});
		component.configure(it -> it.getBaseName().convention(project.getName()));
		component.get(); // force realize... for now.

		project.afterEvaluate(getObjects().newInstance(TargetMachineRule.class, extension.getTargetMachines(), EXTENSION_NAME));
		project.afterEvaluate(getObjects().newInstance(TargetLinkageRule.class, extension.getTargetLinkages(), EXTENSION_NAME));
		project.afterEvaluate(getObjects().newInstance(TargetBuildTypeRule.class, extension.getTargetBuildTypes(), EXTENSION_NAME));
		project.afterEvaluate(extension::finalizeExtension);

		project.getExtensions().add(CppLibraryExtension.class, EXTENSION_NAME, extension);
	}
}
