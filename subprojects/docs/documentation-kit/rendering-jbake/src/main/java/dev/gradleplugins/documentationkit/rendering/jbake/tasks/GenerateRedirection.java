package dev.gradleplugins.documentationkit.rendering.jbake.tasks;

import lombok.Value;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.io.PrintWriter;

public abstract class GenerateRedirection extends DefaultTask {
	@Nested
	public abstract SetProperty<Redirection> getRedirections();

	@OutputDirectory
	public abstract DirectoryProperty getDestinationDirectory();

	@TaskAction
	private void doGenerate() throws IOException {
		FileUtils.deleteDirectory(getDestinationDirectory().get().getAsFile());

		val dir = getDestinationDirectory().get();
		for (Redirection redirection : getRedirections().get()) {
			val file = dir.file("content/" + redirection.getFrom() + "/index.adoc").getAsFile();
			file.getParentFile().mkdirs();
			try (PrintWriter out = new PrintWriter(file)) {
				val from = dir.file(redirection.getFrom()).getAsFile().toPath();
				val to = dir.file(redirection.getTo()).getAsFile().toPath();
				out.println(":jbake-type: redirection");
				out.println(":jbake-redirecturl: " + from.relativize(to).toString());
				out.println(":jbake-status: published");
			}
		}

		dir.dir("templates").getAsFile().mkdirs();
		try (val out = new PrintWriter(dir.file("templates/redirection.gsp").getAsFile())) {
			out.println("<!DOCTYPE html>");
			out.println("<html lang=\"en\">");
			out.println("  <head>");
			out.println("	<meta http-equiv=\"Refresh\" content=\"0; url=${content.redirecturl}\" />");
			out.println("	<% include 'fragment-header-generic.gsp' %>");
			out.println("  </head>");
			out.println("  <body>");
			out.println("	<p>Please follow <a href=\"${content.redirecturl}\">this link</a>.</p>");
			out.println("  </body>");
			out.println("</html>");
		}
	}

	public GenerateRedirection redirect(String from, String to) {
		getRedirections().add(new Redirection(from, to));
		return this;
	}

	@Value
	public static class Redirection {
		@Input
		String from;

		@Input
		String to;
	}
}
