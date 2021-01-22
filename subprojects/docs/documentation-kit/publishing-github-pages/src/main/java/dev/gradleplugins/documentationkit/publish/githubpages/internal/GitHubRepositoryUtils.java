package dev.gradleplugins.documentationkit.publish.githubpages.internal;

import lombok.val;
import lombok.var;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.gradle.api.credentials.PasswordCredentials;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;

import static org.apache.commons.lang3.exception.ExceptionUtils.rethrow;

public final class GitHubRepositoryUtils {
	private GitHubRepositoryUtils() {}

	public static void createOrFetchOrClone(File repositoryDirectory, URI repositoryUri) throws IOException, GitAPIException {
		if (isGitRepository(repositoryDirectory)) {
			try (Git git = Git.open(repositoryDirectory)) {
				cleanRepository(git);
				if (hasGitHubPagesBranch(repositoryUri)) {
					git.fetch().call();
					git.checkout().setName("gh-pages").setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK).setStartPoint("origin/gh-pages").call();
					git.reset().setMode(ResetCommand.ResetType.HARD).setRef("origin/gh-pages").call();
				}
			}
		} else {
			try (Git git = Git.cloneRepository().setDirectory(repositoryDirectory).setURI(repositoryUri.toString()).setNoCheckout(true).setNoTags().call()) {
				if (hasGitHubPagesBranch(repositoryUri)) {
					git.checkout().setCreateBranch(true).setName("gh-pages").setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK).setStartPoint("origin/gh-pages").call();
				} else {
					git.checkout().setOrphan(true).setName("gh-pages").call();
					git.remoteAdd().setName("origin").setUri(new URIish(repositoryDirectory.toString())).call();
				}
			} catch (URISyntaxException e) {
				rethrow(e);
			}
		}
	}

	private static void cleanRepository(Git git) throws GitAPIException {
		git.reset().setMode(ResetCommand.ResetType.HARD).call();
		git.clean().setCleanDirectories(true).setForce(true).call();
	}

	public static void commitAndPushAllFiles(File repositoryDirectory, @Nullable PasswordCredentials credentials) throws IOException, GitAPIException {
		try (Git git = Git.open(repositoryDirectory)) {
			git.add().addFilepattern(".").call();
			git.commit().setAuthor("nokeedevbot", "bot@nokee.dev").setMessage("Publish by nokeedevbot").setAll(true).call();

			var pushCommand = git.push();
			if (credentials != null) {
				pushCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(credentials.getUsername(), credentials.getPassword()));
			}
			pushCommand.call();
		}
	}

	private static boolean hasGitHubPagesBranch(URI repositoryUri) throws GitAPIException {
		Collection<Ref> refs = Git.lsRemoteRepository().setHeads(true).setTags(false).setRemote(repositoryUri.toString()).call();
		return refs.stream().anyMatch(it -> it.getName().equals("refs/heads/gh-pages"));
	}

	private static boolean isGitRepository(File repositoryDirectory) {
		try (Repository repository = new FileRepository(new File(repositoryDirectory, ".git"))) {
			return repository.getObjectDatabase().exists();
		} catch (IOException e) {
			return false;
		}
	}

	public static URI inferGitHubHttpRepositoryUri(File repositoryDirectory) throws IOException, GitAPIException {
		try (val git = Git.open(repositoryDirectory)) {
			val remotes = git.remoteList().call();
			if (remotes.isEmpty()) {
				throw new RuntimeException(String.format("Unable to infer GitHub HTTP repository URI at '%s' because no remotes are available.", repositoryDirectory.getAbsolutePath()));
			}
			val uris = remotes.iterator().next().getURIs();
			if (uris.isEmpty()) {
				throw new RuntimeException(String.format("Unable to infer GitHub HTTP repository URI at '%s' because no remotes are available.", repositoryDirectory.getAbsolutePath()));
			}
			val uri = uris.iterator().next();
			try {
				return new URI(uri.toASCIIString());
			} catch (URISyntaxException e) {
				throw new RuntimeException(String.format("Unable to infer GitHub HTTP repository URI at '%s' because we couldn't parse the remote URI: '%s'.", repositoryDirectory.getAbsolutePath(), uri.toString()), e);
			}
		}
	}
}
