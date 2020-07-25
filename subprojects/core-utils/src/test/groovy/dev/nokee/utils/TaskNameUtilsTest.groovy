package dev.nokee.utils

import spock.lang.Specification
import spock.lang.Subject

@Subject(TaskNameUtils)
class TaskNameUtilsTest extends Specification {
	def "can shorten task name without path"() {
		expect:
		TaskNameUtils.getShortestName('fooBarFar') == 'fBF'
		TaskNameUtils.getShortestName('foo_BarFar42') == 'f_BF42'
	}
}
