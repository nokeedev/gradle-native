package dev.nokee.platform.base.internal

import spock.lang.Specification

class GroupIdTest extends Specification {
	def a = GroupId.of { 'com.example.a' }
	def equalToA = GroupId.of { 'com.example.a' }
	def b = GroupId.of { 'com.example.b' }

	def "can compare instances"() {
		expect:
		a == equalToA
		a != b
		a == a
	}
}
