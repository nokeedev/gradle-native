package dev.gradleplugins.documentationkit.site.base.internal;

import dev.gradleplugins.documentationkit.site.base.SiteExtension;
import dev.gradleplugins.documentationkit.site.base.Sitemap;
import dev.gradleplugins.documentationkit.site.base.tasks.GenerateRobots;
import dev.gradleplugins.documentationkit.site.base.tasks.GenerateSitemap;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.FileVisitDetails;
import org.gradle.api.file.FileVisitor;
import org.gradle.api.file.RelativePath;
import org.gradle.api.tasks.Sync;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;

import javax.inject.Inject;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

public class SiteBasePlugin implements Plugin<Project> {
	private final TaskContainer tasks;

	@Inject
	public SiteBasePlugin(TaskContainer tasks) {
		this.tasks = tasks;
	}

	@Override
	public void apply(Project project) {
		final TaskProvider<GenerateRobots> robotsTask = tasks.register("robots", GenerateRobots.class, task -> {
			task.setGroup("documentation");
			task.getHost().value(project.provider(() -> site(project).map(it -> it.getHost().getOrNull()).orElse(null))).disallowChanges();
			task.getGeneratedRobotsFile().value(project.getLayout().getBuildDirectory().file("tmp/" + task.getName() + "/robots.txt")).disallowChanges();
		});

		final TaskProvider<GenerateSitemap> sitemapTask = tasks.register("sitemap", GenerateSitemap.class, task -> {
			task.dependsOn((Callable<Object>) () -> {
				final Optional<SiteExtension> extension = site(project);
				if (extension.isPresent()) {
					return extension.get().getSources();
				}
				return Collections.emptyList();
			});
			task.setGroup("documentation");
			task.getGeneratedSitemapFile().value(project.getLayout().getBuildDirectory().file("tmp/" + task.getName() + "/sitemap.xml")).disallowChanges();
			task.getSitemapUrls().addAll(project.provider(() -> {
				final List<Sitemap.Url> urls = new ArrayList<Sitemap.Url>();
				site(project).ifPresent(site -> {
					site.getSources().getAsFileTree().visit(new FileVisitor() {
						@Override
						public void visitDir(FileVisitDetails details) {}

						@Override
						public void visitFile(FileVisitDetails details) {
							if (isHtml(details)) {
								try {
									// TODO: Use FileVisitDetails#getLastModified()
									urls.add(new Sitemap.Url(new URL("https://" + site.getHost().get() + "/" + canonizePath(details)), LocalDate.now()));
								} catch (MalformedURLException e) {
									throw new RuntimeException(e);
								}
							}
						}

						private boolean isHtml(FileVisitDetails details) {
							return details.getName().endsWith(".html");
						}

						private String canonizePath(FileVisitDetails details) {
							final RelativePath relativePath = details.getRelativePath();
							if (relativePath.getLastName().equals("index.html")) {
								return Optional.ofNullable(relativePath.getParent()).map(RelativePath::getPathString).orElse("");
							}
							return relativePath.getPathString();
						}
					});
				});
				return urls;
			}));
		});

		final TaskProvider<Sync> stageSiteTask = project.getTasks().register("stageSite", Sync.class, task -> {
			task.from(robotsTask.flatMap(GenerateRobots::getGeneratedRobotsFile));
			task.from(sitemapTask.flatMap(GenerateSitemap::getGeneratedSitemapFile));
			task.from((Callable<Object>) () -> site(project).map(SiteExtension::getSources).map(Collections::singletonList).orElseGet(Collections::emptyList));
			task.setDestinationDir(project.getLayout().getBuildDirectory().dir("site").get().getAsFile());
			task.setIncludeEmptyDirs(false);

			task.doFirst(t -> {
				site(project).ifPresent(site -> {
					site.getSymbolicLinks().get().keySet().forEach(src -> {
						try {
							Files.deleteIfExists(task.getDestinationDir().toPath().resolve(src));
						} catch (IOException e) {
							throw new UncheckedIOException(e);
						}
					});
				});
			});

			task.doLast(t -> {
				site(project).ifPresent(site -> {
					site.getSymbolicLinks().get().forEach((src, dst) -> {
						try {
							Files.createSymbolicLink(task.getDestinationDir().toPath().resolve(src), Paths.get(dst));
						} catch (IOException e) {
							throw new UncheckedIOException(e);
						}
					});
				});
			});
		});

		project.getTasks().register("site", task -> {
			task.dependsOn(stageSiteTask);
			task.setGroup("documentation");
			task.setDescription("Assemble your site");
		});
	}

	private static Optional<SiteExtension> site(Project project) {
		return Optional.ofNullable(project.getExtensions().findByName("site")).map(SiteExtension.class::cast);
	}
}
