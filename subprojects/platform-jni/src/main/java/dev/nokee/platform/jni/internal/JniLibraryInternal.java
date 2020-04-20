package dev.nokee.platform.jni.internal;

import dev.nokee.language.base.internal.LanguageSourceSetInternal;
import dev.nokee.platform.base.internal.BinaryInternal;
import dev.nokee.platform.base.internal.GroupId;
import dev.nokee.platform.jni.JniLibrary;
import dev.nokee.platform.nativebase.internal.ConfigurationUtils;
import dev.nokee.platform.nativebase.internal.DefaultTargetMachine;
import dev.nokee.platform.nativebase.internal.SharedLibraryBinaryInternal;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileCollection;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.jvm.tasks.Jar;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public abstract class JniLibraryInternal implements JniLibrary {
	private final NamingScheme names;
	private final DomainObjectSet<? super BinaryInternal> binaries;
	private final DomainObjectSet<? super LanguageSourceSetInternal> sources;
	private final Configuration implementation;
	private final DefaultTargetMachine targetMachine;
	private final GroupId groupId;
	private final ConfigurationContainer configurations;
	private final Configuration nativeRuntime;
	private JniJarBinaryInternal jarBinary;
	private Optional<SharedLibraryBinaryInternal> sharedLibraryBinary = Optional.empty();
	private final TaskProvider<Task> assembleTask;

	@Inject
	public JniLibraryInternal(TaskContainer tasks, NamingScheme names, ObjectFactory objectFactory, ProviderFactory providers, ConfigurationContainer configurations, DomainObjectSet<? super LanguageSourceSetInternal> sources, Configuration implementation, DefaultTargetMachine targetMachine, GroupId groupId) {
		this.names = names;
		binaries = objectFactory.domainObjectSet(BinaryInternal.class);
		this.configurations = configurations;
		this.sources = sources;
		this.implementation = implementation;
		this.targetMachine = targetMachine;
		this.groupId = groupId;

		ConfigurationUtils configurationUtils = objectFactory.newInstance(ConfigurationUtils.class);
		nativeRuntime = configurations.create(names.getConfigurationName("nativeRuntime"), configurationUtils.asIncomingRuntimeLibrariesFrom(implementation).forTargetMachine(targetMachine).asDebug());

		getNativeRuntimeFiles().from(nativeRuntime);
		getResourcePath().convention(providers.provider(() -> names.getResourcePath(groupId)));

		this.assembleTask = registerAssembleTaskIfAbsent(tasks);
		assembleTask.configure(task -> {
			task.dependsOn((Callable<List<TaskProvider<?>>>) () -> {
				List<TaskProvider<?>> result = new ArrayList<>();
				result.addAll(sharedLibraryBinary.map(it -> singletonList(it.getLinkTask())).orElse(emptyList()));
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

	public DomainObjectSet<? super BinaryInternal> getBinaries() {
		return binaries;
	}

	public void registerSharedLibraryBinary() {
		SharedLibraryBinaryInternal sharedLibraryBinary = getObjectFactory().newInstance(SharedLibraryBinaryInternal.class, names, configurations, sources, implementation, targetMachine);
		getNativeRuntimeFiles().from(sharedLibraryBinary.getLinkedFile());
		this.sharedLibraryBinary = Optional.of(sharedLibraryBinary);
		binaries.add(sharedLibraryBinary);
	}

	public void registerJniJarBinary() {
		TaskProvider<Jar> jarTask = getTasks().register(names.getTaskName("jar"), Jar.class, task -> {
			task.getArchiveBaseName().set(names.getBaseName().withKababDimensions());
		});
		registerJniJarBinary(jarTask);
	}

	public JniJarBinaryInternal getJar() {
		return jarBinary;
	}

	public Optional<SharedLibraryBinaryInternal> getSharedLibrary() {
		return sharedLibraryBinary;
	}

	public FileCollection getNativeRuntimeDependencies() {
		return nativeRuntime;
	}

	public abstract ConfigurableFileCollection getNativeRuntimeFiles();

	public void registerJniJarBinary(TaskProvider<Jar> jarTask) {
		jarBinary = getObjectFactory().newInstance(JniJarBinaryInternal.class, jarTask);
		binaries.add(jarBinary);
	}

	public DefaultTargetMachine getTargetMachine() {
		return targetMachine;
	}

	public TaskProvider<Task> getAssembleTask() {
		return assembleTask;
	}
}
