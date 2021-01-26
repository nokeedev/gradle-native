package dev.gradleplugins.documentationkit;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import lombok.SneakyThrows;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.file.FileCollection;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;

public class DependenciesLoader {
	private final DependencySerializer serializer;
	private final ConfigurationContainer configurations;

	private DependenciesLoader(DependencySerializer serializer, ConfigurationContainer configurations) {
		this.serializer = serializer;
		this.configurations = configurations;
	}

	public static DependenciesLoader forProject(Project project) {
		return new DependenciesLoader(new DependencySerializer(project.getDependencies()), project.getConfigurations());
	}

	public Callable<Object> load(File manifestFile) {
		return memoizeCallable(() -> asDetachedConfiguration(serializer.deserialize(manifestFile)));
	}

	private FileCollection asDetachedConfiguration(List<Dependency> dependencies) {
		return configurations.detachedConfiguration(dependencies.toArray(new Dependency[0])).getIncoming().getFiles();
	}

	private static <T> Callable<T> memoizeCallable(Callable<T> callable) {
		return Suppliers.memoize(new Supplier<T>() {
			@Override
			@SneakyThrows
			public T get() {
				return callable.call();
			}
		})::get;
	}
}
