package dev.gradleplugins.documentationkit.publish.githubpages

import dev.gradleplugins.documentationkit.publish.githubpages.internal.GitHubPagesPublishPlugin
import dev.gradleplugins.integtests.fixtures.AbstractGradleSpecification
import spock.lang.Subject

@Subject(GitHubPagesPublishPlugin)
class GitHubPagesPublishPluginFunctionalTest extends AbstractGradleSpecification {
	def "has default import for task type"() {
		buildFile << '''
			plugins {
				id 'dev.gradleplugins.documentation.github-pages-publish'
			}

			tasks.named('publishToGitHubPages', PublishToGitHubPages)
		'''

		expect:
		succeeds('tasks')
	}
}
