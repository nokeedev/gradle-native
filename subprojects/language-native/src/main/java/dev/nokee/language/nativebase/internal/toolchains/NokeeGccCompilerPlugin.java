package dev.nokee.language.nativebase.internal.toolchains;

import org.gradle.api.NamedDomainObjectFactory;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.internal.file.FileResolver;
import org.gradle.internal.operations.BuildOperationExecutor;
import org.gradle.internal.os.OperatingSystem;
import org.gradle.internal.reflect.Instantiator;
import org.gradle.internal.service.ServiceRegistry;
import org.gradle.internal.work.WorkerLeaseService;
import org.gradle.model.Defaults;
import org.gradle.model.RuleSource;
import org.gradle.nativeplatform.internal.CompilerOutputFileNamingSchemeFactory;
import org.gradle.nativeplatform.plugins.NativeComponentModelPlugin;
import org.gradle.nativeplatform.plugins.NativeComponentPlugin;
import org.gradle.nativeplatform.toolchain.Gcc;
import org.gradle.nativeplatform.toolchain.internal.NativeToolChainRegistryInternal;
import org.gradle.nativeplatform.toolchain.internal.gcc.GccToolChain;
import org.gradle.nativeplatform.toolchain.internal.gcc.metadata.SystemLibraryDiscovery;
import org.gradle.nativeplatform.toolchain.internal.metadata.CompilerMetaDataProviderFactory;
import org.gradle.process.internal.ExecActionFactory;

public class NokeeGccCompilerPlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(NativeComponentModelPlugin.class);
	}

	static class Rules extends RuleSource {
		@Defaults
		public static void addToolChain(NativeToolChainRegistryInternal toolChainRegistry, ServiceRegistry serviceRegistry) {
			final FileResolver fileResolver = serviceRegistry.get(FileResolver.class);
			final ExecActionFactory execActionFactory = serviceRegistry.get(ExecActionFactory.class);
			final CompilerOutputFileNamingSchemeFactory compilerOutputFileNamingSchemeFactory = serviceRegistry.get(CompilerOutputFileNamingSchemeFactory.class);

			final Instantiator instantiator = serviceRegistry.get(Instantiator.class);

			final BuildOperationExecutor buildOperationExecutor = serviceRegistry.get(BuildOperationExecutor.class);

			final CompilerMetaDataProviderFactory metaDataProviderFactory = serviceRegistry.get(CompilerMetaDataProviderFactory.class);

			final WorkerLeaseService workerLeaseService = serviceRegistry.get(WorkerLeaseService.class);
			final SystemLibraryDiscovery standardLibraryDiscovery = serviceRegistry.get(SystemLibraryDiscovery.class);

			toolChainRegistry.registerFactory(Gcc.class, new NamedDomainObjectFactory<Gcc>() {
				@Override
				public Gcc create(String name) {
					return instantiator.newInstance(NokeeGccToolChain.class, instantiator, name, buildOperationExecutor, OperatingSystem.current(), fileResolver, execActionFactory, compilerOutputFileNamingSchemeFactory, metaDataProviderFactory, standardLibraryDiscovery, workerLeaseService);
				}
			});
			toolChainRegistry.registerDefaultToolChain(GccToolChain.DEFAULT_NAME, Gcc.class);
		}

	}
}
