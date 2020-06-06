//package dev.nokee.platform.nativebase.internal.rules;
//
//import dev.nokee.platform.base.Variant;
//import dev.nokee.platform.base.internal.KnownVariant;
//import dev.nokee.platform.base.internal.NamingScheme;
//import dev.nokee.platform.base.internal.VariantProvider;
//import dev.nokee.platform.nativebase.NativeLibrary;
//import dev.nokee.platform.nativebase.SharedLibraryBinary;
//import dev.nokee.platform.nativebase.internal.*;
//import dev.nokee.platform.nativebase.tasks.LinkSharedLibrary;
//import dev.nokee.platform.nativebase.tasks.internal.LinkSharedLibraryTask;
//import dev.nokee.runtime.nativebase.internal.DefaultMachineArchitecture;
//import dev.nokee.runtime.nativebase.internal.DefaultOperatingSystemFamily;
//import dev.nokee.runtime.nativebase.internal.DefaultTargetMachine;
//import org.gradle.api.Action;
//import org.gradle.api.artifacts.Configuration;
//import org.gradle.api.artifacts.ConfigurationContainer;
//import org.gradle.api.file.ProjectLayout;
//import org.gradle.api.file.RegularFile;
//import org.gradle.api.model.ObjectFactory;
//import org.gradle.api.provider.Provider;
//
//import javax.inject.Inject;
//
//import static dev.nokee.platform.nativebase.internal.BaseNativeComponent.one;
//
//public abstract class OutgoingNativeDependenciesRule implements Action<BaseNativeComponent<?>> {
//	private final ConfigurationUtils builder = getObjects().newInstance(ConfigurationUtils.class);
//
//	@Override
//	public void execute(BaseNativeComponent<? extends Variant> component) {
//		component.getVariantCollection().whenElementKnown(DefaultNativeLibraryVariant.class, onKnownVariant(component));
//	}
//
//	public Action<KnownVariant<? extends DefaultNativeLibraryVariant>> onKnownVariant(BaseNativeComponent<?> component) {
//		return knownVariant -> {
//			NamingScheme names = component.getNames().forBuildVariant(knownVariant.getBuildVariant(), component.getBuildVariants().get());
//
////			Configuration api = getConfigurations().create(names.getConfigurationName("api"), ConfigurationUtils::configureAsBucket);
////			Configuration implementation = getConfigurations().create(names.getConfigurationName("implementation"), ConfigurationUtils::configureAsBucket);
////			Configuration compileOnly = getConfigurations().create(names.getConfigurationName("compileOnly"), ConfigurationUtils::configureAsBucket);
////			Configuration linkOnly = getConfigurations().create(names.getConfigurationName("linkOnly"), ConfigurationUtils::configureAsBucket);
////			Configuration runtimeOnly = getConfigurations().create(names.getConfigurationName("runtimeOnly"), ConfigurationUtils::configureAsBucket);
//
//
//			DefaultTargetMachine targetMachine = new DefaultTargetMachine(knownVariant.getBuildVariant().getAxisValue(DefaultOperatingSystemFamily.DIMENSION_TYPE), knownVariant.getBuildVariant().getAxisValue(DefaultMachineArchitecture.DIMENSION_TYPE));
//			Configuration compileElements = getConfigurations().create(names.getConfigurationName("compileElements"), builder.asOutgoingHeaderSearchPathFrom(api, compileOnly));
//			Configuration linkElements = getConfigurations().create(names.getConfigurationName("linkElements"), builder.asOutgoingHeaderSearchPathFrom(api, linkOnly).withSharedLinkage().forTargetMachine(targetMachine));
//			Configuration runtimeElements = getConfigurations().create(names.getConfigurationName("runtimeElements"), builder.asOutgoingHeaderSearchPathFrom(implementation, runtimeOnly).withSharedLinkage().forTargetMachine(targetMachine));
//
//			compileElements.getOutgoing().artifact(getLayout().getProjectDirectory().file("src/main/public"));
//			linkElements.getOutgoing().artifact(knownVariant.flatMap(this::getOutgoingLinkLibrary));
//			runtimeElements.getOutgoing().artifact(knownVariant.flatMap(this::getOutgoingRuntimeLibrary));
//		};
//	}
//
//	private Provider<RegularFile> getOutgoingLinkLibrary(DefaultNativeLibraryVariant variant) {
//		if (variant.getBuildVariant().getAxisValue(DefaultOperatingSystemFamily.DIMENSION_TYPE).isWindows()) {
//			return one(variant.getBinaries().withType(SharedLibraryBinary.class).get()).getLinkTask().flatMap(it -> ((LinkSharedLibraryTask) it).getImportLibrary());
//		}
//		return one(variant.getBinaries().withType(SharedLibraryBinary.class).get()).getLinkTask().flatMap(LinkSharedLibrary::getLinkedFile);
//	}
//
//	private Provider<RegularFile> getOutgoingRuntimeLibrary(DefaultNativeLibraryVariant variant) {
//		return one(variant.getBinaries().withType(SharedLibraryBinary.class).get()).getLinkTask().flatMap(LinkSharedLibrary::getLinkedFile);
//	}
//
//	@Inject
//	protected abstract ConfigurationContainer getConfigurations();
//
//	@Inject
//	protected abstract ObjectFactory getObjects();
//
//	@Inject
//	protected abstract ProjectLayout getLayout();
//}
