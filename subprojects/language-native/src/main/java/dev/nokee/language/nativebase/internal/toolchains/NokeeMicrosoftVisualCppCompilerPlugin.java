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
import org.gradle.nativeplatform.toolchain.VisualCpp;
import org.gradle.nativeplatform.toolchain.internal.NativeToolChainRegistryInternal;
import org.gradle.nativeplatform.toolchain.internal.msvcpp.UcrtLocator;
import org.gradle.nativeplatform.toolchain.internal.msvcpp.VisualCppToolChain;
import org.gradle.nativeplatform.toolchain.internal.msvcpp.VisualStudioLocator;
import org.gradle.nativeplatform.toolchain.internal.msvcpp.WindowsSdkLocator;
import org.gradle.process.internal.ExecActionFactory;

public class NokeeMicrosoftVisualCppCompilerPlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(NativeComponentModelPlugin.class);
	}

	static class Rules extends RuleSource {
		@Defaults
		public static void addToolChain(NativeToolChainRegistryInternal toolChainRegistry, ServiceRegistry serviceRegistry) {
			final FileResolver fileResolver = serviceRegistry.get(FileResolver.class);
			final ExecActionFactory execActionFactory = serviceRegistry.get(ExecActionFactory.class);
			final Instantiator instantiator = serviceRegistry.get(Instantiator.class);
			final OperatingSystem operatingSystem = serviceRegistry.get(OperatingSystem.class);
			final BuildOperationExecutor buildOperationExecutor = serviceRegistry.get(BuildOperationExecutor.class);
			final CompilerOutputFileNamingSchemeFactory compilerOutputFileNamingSchemeFactory = serviceRegistry.get(CompilerOutputFileNamingSchemeFactory.class);
			final VisualStudioLocator visualStudioLocator = serviceRegistry.get(VisualStudioLocator.class);
			final UcrtLocator ucrtLocator = serviceRegistry.get(UcrtLocator.class);
			final WindowsSdkLocator windowsSdkLocator = serviceRegistry.get(WindowsSdkLocator.class);
			final WorkerLeaseService workerLeaseService = serviceRegistry.get(WorkerLeaseService.class);

			toolChainRegistry.registerFactory(VisualCpp.class, new NamedDomainObjectFactory<VisualCpp>() {
				@Override
				public VisualCpp create(String name) {
					return instantiator.newInstance(VisualCppToolChain.class, name, buildOperationExecutor, operatingSystem, fileResolver, execActionFactory, compilerOutputFileNamingSchemeFactory, visualStudioLocator, windowsSdkLocator, ucrtLocator, instantiator, workerLeaseService);
				}
			});
			toolChainRegistry.registerDefaultToolChain(VisualCppToolChain.DEFAULT_NAME, VisualCpp.class);
		}

	}
}
