package dev.nokee.testing.nativebase.internal;

import com.google.common.collect.ImmutableList;
import dev.nokee.language.base.internal.BaseSourceSet;
import dev.nokee.language.base.internal.UTTypeUtils;
import dev.nokee.language.c.internal.CHeaderSet;
import dev.nokee.language.cpp.internal.CppHeaderSet;
import dev.nokee.language.nativebase.internal.UTTypeObjectCode;
import dev.nokee.platform.base.internal.BaseComponent;
import dev.nokee.platform.base.internal.BuildVariant;
import dev.nokee.platform.base.internal.DefaultBuildVariant;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.nativebase.ExecutableBinary;
import dev.nokee.platform.nativebase.NativeBinary;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.platform.nativebase.internal.BaseNativeComponent;
import dev.nokee.platform.nativebase.internal.BaseNativeExtension;
import dev.nokee.platform.nativebase.internal.DefaultBinaryLinkage;
import dev.nokee.platform.nativebase.internal.dependencies.AbstractBinaryAwareNativeComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeComponentDependencies;
import dev.nokee.platform.nativebase.tasks.internal.LinkExecutableTask;
import dev.nokee.runtime.nativebase.internal.DefaultMachineArchitecture;
import dev.nokee.runtime.nativebase.internal.DefaultOperatingSystemFamily;
import dev.nokee.testing.base.TestSuiteComponent;
import dev.nokee.testing.nativebase.NativeTestSuite;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.provider.Property;
import org.gradle.language.nativeplatform.tasks.AbstractNativeSourceCompileTask;
import org.gradle.language.nativeplatform.tasks.UnexportMainSymbol;

import javax.inject.Inject;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

public abstract class DefaultNativeTestSuiteComponent extends BaseNativeComponent<DefaultNativeTestSuiteVariant> implements NativeTestSuite {
	private final DefaultNativeComponentDependencies dependencies;

	@Inject
	public DefaultNativeTestSuiteComponent(NamingScheme names) {
		super(names, DefaultNativeTestSuiteVariant.class);

		this.dependencies = getObjects().newInstance(DefaultNativeComponentDependencies.class, getNames());
		this.getDimensions().convention(ImmutableList.of(DefaultOperatingSystemFamily.DIMENSION_TYPE, DefaultBinaryLinkage.DIMENSION_TYPE, DefaultMachineArchitecture.DIMENSION_TYPE));

		this.getBuildVariants().convention(getProviders().provider(this::createBuildVariants));
		this.getBuildVariants().finalizeValueOnRead();
		this.getBuildVariants().disallowChanges(); // Let's disallow changing them for now.

		this.getDimensions().disallowChanges(); // Let's disallow changing them for now.
	}

	@Override
	public String getName() {
		return getNames().getComponentName();
	}

	protected Iterable<BuildVariant> createBuildVariants() {
		if (getTestedComponent().isPresent()) {
			val buildVariantBuilder = new LinkedHashSet<BuildVariant>();
			for (val buildVariant : getTestedComponent().get().getBuildVariants().get()) {
				val dimensionValues = buildVariant.getDimensions().stream().map(it -> {
					if (it instanceof DefaultBinaryLinkage) {
						return DefaultBinaryLinkage.EXECUTABLE;
					}
					return it;
				}).collect(Collectors.toList());

				buildVariantBuilder.add(DefaultBuildVariant.of(dimensionValues));
			}
			return ImmutableList.copyOf(buildVariantBuilder);
		}
		throw new UnsupportedOperationException();
	}

	@Override
	public DefaultNativeComponentDependencies getDependencies() {
		return dependencies;
	}

	@Override
	public void dependencies(Action<? super NativeComponentDependencies> action) {
		action.execute(dependencies);
	}

	@Override
	protected DefaultNativeTestSuiteVariant createVariant(String name, BuildVariant buildVariant, AbstractBinaryAwareNativeComponentDependencies variantDependencies) {
		NamingScheme names = getNames().forBuildVariant(buildVariant, getBuildVariants().get());

		val result = getObjects().newInstance(DefaultNativeTestSuiteVariant.class, name, names, buildVariant, variantDependencies);
		return result;
	}

	public abstract Property<BaseComponent<?>> getTestedComponent();

	@Override
	public TestSuiteComponent testedComponent(Object component) {
		if (component instanceof BaseNativeExtension) {
			getTestedComponent().set(((BaseNativeExtension) component).getComponent());
		} else if (component instanceof BaseComponent) {
			getTestedComponent().set((BaseComponent) component);
		} else {
			throw new UnsupportedOperationException();
		}
		return this;
	}

	@Override
	public void finalizeExtension(Project project) {
		super.finalizeExtension(project);
		getTestedComponent().disallowChanges();
		if (getTestedComponent().isPresent()) {
			val component = getTestedComponent().get();
			component.getSourceCollection().withType(BaseSourceSet.class).configureEach(sourceSet -> {
				if (getSourceCollection().withType(sourceSet.getClass()).isEmpty()) {
					getSourceCollection().add(getObjects().newInstance(sourceSet.getClass(), sourceSet.getName()).from(getNames().getSourceSetPath(sourceSet.getName())));
				}
			});
			if (component instanceof BaseNativeComponent) {
				getDependencies().getImplementationDependencies().extendsFrom(((BaseNativeComponent<?>) component).getDependencies().getImplementationDependencies());
			}
			getBinaries().configureEach(ExecutableBinary.class, binary -> {
				val relocateTask = getTasks().register(getNames().getTaskName("relocateMainSymbolFor"), UnexportMainSymbol.class, task -> {
					task.getObjects().from(component.getBinaries().withType(NativeBinary.class).flatMap(it -> {
						return it.getCompileTasks().getElements().map(t -> {
							return t.stream().map(a -> {
								return ((AbstractNativeSourceCompileTask) a).getObjectFileDir().getAsFileTree().matching(UTTypeUtils.onlyIf(UTTypeObjectCode.INSTANCE));
							}).collect(Collectors.toList());
						}).get();
					}));
					task.getOutputDirectory().set(project.getLayout().getBuildDirectory().dir("objs/for-test/" + getNames().getComponentName()));
				});
				binary.getLinkTask().configure(task -> {
					val taskInternal = (LinkExecutableTask)task;
					taskInternal.source(relocateTask.map(UnexportMainSymbol::getRelocatedObjects));
				});
			});
			getSourceCollection().addAll(component.getSourceCollection().matching(it -> it instanceof CppHeaderSet || it instanceof CHeaderSet));
		}
	}
}
