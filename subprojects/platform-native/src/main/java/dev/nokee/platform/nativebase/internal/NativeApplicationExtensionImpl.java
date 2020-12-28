package dev.nokee.platform.nativebase.internal;

import dev.nokee.platform.base.VariantView;
import dev.nokee.platform.nativebase.NativeApplication;
import dev.nokee.platform.nativebase.NativeApplicationComponentDependencies;
import dev.nokee.platform.nativebase.NativeApplicationExtension;
import dev.nokee.runtime.nativebase.TargetBuildType;
import dev.nokee.runtime.nativebase.TargetMachine;
import dev.nokee.utils.ConfigureUtils;
import lombok.Getter;
import org.gradle.api.Project;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.provider.SetProperty;

import static dev.nokee.utils.ConfigureUtils.configureDisplayName;

public final class NativeApplicationExtensionImpl extends BaseNativeExtension<DefaultNativeApplicationComponent> implements NativeApplicationExtension {
	private final DefaultNativeApplicationComponent component;
	@Getter private final SetProperty<TargetMachine> targetMachines;
	@Getter private final SetProperty<TargetBuildType> targetBuildTypes;

	public NativeApplicationExtensionImpl(DefaultNativeApplicationComponent component, ObjectFactory objects, ProviderFactory providers, ProjectLayout layout) {
		super(component, objects, providers, layout);
		this.component = component;
		this.targetMachines = configureDisplayName(objects.setProperty(TargetMachine.class), "targetMachines");
		this.targetBuildTypes = configureDisplayName(objects.setProperty(TargetBuildType.class), "targetBuildTypes");
	}

	public void setTargetMachines(Object value) {
		ConfigureUtils.setPropertyValue(targetMachines, value);
	}

	public void setTargetBuildTypes(Object value) {
		ConfigureUtils.setPropertyValue(targetBuildTypes, value);
	}

	@Override
	public NativeApplicationComponentDependencies getDependencies() {
		return component.getDependencies();
	}

	@Override
	public VariantView<NativeApplication> getVariants() {
		return component.getVariantCollection().getAsView(NativeApplication.class);
	}

	public void finalizeExtension(Project project) {
		component.finalizeExtension(project);
	}
}
