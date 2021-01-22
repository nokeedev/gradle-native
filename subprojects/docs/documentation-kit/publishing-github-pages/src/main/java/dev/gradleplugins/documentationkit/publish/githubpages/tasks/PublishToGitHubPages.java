package dev.gradleplugins.documentationkit.publish.githubpages.tasks;

import com.google.common.hash.Hashing;
import dev.gradleplugins.documentationkit.publish.githubpages.internal.GitHubRepositoryUtils;
import lombok.val;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.gradle.api.DefaultTask;
import org.gradle.api.credentials.PasswordCredentials;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.nio.charset.Charset;

public abstract class PublishToGitHubPages extends DefaultTask {
	@InputDirectory
	public abstract DirectoryProperty getPublishDirectory();

	@Internal
	public abstract Property<PasswordCredentials> getCredentials();

	@Input
	public abstract Property<URI> getUri();

	@Inject
	protected abstract FileSystemOperations getFileOperations();

	@TaskAction
	private void doPublish() throws GitAPIException, IOException {
		val repositoryDirectory = new File(getTemporaryDir(), BigInteger.valueOf(Hashing.md5().hashString(getUri().get().toString(), Charset.defaultCharset()).asLong()).toString(36));
		repositoryDirectory.mkdirs();
		GitHubRepositoryUtils.createOrFetchOrClone(repositoryDirectory, getUri().get());

		getFileOperations().sync(spec -> {
			spec.into(repositoryDirectory);
			spec.from(getPublishDirectory());
		});

		GitHubRepositoryUtils.commitAndPushAllFiles(repositoryDirectory, getCredentials().getOrNull());
	}
}
