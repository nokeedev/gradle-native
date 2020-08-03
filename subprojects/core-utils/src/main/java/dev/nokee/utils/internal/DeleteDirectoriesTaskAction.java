package dev.nokee.utils.internal;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.file.Directory;

import java.io.File;
import java.nio.file.Path;

import static dev.nokee.utils.DeferredUtils.flatUnpackUntil;
import static dev.nokee.utils.DeferredUtils.unpack;


@RequiredArgsConstructor
public class DeleteDirectoriesTaskAction implements Action<Task> {
	private final Iterable<Object> directories;

	@Override
	public void execute(Task task) {
		flatUnpackUntil(directories, this::unpackToFile, File.class).forEach(this::deleteDirectory);
	}

	@SneakyThrows
	private void deleteDirectory(File directory) {
		FileUtils.deleteDirectory(directory);
	}

	private Object unpackToFile(Object obj) {
		obj = unpack(obj);
		if (obj instanceof Directory) {
			return ((Directory) obj).getAsFile();
		} else if (obj instanceof Path) {
			return ((Path)obj).toFile();
		}
		return obj;
	}
}
