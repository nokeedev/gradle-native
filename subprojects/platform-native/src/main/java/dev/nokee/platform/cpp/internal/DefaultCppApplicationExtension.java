package dev.nokee.platform.cpp.internal;

import dev.nokee.language.cpp.internal.CppHeaderSet;
import dev.nokee.language.cpp.internal.CppSourceSet;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.VariantView;
import dev.nokee.platform.cpp.CppApplicationExtension;
import dev.nokee.platform.nativebase.NativeApplication;
import dev.nokee.platform.nativebase.NativeApplicationComponentDependencies;
import dev.nokee.platform.nativebase.internal.BaseNativeExtension;
import dev.nokee.platform.nativebase.internal.DefaultNativeApplicationComponent;
import dev.nokee.runtime.nativebase.TargetBuildType;
import dev.nokee.runtime.nativebase.TargetMachine;
import dev.nokee.utils.ConfigureUtils;
import lombok.Getter;
import org.gradle.api.Project;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.provider.SetProperty;

import javax.inject.Inject;

import static dev.nokee.utils.ConfigureUtils.configureDisplayName;
import static dev.nokee.utils.ConfigureUtils.setPropertyValue;

public class DefaultCppApplicationExtension extends BaseNativeExtension<DefaultNativeApplicationComponent> implements CppApplicationExtension, Component {
	@Getter private final ConfigurableFileCollection cppSources;
	@Getter private final ConfigurableFileCollection privateHeaders;
	@Getter private final SetProperty<TargetMachine> targetMachines;
	@Getter private final SetProperty<TargetBuildType> targetBuildTypes;

	@Inject
	public DefaultCppApplicationExtension(DefaultNativeApplicationComponent component, ObjectFactory objects, ProviderFactory providers, ProjectLayout layout) {
		super(component, objects, providers, layout);
		this.cppSources = objects.fileCollection();
		this.privateHeaders = objects.fileCollection();
		this.targetMachines = configureDisplayName(objects.setProperty(TargetMachine.class), "targetMachines");
		this.targetBuildTypes = configureDisplayName(objects.setProperty(TargetBuildType.class), "targetBuildTypes");

		getComponent().getSourceCollection().add(getObjects().newInstance(CppSourceSet.class, "cpp").from(this.getCppSources().getElements().map(toIfEmpty("src/main/cpp"))));
		getComponent().getSourceCollection().add(getObjects().newInstance(CppHeaderSet.class, "headers").srcDir(getPrivateHeaders().getElements().map(toIfEmpty("src/main/headers"))));
	}

	public void setTargetMachines(Object value) {
		ConfigureUtils.setPropertyValue(targetMachines, value);
	}

	public void setTargetBuildTypes(Object value) {
		ConfigureUtils.setPropertyValue(targetBuildTypes, value);
	}

	@Override
	public NativeApplicationComponentDependencies getDependencies() {
		return getComponent().getDependencies();
	}

	public void finalizeExtension(Project project) {
		getComponent().finalizeExtension(project);
	}

	@Override
	public VariantView<NativeApplication> getVariants() {
		return getComponent().getVariantCollection().getAsView(NativeApplication.class);
	}
}
