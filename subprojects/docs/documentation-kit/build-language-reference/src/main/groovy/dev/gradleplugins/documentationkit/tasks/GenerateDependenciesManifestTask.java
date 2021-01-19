package dev.gradleplugins.documentationkit.tasks;

import com.google.gson.*;
import dev.nokee.utils.SpecUtils;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

public abstract class GenerateDependenciesManifestTask extends DefaultTask {
	@Internal
	public abstract SetProperty<Dependency> getDependencies();

	@OutputFile
	public abstract RegularFileProperty getManifestFile();

	@Inject
	public GenerateDependenciesManifestTask() {
		getOutputs().upToDateWhen(SpecUtils.satisfyNone());
	}

	@TaskAction
	private void doGenerate() throws IOException {
		val gson = new GsonBuilder().registerTypeHierarchyAdapter(Dependency.class, DependencySerializer.INSTANCE).create();
		FileUtils.write(getManifestFile().get().getAsFile(), gson.toJson(getDependencies().get()), StandardCharsets.UTF_8);
	}

	private enum DependencySerializer implements JsonSerializer<Dependency> {
		INSTANCE;

		public JsonElement serialize(Dependency obj, Type typeOfSrc, JsonSerializationContext context) {
			val result = new JsonObject();
			result.add("group", new JsonPrimitive(obj.getGroup()));
			result.add("name", new JsonPrimitive(obj.getName()));
			if (obj.getVersion() == null) {
				result.add("version", null);
			} else {
				result.add("version", new JsonPrimitive(obj.getVersion()));
			}
			return result;
		}
	}
}
