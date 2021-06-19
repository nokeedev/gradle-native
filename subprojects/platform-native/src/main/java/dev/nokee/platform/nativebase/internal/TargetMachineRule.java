package dev.nokee.platform.nativebase.internal;

import com.google.common.collect.ImmutableList;
import dev.nokee.runtime.nativebase.TargetMachine;
import dev.nokee.runtime.nativebase.internal.DefaultTargetMachineFactory;
import lombok.AccessLevel;
import lombok.Getter;
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

public class TargetMachineRule implements Action<Project> {
	private final ToolChainSelectorInternal toolChainSelector;
	private final SetProperty<TargetMachine> targetMachines;
	@Getter(AccessLevel.PROTECTED) private final ObjectFactory objects;

	@Inject
	public TargetMachineRule(SetProperty<TargetMachine> targetMachines, String componentName, ObjectFactory objects) {
		this.targetMachines = targetMachines;
		this.objects = objects;
		this.toolChainSelector = objects.newInstance(ToolChainSelectorInternal.class);
		targetMachines.convention(ImmutableList.of(DefaultTargetMachineFactory.host()));
	}

	@Override
	public void execute(Project project) {
		this.targetMachines.disallowChanges();
		this.targetMachines.finalizeValue();
		Set<TargetMachine> targetMachines = this.targetMachines.get();
		assertTargetMachinesAreKnown(targetMachines);
	}

	private void assertTargetMachinesAreKnown(Collection<TargetMachine> targetMachines) {
		List<TargetMachine> unknownTargetMachines = targetMachines.stream().filter(it -> !toolChainSelector.isKnown(it)).collect(Collectors.toList());
		if (!unknownTargetMachines.isEmpty()) {
			throw new IllegalArgumentException("The following target machines are not know by the defined tool chains:\n" + unknownTargetMachines.stream().map(it -> " * " + it.getOperatingSystemFamily().getCanonicalName() + " " + it.getArchitecture().getCanonicalName()).collect(joining("\n")));
		}
	}
}
