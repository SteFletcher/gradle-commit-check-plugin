/**
 * Created by stefletcher on 02/12/2016.
 */
package com.stefletcher.gradle

import org.ajoberstar.grgit.Grgit
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.springframework.boot.test.OutputCapture
import spock.lang.Specification
import spock.lang.Unroll

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class GradleCompatabilityTests extends Specification {
    @Rule
    final TemporaryFolder testProjectDir = new TemporaryFolder()

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
    @Unroll
    def "Plugin compatible with all 2.x gradle variants"() {
        given:
        def grgit = Grgit.init(dir: testProjectDir.getRoot())
        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('build', '--stacktrace')
                .withPluginClasspath()
                .withGradleDistribution(versionToURI(gradleVersion))
                .withDebug(true)
                .build()

        then:
        result.task(":commitMessage").outcome == SUCCESS
        def hookFile = new File(testProjectDir.getRoot().absolutePath + '/.git/hooks/commit-msg')
        hookFile.exists()
        where:
            gradleVersion << ['2.8', '2.9', '2.10','2.11','2.12', '2.13','2.14', '3.0']
    }

    URI versionToURI(String version) {
        "https://services.gradle.org/distributions/gradle-${version}-all.zip".toURI()
    }
}