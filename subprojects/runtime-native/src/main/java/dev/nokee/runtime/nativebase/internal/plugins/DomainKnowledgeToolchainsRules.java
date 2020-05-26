package dev.nokee.runtime.nativebase.internal.plugins;

import org.gradle.model.Finalize;
import org.gradle.model.RuleSource;
import org.gradle.nativeplatform.toolchain.Gcc;
import org.gradle.nativeplatform.toolchain.NativeToolChainRegistry;

public class DomainKnowledgeToolchainsRules extends RuleSource {
	@Finalize
	public void addGcc8(NativeToolChainRegistry toolChains) {
		toolChains.create("gcc8", Gcc.class, toolchain -> {
			toolchain.eachPlatform(platform -> {
				platform.getcCompiler().setExecutable("gcc-8");
				platform.getCppCompiler().setExecutable("g++-8");
				platform.getObjcCompiler().setExecutable("gcc-8");
				platform.getObjcppCompiler().setExecutable("g++-8");
				platform.getAssembler().setExecutable("gcc-8");
				platform.getStaticLibArchiver().setExecutable("gcc-ar-8");
				platform.getLinker().setExecutable("g++-8");
			});
		});
	}
}
