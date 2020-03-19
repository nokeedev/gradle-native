package dev.nokee.platform.nativebase.internal;

import dev.nokee.platform.nativebase.TargetMachine;
import org.apache.commons.lang3.SystemUtils;
import org.gradle.internal.os.OperatingSystem;
import org.gradle.model.internal.registry.ModelRegistry;
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform;
import org.gradle.nativeplatform.platform.internal.NativePlatformInternal;
import org.gradle.nativeplatform.toolchain.internal.NativeLanguage;
import org.gradle.nativeplatform.toolchain.internal.NativeToolChainInternal;
import org.gradle.nativeplatform.toolchain.internal.NativeToolChainRegistryInternal;
import org.gradle.nativeplatform.toolchain.internal.PlatformToolProvider;
import org.gradle.nativeplatform.toolchain.internal.msvcpp.VisualCppToolChain;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

public class ToolChainSelectorInternal {
	private final ModelRegistry modelRegistry;
	private final NativePlatformFactory nativePlatformFactory = new NativePlatformFactory();

	@Inject
	public ToolChainSelectorInternal(ModelRegistry modelRegistry) {
		this.modelRegistry = modelRegistry;
	}

	public boolean isKnown(TargetMachine targetMachine) {
		return getToolChains().stream().anyMatch(it -> it.knows(targetMachine));
	}

	public boolean canBuild(TargetMachine targetMachine) {
		return getToolChains().stream().anyMatch(it -> it.canBuild(targetMachine));
	}

	private Collection<ToolChain> getToolChains() {
		List<ToolChain> result = new ArrayList<>();
		result.add(new DomainKnowledgeToolChain());
		result.addAll(modelRegistry.realize("toolChains", NativeToolChainRegistryInternal.class).withType(NativeToolChainInternal.class).stream().map(SoftwareModelToolChain::new).collect(Collectors.toList()));
		return result;
	}

	private interface ToolChain {
		boolean knows(TargetMachine targetMachine);

		boolean canBuild(TargetMachine targetMachine);
	}

	private class SoftwareModelToolChain implements ToolChain {
		private final NativeToolChainInternal delegate;

		private SoftwareModelToolChain(NativeToolChainInternal delegate) {
			this.delegate = delegate;
		}

		@Override
		public boolean knows(TargetMachine targetMachine) {
			NativePlatformInternal targetPlatform = nativePlatformFactory.create(targetMachine);

			PlatformToolProvider toolProvider = delegate.select(NativeLanguage.CPP, targetPlatform);
			if (toolProvider.isAvailable()) {
				return true;
			}

			// Code an exception here as VisualCpp will return an unavailable (not unsupported) when the tool is not available
			// For VisualCpp toolchain where target is not Windows, return not supported.
			if (!toolProvider.isSupported() || (!targetPlatform.getOperatingSystem().isWindows() && delegate instanceof VisualCppToolChain)) {
				return false;
			}
			return true;
		}

		@Override
		public boolean canBuild(TargetMachine targetMachine) {
			NativePlatformInternal targetPlatform = nativePlatformFactory.create(targetMachine);

			PlatformToolProvider toolProvider = delegate.select(NativeLanguage.CPP, targetPlatform);
			if (toolProvider.isAvailable()) {
				return true;
			}
			return false;
		}
	}

	private static class DomainKnowledgeToolChain implements ToolChainSelectorInternal.ToolChain {
		@Override
		public boolean knows(TargetMachine targetMachine) {
			// Shortcut
			//   if we ...
			//      ... target linux and current host is not Linux
			//      or
			//      ... target macOS and current host is not macOS
			//   we know it is known and we should be able to compile for it
			if ((isTargetingLinuxButNotLinux(targetMachine) || isTargetingMacOsButNotMacOs(targetMachine) || isTargetingFreeBsdButNotFreeBsd(targetMachine) || isTargetingWindowsButNotWindows(targetMachine)) && isKnownArchitecture(targetMachine)) {
				return true;
			}
			return false;
		}

		private static boolean isKnownArchitecture(TargetMachine targetMachine) {
			return asList(DefaultMachineArchitecture.X86, DefaultMachineArchitecture.X86_64).contains(targetMachine.getArchitecture());
		}

		private static boolean isTargetingMacOsButNotMacOs(TargetMachine targetMachine) {
			return targetMachine.getOperatingSystemFamily().isMacOs() && !OperatingSystem.current().isMacOsX();
		}

		private static boolean isTargetingLinuxButNotLinux(TargetMachine targetMachine) {
			return targetMachine.getOperatingSystemFamily().isLinux() && !SystemUtils.IS_OS_LINUX;
		}

		private static boolean isTargetingFreeBsdButNotFreeBsd(TargetMachine targetMachine) {
			return targetMachine.getOperatingSystemFamily().isFreeBSD() && !SystemUtils.IS_OS_FREE_BSD;
		}

		private static boolean isTargetingWindowsButNotWindows(TargetMachine targetMachine) {
			return targetMachine.getOperatingSystemFamily().isWindows() && !SystemUtils.IS_OS_WINDOWS;
		}

		@Override
		public boolean canBuild(TargetMachine targetMachine) {
			// Shortcut, we need to model target host for the tool chain and their target platform
			//    We assume the same host should always be buildable and supported
			if (DefaultNativePlatform.getCurrentOperatingSystem().toFamilyName().equals(((DefaultOperatingSystemFamily)targetMachine.getOperatingSystemFamily()).getName())) {
				return true;
			}
			return false;
		}
	}
}
