package dev.nokee.platform.nativebase.internal;

import com.google.common.collect.ImmutableList;
import dev.nokee.runtime.nativebase.TargetBuildType;
import dev.nokee.runtime.nativebase.internal.TargetBuildTypes;
import lombok.AccessLevel;
import lombok.Getter;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.SetProperty;

import javax.inject.Inject;

public class TargetBuildTypeRule implements Action<Project> {
	private final SetProperty<TargetBuildType> targetBuildTypes;
	@Getter(AccessLevel.PROTECTED) private final ObjectFactory objects;
	@Getter(AccessLevel.PROTECTED) private final DependencyHandler dependencies;

	@Inject
	public TargetBuildTypeRule(SetProperty<TargetBuildType> targetBuildTypes, String componentName, ObjectFactory objects, DependencyHandler dependencies) {
		this.targetBuildTypes = targetBuildTypes;
		this.objects = objects;
		this.dependencies = dependencies;
		targetBuildTypes.convention(ImmutableList.of(TargetBuildTypes.DEFAULT));
	}

	@Override
	public void execute(Project project) {
		this.targetBuildTypes.disallowChanges();
		this.targetBuildTypes.finalizeValue();
	}
}
