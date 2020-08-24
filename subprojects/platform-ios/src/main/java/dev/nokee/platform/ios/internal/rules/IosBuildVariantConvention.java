package dev.nokee.platform.ios.internal.rules;

import com.google.common.collect.ImmutableList;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.DefaultBuildVariant;
import dev.nokee.platform.nativebase.internal.DefaultBinaryLinkage;
import dev.nokee.runtime.nativebase.internal.DefaultMachineArchitecture;
import dev.nokee.runtime.nativebase.internal.DefaultOperatingSystemFamily;

import java.util.concurrent.Callable;

public class IosBuildVariantConvention implements Callable<Iterable<BuildVariantInternal>> {
	@Override
	public Iterable<BuildVariantInternal> call() throws Exception {
		return ImmutableList.of(DefaultBuildVariant.of(DefaultOperatingSystemFamily.forName("ios"), DefaultMachineArchitecture.X86_64, DefaultBinaryLinkage.EXECUTABLE));
	}
}
