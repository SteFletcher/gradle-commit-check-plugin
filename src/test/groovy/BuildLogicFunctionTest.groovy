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

    @org.junit.Rule
    OutputCapture capture = new OutputCapture()

    File buildFile
    Grgit grgit

    def setup() {

        buildFile = testProjectDir.newFile('build.gradle')
        buildFile << '''
            plugins {
                id 'com.stefletcher.gradle-commit-check-plugin'
                id 'java'
            }

            gitCommitFormat {
                expression = /^[A-Za-z0-9]* -[A-Za-z0-9 ]*/
            }
        '''
    }

    def "file written to .git directory when directory exists"() {
        given:
        def grgit = Grgit.init(dir: testProjectDir.getRoot())
        when:
            def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('build', '--stacktrace')
                .withPluginClasspath()
                    .withDebug(true)
                .build()

        then:
            result.task(":commitMessage").outcome == SUCCESS
            def hookFile = new File(testProjectDir.getRoot().absolutePath+'/.git/hooks/commit-msg')
            hookFile.exists()
            
    }

    def "no attempt to write hook file if .git folder doesn't exist"() {
        given:
            // plugin is applied to a non-git project
        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('build', '--stacktrace')
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

        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('build', '--stacktrace')
                .withPluginClasspath()
                .build()
        // add all to git index
        grgit.add(patterns: ['*'], update: true)

        when:
            def toCommit = new File(testProjectDir.getRoot().getAbsolutePath() + '/test.txt')
            toCommit << "some text"
            def commit = grgit.commit(message: 'some message')

        then:
        result.task(':commitMessage').outcome == SUCCESS
            capture.toString().contains("Incorrect commit message format: some message")
            GrgitException grgitException = thrown()
            grgitException.getCause().class == org.eclipse.jgit.api.errors.AbortedByHookException

    }

    def "should succeed when commit message format meets expectation"() {
        given:
        grgit = Grgit.init(dir: testProjectDir.getRoot())

        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('build', '--stacktrace')
                .withPluginClasspath()
                .build()
        // add all to git index
        grgit.add(patterns: ['*'], update: true)

        when:
        def toCommit = new File(testProjectDir.getRoot().getAbsolutePath() + '/test.txt')
        toCommit << "some text"
        grgit.commit(message: 'SOMEID - MESSAGE')

        then:
        result.task(':commitMessage').outcome == SUCCESS
        !capture.toString().contains("Incorrect commit message format: \"SOMEID - MESSAGE\"")

    }


    def "should not fail when commit message format not defined"() {
        when:
//        def buildFile = new File(testProjectDir.absolutePath+'/build.gradle')
        buildFile.delete()
        buildFile = testProjectDir.newFile('build.gradle')
        buildFile << '''
            plugins {
                id 'com.stefletcher.gradle-commit-check-plugin'
                id 'java'
            }

        '''

        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('build', '--stacktrace')
                .withPluginClasspath()
                .build()

        then:
        result.task(':commitMessage').outcome == SUCCESS


    }
}