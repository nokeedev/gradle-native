package dev.gradleplugins.documentationkit.publish.githubpages.internal;

import dev.gradleplugins.fixtures.vcs.GitFileRepository;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.errors.NoRemoteRepositoryException;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import spock.lang.Subject;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Optional;

import static dev.gradleplugins.documentationkit.publish.githubpages.internal.GitHubRepositoryUtils.commitAndPushAllFiles;
import static dev.gradleplugins.documentationkit.publish.githubpages.internal.GitHubRepositoryUtils.createOrFetchOrClone;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.createDirectories;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.io.FileMatchers.aFileNamed;
import static org.hamcrest.io.FileMatchers.anExistingFile;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Subject(GitHubRepositoryUtils.class)
class GitHubRepositoryUtilsTest {
	GitFileRepository repo;
	@TempDir
	Path testDirectory;

	@BeforeEach
	void setUpGitRepository() throws GitAPIException, IOException {
		repo = GitFileRepository.init(testDirectory.resolve("repo").toFile());
		initialCommit(repo);
	}

	private static void initialCommit(GitFileRepository repo) throws IOException, GitAPIException {
		repo.file("foo.txt").createNewFile();
		repo.file("bar.txt").createNewFile();
		repo.commit("Initial commit", "foo.txt", "bar.txt");
	}

	private static void initialGitHubPagesBranch(GitFileRepository repo) throws IOException, GitAPIException {
		repo.createBranch("gh-pages");
		repo.checkout("gh-pages");
		repo.file(".nojekyll").createNewFile();
		repo.commit("No jekyll");
	}

	private static void pollute(GitFileRepository repo) throws IOException {
		repo.file("foo.html").createNewFile();
		repo.file("foo.txt").delete();
		FileUtils.write(repo.file("bar.txt"), "new content", UTF_8);
	}

	@AfterEach
	void tearDownGitRepository() {
		repo.close();
	}

	@Nested
	class MissingGitHubPagesBranch {
		private GitFileRepository localRepo;

		@BeforeEach
		void initializeRepo() throws GitAPIException, IOException {
			File localRepoDirectory = testDirectory.resolve("temp").toFile();
			createOrFetchOrClone(localRepoDirectory, repo.getUrl());
			localRepo = GitFileRepository.open(localRepoDirectory);
		}

		@AfterEach
		void closeLocalRepo() {
			localRepo.close();
		}

		@Test
		void createsNewEmptyGitHubPagesBranchWhenMissing() throws GitAPIException, URISyntaxException, IOException {
			assertThat("repository remotes points to source repository", localRepo.getRemotes().get(0), equalTo(repo.getUrl()));
			assertThat("repository head points at gh-pages branch", localRepo, hasHeadRef("refs/heads/gh-pages"));
			assertThat("no files are checked out", localRepo.getWorkTree().listFiles(withoutHiddenFiles()), emptyArray());
		}

		@Test
		void canPushAllFilesWithoutPriorGitHubPagesBranches() throws IOException, GitAPIException {
			repo.file("foo.html").createNewFile();
			commitAndPushAllFiles(localRepo.getWorkTree(), Optional.empty());

			repo.checkout("gh-pages");
			assertThat(repo.getWorkTree().listFiles(withoutHiddenFiles()),
				hasItemInArray(aFileNamed(equalTo("foo.html"))));
		}
	}

	@Nested
	class ExistingRemoteGitHubPagesBranch {
		private GitFileRepository localRepo;

		@BeforeEach
		void initializeRepo() throws GitAPIException, IOException {
			initialGitHubPagesBranch(repo);

			File localRepoDirectory = testDirectory.resolve("temp").toFile();
			createOrFetchOrClone(localRepoDirectory, repo.getUrl());
			localRepo = GitFileRepository.open(localRepoDirectory);
		}

		@AfterEach
		void closeLocalRepo() {
			localRepo.close();
		}

		@Test
		void checksOutGitHubPagesBranchFromClone() {
			assertThat("repository head points at gh-pages branch", localRepo, hasHeadRef("refs/heads/gh-pages"));
			assertThat("files are checked out", localRepo.file(".nojekyll"), anExistingFile());
		}

		@Test
		void canPushAllFilesOnCheckedOutGitHubPagesBranchFromClone() throws IOException, GitAPIException {
			pollute(localRepo);
			commitAndPushAllFiles(localRepo.getWorkTree(), Optional.empty());

			repo.getGit().reset().setMode(ResetCommand.ResetType.HARD).call();
			assertThat(repo.getWorkTree().listFiles(withoutHiddenFiles()),
				arrayContainingInAnyOrder(aFileNamed(equalTo("foo.html")), aFileNamed(equalTo("bar.txt"))));
			assertThat(FileUtils.readFileToString(repo.file("bar.txt"), UTF_8), equalTo("new content"));
		}

		@Nested
		class RemoteGitHubPagesBranchHasChanged {
			@BeforeEach
			void addNewFilesOnRemoteBranch() throws GitAPIException, IOException {
				repo.checkout("gh-pages");
				repo.file("bar.txt").createNewFile();
				repo.commit("New files");

				createOrFetchOrClone(localRepo.getWorkTree(), repo.getUrl());
			}


			@Test
			void resetsGitHubPagesBranchToLatestWhenAlreadyExists() {
				assertThat("repository head points at gh-pages branch", localRepo, hasHeadRef("refs/heads/gh-pages"));
				assertThat("old files are checked out", localRepo.file(".nojekyll"), anExistingFile());
				assertThat("new files are checked out", localRepo.file("bar.txt"), anExistingFile());
			}
		}

		@Nested
		class DirtyLocalGitHubPagesBranch {
			@BeforeEach
			void makeLocalRepoDirty() throws IOException, GitAPIException {
				pollute(localRepo);
				createOrFetchOrClone(localRepo.getWorkTree(), repo.getUrl());
			}

			@Test
			void hardResetCloneWhenDirty() throws IOException {
				assertThat("local file is removed", localRepo.file("foo.html"), not(anExistingFile()));
				assertThat("file is restored", localRepo.file("foo.txt"), anExistingFile());
				assertThat("local file content is reset", FileUtils.readFileToString(localRepo.file("bar.txt"), UTF_8), emptyString());
				assertThat("repository head points at gh-pages branch", localRepo, hasHeadRef("refs/heads/gh-pages"));
			}
		}
	}

	@Nested
	class DirtyLocalWithMissingRemoteGitHubPagesBranch {
		private GitFileRepository localRepo;

		@BeforeEach
		void initializeRepo() throws GitAPIException, IOException {
			File localRepoDirectory = testDirectory.resolve("temp").toFile();
			createOrFetchOrClone(localRepoDirectory, repo.getUrl());
			localRepo = GitFileRepository.open(localRepoDirectory);

			pollute(localRepo);
			createOrFetchOrClone(localRepoDirectory, repo.getUrl());
		}

		@AfterEach
		void closeLocalRepo() {
			localRepo.close();
		}

		@Test
		void hardResetNewBranchWhenDirty() throws GitAPIException, URISyntaxException, IOException {
			assertThat("new gh-pages branch should be empty", localRepo.file("foo.html"), not(anExistingFile()));
			assertThat("new gh-pages branch should be empty", localRepo.file("foo.txt"), not(anExistingFile()));
			assertThat("new gh-pages branch should be empty", localRepo.file("bar.txt"), not(anExistingFile()));
			assertThat("repository head points at gh-pages branch", localRepo, hasHeadRef("refs/heads/gh-pages"));
		}
	}

	@Test
	void throwsExceptionWhenRepositoryIsMissing() throws IOException {
		val localRepo = createDirectories(testDirectory.resolve("temp")).toFile();
		val remoteRepo = testDirectory.resolve("missing").toFile().toURI();

		val ex = assertThrows(InvalidRemoteException.class, () -> GitHubRepositoryUtils.createOrFetchOrClone(localRepo, remoteRepo));
		assertThat(ex.getMessage(), equalTo("Invalid remote: origin"));
		assertThat(ex.getCause(), isA(NoRemoteRepositoryException.class));
		assertThat(ex.getCause().getMessage().replace("file:///", "file:/"), equalTo(remoteRepo + ": not found."));
	}

	private static FilenameFilter withoutHiddenFiles() {
		return (dir, name) -> !name.startsWith(".");
	}

	private static Matcher<GitFileRepository> hasHeadRef(String ref) {
		return new FeatureMatcher<GitFileRepository, String>(equalTo(ref), "", "") {
			@SneakyThrows
			@Override
			protected String featureValueOf(GitFileRepository actual) {
				return actual.getHead().getTarget().getName();
			}
		};
	}
}
