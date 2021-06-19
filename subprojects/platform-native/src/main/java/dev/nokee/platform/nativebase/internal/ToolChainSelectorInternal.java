package dev.nokee.platform.nativebase.internal;

import dev.nokee.runtime.nativebase.MachineArchitecture;
import dev.nokee.runtime.nativebase.OperatingSystemFamily;
import dev.nokee.runtime.nativebase.TargetMachine;
import lombok.val;
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
import org.gradle.nativeplatform.toolchain.internal.swift.SwiftcToolChain;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

public class ToolChainSelectorInternal {
	private static final Map<TargetMachine, Boolean> KNOWN_TOOLCHAINS = new ConcurrentHashMap<>();
	private final ModelRegistry modelRegistry;
	private final NativePlatformFactory nativePlatformFactory = new NativePlatformFactory();

	@Inject
	public ToolChainSelectorInternal(ModelRegistry modelRegistry) {
		this.modelRegistry = modelRegistry;
	}

	public boolean isKnown(TargetMachine targetMachine) {
		// Only cache known toolchains... assuming no-one configured the toolchain.
		Boolean result = KNOWN_TOOLCHAINS.get(targetMachine);
		if (result == null) {
			result = getToolChains().stream().anyMatch(it -> it.knows(targetMachine));
			if (result) {
				KNOWN_TOOLCHAINS.put(targetMachine, result);
			}
		}
		return result;
	}

	public boolean canBuild(TargetMachine targetMachine) {
		return getToolChains().stream().anyMatch(it -> it.canBuild(targetMachine));
	}

	public NativeToolChainInternal select(TargetMachine targetMachine) {
		NativePlatformInternal targetPlatform = nativePlatformFactory.create(targetMachine);
		NativeToolChainRegistryInternal registry = modelRegistry.realize("toolChains", NativeToolChainRegistryInternal.class);
		NativeToolChainInternal toolChain = (NativeToolChainInternal)registry.getForPlatform(targetPlatform);
		toolChain.assertSupported();

		return toolChain;
	}

	public NativeToolChainInternal selectSwift(TargetMachine targetMachine) {
		NativePlatformInternal targetPlatform = nativePlatformFactory.create(targetMachine);
		// HACK(daniel): Supa hacka! The way native platform match with the toolchain for Swift is a bit special.
		//  By using the "host" platform we can have the Swift toolchain selection happen. ;-)
		if (targetMachine.getOperatingSystemFamily().getCanonicalName().equals(OperatingSystemFamily.IOS)) {
			targetPlatform = DefaultNativePlatform.host();
		}
		NativeToolChainRegistryInternal registry = modelRegistry.realize("toolChains", NativeToolChainRegistryInternal.class);
		NativeToolChainInternal toolChain = (NativeToolChainInternal)registry.getForPlatform(NativeLanguage.SWIFT, targetPlatform);
		toolChain.assertSupported();

		return toolChain;
	}

	private Collection<ToolChain> getToolChains() {
		val result = new ArrayList<ToolChain>();
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

			NativeLanguage nativeLanguage = NativeLanguage.CPP;
			if (delegate instanceof SwiftcToolChain) {
				nativeLanguage = NativeLanguage.SWIFT;
			}

			PlatformToolProvider toolProvider = delegate.select(nativeLanguage, targetPlatform);
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

			NativeLanguage nativeLanguage = NativeLanguage.CPP;
			if (delegate instanceof SwiftcToolChain) {
				nativeLanguage = NativeLanguage.SWIFT;
			}

			PlatformToolProvider toolProvider = delegate.select(nativeLanguage, targetPlatform);
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
			return asList(MachineArchitecture.X86, MachineArchitecture.X86_64).contains(targetMachine.getArchitecture().getCanonicalName());
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
			if (DefaultNativePlatform.getCurrentOperatingSystem().toFamilyName().equals(targetMachine.getOperatingSystemFamily().getCanonicalName())) {
				return true;
			}
			return false;
		}
	}
}
