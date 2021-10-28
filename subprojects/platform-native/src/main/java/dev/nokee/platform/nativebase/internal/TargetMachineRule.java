/*
 * Copyright 2020-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.nokee.platform.nativebase.internal;

import com.google.common.collect.ImmutableList;
import dev.nokee.language.nativebase.internal.ToolChainSelectorInternal;
import dev.nokee.runtime.nativebase.TargetMachine;
import dev.nokee.runtime.nativebase.internal.TargetMachines;
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
		targetMachines.convention(ImmutableList.of(TargetMachines.host()));
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
