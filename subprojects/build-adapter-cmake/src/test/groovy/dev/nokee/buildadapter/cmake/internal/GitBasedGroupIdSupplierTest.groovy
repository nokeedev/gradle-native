package dev.nokee.buildadapter.cmake.internal

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.transport.URIish
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Subject

@Subject(GitBasedGroupIdSupplier)
class GitBasedGroupIdSupplierTest extends Specification {
	@Rule
	private final TemporaryFolder temporaryFolder = new TemporaryFolder()

	protected void makeSingleRepository(String remoteUri) {
		def git = Git.init().setDirectory(temporaryFolder.root).call()
		git.remoteAdd().setName("origin").setUri(new URIish(remoteUri)).call()
	}

	def "can compute group id for http-based git repository"() {
		given:
		makeSingleRepository("https://github.com/google/googletest")

		expect:
		new GitBasedGroupIdSupplier(temporaryFolder.root).get() == 'com.github.google.googletest'
	}
}
