package dev.nokee.docs.tasks;

import dev.nokee.docs.Dsl;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.*;
import org.gradle.api.tasks.*;

import javax.inject.Inject;
import java.io.IOException;
import java.io.UncheckedIOException;

@CacheableTask
public abstract class StageSample extends DefaultTask {
	@InputFiles
	@PathSensitive(PathSensitivity.RELATIVE)
	public abstract ConfigurableFileCollection getGroovyDslSources();

	@InputFiles
	@PathSensitive(PathSensitivity.RELATIVE)
	public abstract ConfigurableFileCollection getKotlinDslSources();

	@InputFiles
	@PathSensitive(PathSensitivity.RELATIVE)
	public abstract ConfigurableFileCollection getContentSources();

	@OutputDirectory
	public abstract DirectoryProperty getDestinationDirectory();

	@Inject
	public StageSample() {
		getDestinationDirectory().value(getLayout().getBuildDirectory().dir("tmp/" + getName())).disallowChanges();
	}

	@Inject
	protected abstract ProjectLayout getLayout();

	@Inject
	protected abstract FileSystemOperations getFileOperations();

	@TaskAction
	private void doStage() {
		getFileOperations().sync(spec -> {
			spec.into(getDestinationDirectory());
			spec.from(getGroovyDslSources(), childSpec -> childSpec.into(Dsl.GROOVY_DSL.getName()));
			spec.from(getKotlinDslSources(), childSpec -> childSpec.into(Dsl.KOTLIN_DSL.getName()));
			spec.from(getContentSources(), childSpec -> {
				childSpec.rename(it -> {
					if (it.contains("README.adoc")) {
						return it.replace("README.adoc", "index.adoc");
					}
					return it;
				});
			});
			spec.setDuplicatesStrategy(DuplicatesStrategy.EXCLUDE);
		});

		try {
			getDestinationDirectory().get().file("groovy-dsl/.jbakeignore").getAsFile().createNewFile();
			getDestinationDirectory().get().file("kotlin-dsl/.jbakeignore").getAsFile().createNewFile();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
