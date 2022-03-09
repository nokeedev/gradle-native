package dev.nokee.docs.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;
import org.gradle.workers.WorkerExecutor;

import javax.inject.Inject;
import java.io.*;

@CacheableTask
public abstract class CreateEmbeddedPlayer extends DefaultTask {
	@Input
	public abstract Property<String> getMp4FileName();

	@OutputFile
	public abstract RegularFileProperty getHtmlPlayerFile();

	@Inject
	public CreateEmbeddedPlayer() {
		getHtmlPlayerFile().value(getLayout().getBuildDirectory().file(getMp4FileName().map(it -> "tmp/" + getName() + "/" + it.replace(".mp4", ".embed.html")))).disallowChanges();
	}

	@Inject
	protected abstract ProjectLayout getLayout();

	@Inject
	protected abstract WorkerExecutor getWorkerExecutor();

	@TaskAction
	private void doCreate() {
		getWorkerExecutor().classLoaderIsolation().submit(CompileAction.class, it -> {
			it.getMp4FileName().set(getMp4FileName());
			it.getOutputFile().set(getHtmlPlayerFile());
		});
	}

	public interface CompileParameters extends WorkParameters {
		Property<String> getMp4FileName();
		RegularFileProperty getOutputFile();
	}

	public static abstract class CompileAction implements WorkAction<CompileParameters> {
		@Override
		public void execute() {
			File outputFile = getParameters().getOutputFile().get().getAsFile();
			outputFile.getParentFile().mkdirs();

			try (PrintWriter out = new PrintWriter(new FileOutputStream(outputFile))) {
				out.println("<!DOCTYPE html>");
				out.println("<html>");
				out.println("<head>");
				out.println("	<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
				out.println("	<style type=\"text/css\">");
				out.println("		body {");
				out.println("			background-color: black;");
				out.println("		}");
				out.println("		video {");
				out.println("			width:100%;");
				out.println("			max-width:600px;");
				out.println("			height:auto;");
				out.println("		}");
				out.println("	</style>");
				out.println("</head>");
				out.println("<body>");
				out.println("<video controls>");
				out.println("	<source src=\"" + getParameters().getMp4FileName().get() + "\" type=\"video/mp4\">");
				out.println("Your browser does not support video");
				out.println("</video>");
				out.println("</body>");
				out.println("</html>");
			} catch (FileNotFoundException e) {
				throw new UncheckedIOException(e);
			}
		}
	}
}
