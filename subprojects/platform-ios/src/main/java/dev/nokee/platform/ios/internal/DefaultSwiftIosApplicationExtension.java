package dev.nokee.platform.ios.internal;

import com.google.common.collect.ImmutableList;
import dev.nokee.language.base.internal.DefaultSourceSet;
import dev.nokee.language.swift.internal.UTTypeSwiftSource;
import dev.nokee.platform.base.internal.BuildVariant;
import dev.nokee.platform.base.internal.DefaultBuildVariant;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.ios.SwiftIosApplicationExtension;
import dev.nokee.runtime.nativebase.internal.*;
import org.gradle.api.Project;

import javax.inject.Inject;

public abstract class DefaultSwiftIosApplicationExtension extends BaseNativeApplicationComponent implements SwiftIosApplicationExtension {
	@Inject
	public DefaultSwiftIosApplicationExtension(DefaultNativeComponentDependencies dependencies, NamingScheme names) {
		super(dependencies, names);
		getSourceCollection().add(getObjects().newInstance(DefaultSourceSet.class, new UTTypeSwiftSource()).srcDir("src/main/swift"));
	}

	@Override
	protected Iterable<BuildVariant> createBuildVariants() {
		return ImmutableList.of(DefaultBuildVariant.of(DefaultOperatingSystemFamily.forName("ios"), DefaultMachineArchitecture.X86_64));
	}

	@Override
	public void finalizeExtension(Project project) {
		super.finalizeExtension(project);
		getBuildVariants().get().forEach(buildVariant -> {
			getTasks().register("bundle");
		});
	}
}
