package dev.gradleplugins.documentationkit.site.githubpages

import dev.gradleplugins.documentationkit.site.githubpages.internal.GitHubPagesSitePlugin
import dev.gradleplugins.integtests.fixtures.AbstractGradleSpecification
import spock.lang.Subject

@Subject(GitHubPagesSitePlugin)
class GitHubPagesSitePluginFunctionalTest extends AbstractGradleSpecification {
	def "generate CNAME in site"() {
		buildFile << '''
			plugins {
				id 'dev.gradleplugins.documentation.github-pages-site'
			}

			site {
				customDomain = subdomain('foo.example.com')
			}
		'''

		expect:
		succeeds('site')
		file('build/site/CNAME').text == 'foo.example.com'
	}

	def "generate .nojekyll in site"() {
		buildFile << '''
			plugins {
				id 'dev.gradleplugins.documentation.github-pages-site'
			}
		'''

		expect:
		succeeds('site')
		file('build/site/.nojekyll').exists()
	}
}
