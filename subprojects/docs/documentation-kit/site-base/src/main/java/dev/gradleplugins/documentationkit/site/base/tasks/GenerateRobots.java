package dev.gradleplugins.documentationkit.site.base.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

public abstract class GenerateRobots extends DefaultTask {
	@Input
	public abstract Property<String> getHost();

	@OutputFile
	public abstract RegularFileProperty getGeneratedRobotsFile();

	@TaskAction
	private void doGenerate() throws FileNotFoundException {
		try (PrintWriter out = new PrintWriter(getGeneratedRobotsFile().get().getAsFile())) {
			out.println("User-agent: *");
			out.println("sitemap: https://" + getHost().get() + "/sitemap.xml");
		}
	}
}
