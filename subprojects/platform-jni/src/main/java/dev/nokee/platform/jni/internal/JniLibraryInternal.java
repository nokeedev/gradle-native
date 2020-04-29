package dev.nokee.platform.jni.internal;

import com.google.common.collect.ImmutableList;
import dev.nokee.language.base.internal.LanguageSourceSetInternal;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.jni.JniJarBinary;
import dev.nokee.platform.jni.JniLibrary;
import dev.nokee.platform.nativebase.SharedLibraryBinary;
import dev.nokee.platform.nativebase.internal.ConfigurationUtils;
import dev.nokee.platform.nativebase.internal.DefaultTargetMachine;
import dev.nokee.platform.nativebase.internal.SharedLibraryBinaryInternal;
import org.gradle.api.Action;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.RegularFile;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.internal.Cast;
import org.gradle.jvm.tasks.Jar;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static dev.nokee.platform.base.internal.TaskUtils.dependsOn;

public abstract class JniLibraryInternal implements JniLibrary {
	private final NamingScheme names;
	private final DomainObjectSet<BinaryInternal> binaryCollection;
	private final DomainObjectSet<? super LanguageSourceSetInternal> sources;
	private final Configuration implementation;
	private final DefaultTargetMachine targetMachine;
	private final GroupId groupId;
	private final ConfigurationContainer configurations;
	private final Configuration nativeRuntime;
	private AbstractJarBinary jarBinary;
	private SharedLibraryBinaryInternal sharedLibraryBinary;
	private final TaskProvider<Task> assembleTask;

	@Inject
	public JniLibraryInternal(TaskContainer tasks, NamingScheme names, ObjectFactory objectFactory, ProviderFactory providers, ConfigurationContainer configurations, DomainObjectSet<? super LanguageSourceSetInternal> sources, Configuration implementation, DefaultTargetMachine targetMachine, GroupId groupId, DomainObjectSet<BinaryInternal> parentBinaries) {
		this.names = names;
		binaryCollection = objectFactory.domainObjectSet(BinaryInternal.class);
		this.configurations = configurations;
		this.sources = sources;
		this.implementation = implementation;
		this.targetMachine = targetMachine;
		this.groupId = groupId;

		binaryCollection.configureEach( binary -> {
			parentBinaries.add(binary);
		});

		ConfigurationUtils configurationUtils = objectFactory.newInstance(ConfigurationUtils.class);
		nativeRuntime = configurations.create(names.getConfigurationName("nativeRuntime"), configurationUtils.asIncomingRuntimeLibrariesFrom(implementation).forTargetMachine(targetMachine).asDebug());

		getNativeRuntimeFiles().from(nativeRuntime);
		getResourcePath().convention(providers.provider(() -> names.getResourcePath(groupId)));

		this.assembleTask = registerAssembleTaskIfAbsent(tasks);
		assembleTask.configure(task -> {
			task.dependsOn((Callable<List<TaskProvider<?>>>) () -> {
				List<TaskProvider<?>> result = new ArrayList<>();
				result.add(sharedLibraryBinary.getLinkTask());
				result.add(jarBinary.getJarTask());
				return result;
			});
		});
	}

	private TaskProvider<Task> registerAssembleTaskIfAbsent(TaskContainer tasks) {
		String assembleTaskName = names.getTaskName("assemble");
		if (assembleTaskName.equals("assemble")) {
			return tasks.named(assembleTaskName);
		}
		return tasks.register(assembleTaskName);
	}

	@Inject
	protected abstract ObjectFactory getObjectFactory();

	@Inject
	protected abstract TaskContainer getTasks();

	public BinaryView<Binary> getBinaries() {
		return Cast.uncheckedCast(getObjectFactory().newInstance(DefaultBinaryView.class, binaryCollection));
	}

	public void registerSharedLibraryBinary() {
		SharedLibraryBinaryInternal sharedLibraryBinary = getObjectFactory().newInstance(SharedLibraryBinaryInternal.class, names, configurations, sources, implementation, targetMachine);
		getNativeRuntimeFiles().from((Callable<List<Provider<RegularFile>>>)() -> {
			if (sharedLibraryBinary.getLinkedFile().isPresent()) {
				return ImmutableList.of(sharedLibraryBinary.getLinkedFile());
			}
			return ImmutableList.of();
		});
		this.sharedLibraryBinary = sharedLibraryBinary;
		binaryCollection.add(sharedLibraryBinary);
		assembleTask.configure(dependsOn(sharedLibraryBinary.getLinkTask()));
	}

	public void registerJniJarBinary() {
		TaskProvider<Jar> jarTask = getTasks().register(names.getTaskName("jar"), Jar.class, task -> {
			task.getArchiveBaseName().set(names.getBaseName().withKababDimensions());
		});
		addJniJarBinary(getObjectFactory().newInstance(DefaultJniJarBinary.class, jarTask));
	}

	public AbstractJarBinary getJar() {
		return jarBinary;
	}

	public SharedLibraryBinaryInternal getSharedLibrary() {
		return sharedLibraryBinary;
	}

	@Override
	public void sharedLibrary(Action<? super SharedLibraryBinary> action) {
		action.execute(sharedLibraryBinary);
	}

	public FileCollection getNativeRuntimeDependencies() {
		return nativeRuntime;
	}

	public abstract ConfigurableFileCollection getNativeRuntimeFiles();

	public void addJniJarBinary(AbstractJarBinary jniJarBinary) {
		jarBinary = jniJarBinary;
		binaryCollection.add(jniJarBinary);
		assembleTask.configure(dependsOn(jniJarBinary.getJarTask()));
	}

	public void addJvmJarBinary(DefaultJvmJarBinary jvmJarBinary) {
		binaryCollection.add(jvmJarBinary);
		assembleTask.configure(dependsOn(jvmJarBinary.getJarTask()));
	}

	public DefaultTargetMachine getTargetMachine() {
		return targetMachine;
	}

	public TaskProvider<Task> getAssembleTask() {
		return assembleTask;
	}
}
