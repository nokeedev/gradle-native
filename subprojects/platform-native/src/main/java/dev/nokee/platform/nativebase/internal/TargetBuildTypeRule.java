package dev.nokee.platform.nativebase.internal;

import com.google.common.collect.ImmutableList;
import dev.nokee.runtime.nativebase.TargetBuildType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.attributes.AttributeCompatibilityRule;
import org.gradle.api.attributes.AttributeDisambiguationRule;
import org.gradle.api.attributes.CompatibilityCheckDetails;
import org.gradle.api.attributes.MultipleCandidatesDetails;
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
		targetBuildTypes.convention(ImmutableList.of(DefaultTargetBuildTypeFactory.DEFAULT));

		getDependencies().getAttributesSchema().attribute(BaseTargetBuildType.BUILD_TYPE_ATTRIBUTE).getDisambiguationRules().add(BuildTypeSelectionRule.class);
		getDependencies().getAttributesSchema().attribute(BaseTargetBuildType.BUILD_TYPE_ATTRIBUTE).getCompatibilityRules().add(BuildTypeCompatibilityRule.class);
	}

	static class BuildTypeCompatibilityRule implements AttributeCompatibilityRule<String> {
		@Override
		public void execute(CompatibilityCheckDetails<String> details) {
			details.compatible(); // Just mark everything as compatible and let the selection rule take over
		}
	}

	static class BuildTypeSelectionRule implements AttributeDisambiguationRule<String> {
		@Override
		public void execute(MultipleCandidatesDetails<String> details) {
			if (details.getConsumerValue() != null && details.getCandidateValues().contains(details.getConsumerValue())) {
				// Select the exact maching value, if available.
				details.closestMatch(details.getConsumerValue());
			} else if (details.getConsumerValue() != null && details.getCandidateValues().size() == 1) {
				// If only one candidate, let's assume it's a match.
				// In theory build type should just dictate debuggability and optimizability which should be interchangeable.
				details.closestMatch(details.getCandidateValues().iterator().next());
			} else {
				// TODO: Should also include something that contains debug
				// Choose something that is close to "debug"
				val firstMatchingDebugBuildType = details.getCandidateValues().stream().filter(it -> it.toLowerCase().equals("debug")).findFirst();
				firstMatchingDebugBuildType.ifPresent(details::closestMatch);
			}
		}
	}

	@Override
	public void execute(Project project) {
		this.targetBuildTypes.disallowChanges();
		this.targetBuildTypes.finalizeValue();
	}
}
