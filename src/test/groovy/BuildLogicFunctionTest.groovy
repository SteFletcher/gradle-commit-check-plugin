package com.stefletcher.gradle

import groovy.io.FileType
import org.ajoberstar.grgit.Grgit
import org.gradle.testkit.runner.GradleRunner
import static org.gradle.testkit.runner.TaskOutcome.*
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class BuildLogicFunctionalTest extends Specification {
    @Rule final TemporaryFolder testProjectDir = new TemporaryFolder()
    File buildFile
//    List<File> pluginClasspath

    def setup() {
        buildFile = testProjectDir.newFile('build.gradle')
//        println(testProjectDir.root)
        def grgit = Grgit.init(dir: testProjectDir.getRoot())
    }

    def "file written to .git directory when directory exists"() {
        given:
        buildFile << """
            plugins {
                id 'com.stefletcher.gradle.git-hooker'
            }
        """

        when:

            def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('commitMessage', '--stacktrace')
//                .withDebug(true)
                .withPluginClasspath()
                .build()

        then:
            println(result.output)
            result.task(":commitMessage").outcome == SUCCESS
            
    }
}