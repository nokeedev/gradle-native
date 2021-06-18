package dev.nokee.runtime.nativebase.internal;

import dev.nokee.runtime.nativebase.BuildType;
import org.gradle.api.attributes.AttributeCompatibilityRule;
import org.gradle.api.attributes.CompatibilityCheckDetails;

import javax.inject.Inject;

/*final*/ abstract class BuildTypeCompatibilityRule implements AttributeCompatibilityRule<BuildType> {
	@Inject
	public BuildTypeCompatibilityRule() {}

	@Override
	public void execute(CompatibilityCheckDetails<BuildType> details) {
		details.compatible(); // Just mark everything as compatible and let the selection rule take over
	}
}
