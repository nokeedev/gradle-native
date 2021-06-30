package dev.nokee.language.nativebase.internal.toolchains;

import org.gradle.api.internal.file.FileResolver;
import org.gradle.internal.operations.BuildOperationExecutor;
import org.gradle.internal.os.OperatingSystem;
import org.gradle.internal.reflect.Instantiator;
import org.gradle.internal.work.WorkerLeaseService;
import org.gradle.nativeplatform.internal.CompilerOutputFileNamingSchemeFactory;
import org.gradle.nativeplatform.platform.internal.NativePlatformInternal;
import org.gradle.nativeplatform.toolchain.internal.clang.ClangToolChain;
import org.gradle.nativeplatform.toolchain.internal.gcc.metadata.SystemLibraryDiscovery;
import org.gradle.nativeplatform.toolchain.internal.metadata.CompilerMetaDataProviderFactory;
import org.gradle.process.internal.ExecActionFactory;

public class NokeeClangToolChain extends ClangToolChain implements KnownAwareToolChain {
	public NokeeClangToolChain(String name, BuildOperationExecutor buildOperationExecutor, OperatingSystem operatingSystem, FileResolver fileResolver, ExecActionFactory execActionFactory, CompilerOutputFileNamingSchemeFactory compilerOutputFileNamingSchemeFactory, CompilerMetaDataProviderFactory metaDataProviderFactory, SystemLibraryDiscovery standardLibraryDiscovery, Instantiator instantiator, WorkerLeaseService workerLeaseService) {
		super(name, buildOperationExecutor, operatingSystem, fileResolver, execActionFactory, compilerOutputFileNamingSchemeFactory, metaDataProviderFactory, standardLibraryDiscovery, instantiator, workerLeaseService);
	}

	@Override
	public boolean isKnown(NativePlatformInternal nativePlatform) {
		return super.getPlatformConfiguration(nativePlatform) != null;
	}
}
