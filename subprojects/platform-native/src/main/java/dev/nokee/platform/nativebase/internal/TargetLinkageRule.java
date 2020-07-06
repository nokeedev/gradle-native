package dev.nokee.platform.nativebase.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import dev.nokee.runtime.nativebase.TargetLinkage;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.attributes.AttributeDisambiguationRule;
import org.gradle.api.attributes.MultipleCandidatesDetails;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.SetProperty;

import javax.inject.Inject;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static java.util.stream.Collectors.joining;

public abstract class TargetLinkageRule implements Action<Project> {
	private final SetProperty<TargetLinkage> targetLinkages;
	private final String componentName;

	@Inject
	public TargetLinkageRule(SetProperty<TargetLinkage> targetLinkages, String componentName) {
		this.targetLinkages = targetLinkages;
		this.componentName = componentName;
		targetLinkages.convention(ImmutableList.of(DefaultBinaryLinkage.SHARED));

		getDependencies().getAttributesSchema().attribute(DefaultBinaryLinkage.LINKAGE_ATTRIBUTE).getDisambiguationRules().add(LinkageSelectionRule.class);
	}

	@Inject
	protected abstract ObjectFactory getObjects();

	@Inject
	protected abstract DependencyHandler getDependencies();

	static class LinkageSelectionRule implements AttributeDisambiguationRule<String> {
		@Override
		public void execute(MultipleCandidatesDetails<String> details) {
			if (details.getCandidateValues().contains(DefaultBinaryLinkage.SHARED.getName())) {
				details.closestMatch(DefaultBinaryLinkage.SHARED.getName());
			}
		}
	}

	@Override
	public void execute(Project project) {
		this.targetLinkages.disallowChanges();
		this.targetLinkages.finalizeValue();
		Set<TargetLinkage> targetLinkages = this.targetLinkages.get();
		assertNonEmpty(targetLinkages, "linkages", componentName);
		assertLinkagesAreSupported(targetLinkages);
	}

	private static void assertNonEmpty(Collection<?> values, String propertyName, String componentName) {
		if (values.isEmpty()) {
			throw new IllegalArgumentException(String.format("A %s needs to be specified for the %s.", propertyName, componentName));
		}
	}

	private static final Set<TargetLinkage> SUPPORTED_LINKAGES = ImmutableSet.of(DefaultBinaryLinkage.SHARED, DefaultBinaryLinkage.STATIC);
	private void assertLinkagesAreSupported(Collection<TargetLinkage> targetLinkages) {
		if (!targetLinkages.stream().allMatch(it -> SUPPORTED_LINKAGES.contains(it))) {
			val unknownLinkages = new HashSet<TargetLinkage>(targetLinkages);
			unknownLinkages.removeAll(SUPPORTED_LINKAGES);
			throw new IllegalArgumentException("The following linkages are not supported:\n" + unknownLinkages.stream().map(it -> " * " + ((DefaultBinaryLinkage) it).getName()).collect(joining("\n")));
		}
	}
}
