package dev.nokee.platform.c.internal;

import dev.nokee.language.c.internal.CHeaderSet;
import dev.nokee.language.c.internal.CSourceSet;
import dev.nokee.platform.base.VariantView;
import dev.nokee.platform.c.CLibraryExtension;
import dev.nokee.platform.nativebase.*;
import dev.nokee.platform.nativebase.internal.*;
import dev.nokee.runtime.nativebase.TargetBuildType;
import dev.nokee.runtime.nativebase.TargetLinkage;
import dev.nokee.runtime.nativebase.TargetMachine;
import lombok.Getter;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.provider.SetProperty;

import javax.inject.Inject;

public class DefaultCLibraryExtension extends BaseNativeExtension<DefaultNativeLibraryComponent> implements CLibraryExtension {
	@Getter private final ConfigurableFileCollection sources;
	@Getter private final ConfigurableFileCollection privateHeaders;
	@Getter private final ConfigurableFileCollection publicHeaders;
	@Getter private final SetProperty<TargetLinkage> targetLinkages;
	@Getter private final SetProperty<TargetMachine> targetMachines;
	@Getter private final SetProperty<TargetBuildType> targetBuildTypes;

	@Inject
	public DefaultCLibraryExtension(DefaultNativeLibraryComponent component, ObjectFactory objects, ProviderFactory providers, ProjectLayout layout) {
		super(component, objects, providers, layout);
		this.sources = objects.fileCollection();
		this.privateHeaders = objects.fileCollection();
		this.publicHeaders = objects.fileCollection();
		this.targetLinkages = objects.setProperty(TargetLinkage.class);
		this.targetMachines = objects.setProperty(TargetMachine.class);
		this.targetBuildTypes = objects.setProperty(TargetBuildType.class);

		getComponent().getSourceCollection().add(getObjects().newInstance(CSourceSet.class, "c").from(getSources().getElements().map(toIfEmpty("src/main/c"))));
		getComponent().getSourceCollection().add(getObjects().newInstance(CHeaderSet.class, "headers").srcDir(getPrivateHeaders().getElements().map(toIfEmpty("src/main/headers"))));
		getComponent().getSourceCollection().add(getObjects().newInstance(CHeaderSet.class, "public").srcDir(getPublicHeaders().getElements().map(toIfEmpty("src/main/public"))));
	}

	@Override
	public NativeLibraryComponentDependencies getDependencies() {
		return getComponent().getDependencies();
	}

	@Override
	public void dependencies(Action<? super NativeLibraryComponentDependencies> action) {
		getComponent().dependencies(action);
	}

	public void finalizeExtension(Project project) {
		getComponent().finalizeExtension(project);
	}

	@Override
	public VariantView<NativeLibrary> getVariants() {
		return getComponent().getVariantCollection().getAsView(NativeLibrary.class);
	}

	@Override
	public TargetMachineFactory getMachines() {
		return DefaultTargetMachineFactory.INSTANCE;
	}

	@Override
	public TargetLinkageFactory getLinkages() {
		return DefaultTargetLinkageFactory.INSTANCE;
	}

	@Override
	public TargetBuildTypeFactory getBuildTypes() {
		return DefaultTargetBuildTypeFactory.INSTANCE;
	}
}
