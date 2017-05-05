package com.stefletcher.gradle


import org.ajoberstar.grgit.Grgit
import org.ajoberstar.grgit.exception.GrgitException
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Shared

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

    def "should output message formatted by specified template"() {
        given:
        grgit = Grgit.init(dir: testProjectDir.getRoot())
        buildFile.delete()
        buildFile = testProjectDir.newFile('build.gradle')
        buildFile << '''
            plugins {
                id 'com.stefletcher.gradle-commit-check-plugin'
                id 'java'
            }

            gitCommitFormat {
                expression = /^[A-Za-z0-9]* -[A-Za-z0-9 ]*/
                template = 'Commit message must conform to: <% print message %>'
            }

        '''

        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('build', '--stacktrace')
                .withDebug(true)
                .withPluginClasspath()
                .build()
        // add all to git index
        grgit.add(patterns: ['*'], update: true)

        when:
        def toCommit = new File(testProjectDir.getRoot().getAbsolutePath() + '/test.txt')
        toCommit << "some text"
        grgit.commit(message: 'BAD COMMIT MESSAGE')

        then:
        thrown GrgitException
        capture.toString().contains('''Commit message must conform to: ^[A-Za-z0-9]* -[A-Za-z0-9 ]*''')

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
            capture.toString().contains("Incorrect commit message format: ^[A-Za-z0-9]* -[A-Za-z0-9 ]*")
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


    def "should build message using project object" () {
        given:
        GitHookPlugin gitHookPlugin = new GitHookPlugin();

        def pluginExtension = new MessageRegExp()
        pluginExtension.expression = "great!"
        pluginExtension.template = "This is a template with a message: <% print message %>"


        when:
        def result = gitHookPlugin.buildMessage(pluginExtension)
        then:
        result.contains "This is a template with a message: great!"

    }
    def "should build message from template string and binding parameters" () {
        given:
        GitHookPlugin gitHookPlugin = new GitHookPlugin();
        def template = "This is a template with a message: <% print message %>"
        def binding = ["message":"great!"]
        when:
        def result = gitHookPlugin.formatMessage(binding, template)
        then:
        result.contains "This is a template with a message: great!"

    }
}