package com.stefletcher.gradle


import org.ajoberstar.grgit.Grgit
import org.ajoberstar.grgit.exception.GrgitException
import org.gradle.testkit.runner.GradleRunner
import static org.gradle.testkit.runner.TaskOutcome.*
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import org.springframework.boot.test.OutputCapture

class BuildLogicFunctionalTest extends Specification {
    @Rule final TemporaryFolder testProjectDir = new TemporaryFolder()
    File buildFile
    Grgit grgit

    @org.junit.Rule
    OutputCapture capture = new OutputCapture()

    def setup() {
        buildFile = testProjectDir.newFile('build.gradle')
    }

    def "file written to .git directory when directory exists"() {
        given:
        def grgit = Grgit.init(dir: testProjectDir.getRoot())
        buildFile << """
            plugins {
                id 'com.stefletcher.gradle.git-hook-plugin'
            }
        """
        when:
            def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('commitMessage', '--stacktrace')
                .withPluginClasspath()
                .build()

        then:
            result.task(":commitMessage").outcome == SUCCESS
            def hookFile = new File(testProjectDir.getRoot().absolutePath+'/.git/hooks/commit-msg')
            hookFile.exists()
            
    }

    def "no attempt to write hook file if .git folder doesn't exist"() {
        given:
        buildFile << """
            plugins {
                id 'com.stefletcher.gradle.git-hook-plugin'
            }
        """
        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('commitMessage', '--stacktrace')
                .withPluginClasspath()
                .build()

        then:
            result.task(":commitMessage").outcome == SUCCESS
            def hookFile = new File(testProjectDir.getRoot().absolutePath+'/.git/hooks/commit-msg')
            !hookFile.exists()

    }

    def "incorrect commit message format fails and informs with correct format"() {
        given:
        grgit = Grgit.init(dir: testProjectDir.getRoot())
        buildFile << """
                plugins {
                    id 'com.stefletcher.gradle.git-hook-plugin'
                }
            """
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('commitMessage', '--stacktrace')
                .withPluginClasspath()
                .build()
        // add all to git index
        grgit.add(patterns: ['*'], update: true)

        when:
            def toCommit = new File(testProjectDir.getRoot().getAbsolutePath() + '/test.txt')
            toCommit << "some text"
            grgit.commit(message: 'some message')

        then:
            capture.toString().contains("go away")
            GrgitException grgitException = thrown()
            grgitException.getCause().class == org.eclipse.jgit.api.errors.AbortedByHookException

    }
}