package dev.nokee.platform.nativebase.internal;

import com.google.common.collect.ImmutableList;
import dev.nokee.runtime.nativebase.TargetMachine;
import dev.nokee.runtime.nativebase.internal.DefaultMachineArchitecture;
import dev.nokee.runtime.nativebase.internal.DefaultOperatingSystemFamily;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.SetProperty;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

public abstract class TargetMachineRule implements Action<Project> {
	private final ToolChainSelectorInternal toolChainSelector = getObjects().newInstance(ToolChainSelectorInternal.class);
	private final SetProperty<TargetMachine> targetMachines;
	private final String componentName;

	@Inject
	public TargetMachineRule(SetProperty<TargetMachine> targetMachines, String componentName) {
		this.targetMachines = targetMachines;
		this.componentName = componentName;
		targetMachines.convention(ImmutableList.of(DefaultTargetMachineFactory.host()));
	}

	@Inject
	protected abstract ObjectFactory getObjects();

	@Override
	public void execute(Project project) {
		this.targetMachines.disallowChanges();
		this.targetMachines.finalizeValue();
		Set<TargetMachine> targetMachines = this.targetMachines.get();
		assertNonEmpty(targetMachines, "target machine", componentName);
		assertTargetMachinesAreKnown(targetMachines);
	}

	private static void assertNonEmpty(Collection<?> values, String propertyName, String componentName) {
		if (values.isEmpty()) {
			throw new IllegalArgumentException(String.format("A %s needs to be specified for the %s.", propertyName, componentName));
		}
	}

	private void assertTargetMachinesAreKnown(Collection<TargetMachine> targetMachines) {
		List<TargetMachine> unknownTargetMachines = targetMachines.stream().filter(it -> !toolChainSelector.isKnown(it)).collect(Collectors.toList());
		if (!unknownTargetMachines.isEmpty()) {
			throw new IllegalArgumentException("The following target machines are not know by the defined tool chains:\n" + unknownTargetMachines.stream().map(it -> " * " + ((DefaultOperatingSystemFamily)it.getOperatingSystemFamily()).getName() + " " + ((DefaultMachineArchitecture)it.getArchitecture()).getName()).collect(joining("\n")));
		}
	}
}
