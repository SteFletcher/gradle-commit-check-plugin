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

import java.util.logging.Logger

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class GradleCompatabilityTests extends Specification {
    @Rule
    final TemporaryFolder testProjectDir = new TemporaryFolder()

    Logger logger = Logger.getLogger("IntegTestLog")

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
    def "Plugin compatible with all recent gradle variants"() {
        when:
        logger.info "Testing with gradle version: $gradleVersion"
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('build', '--stacktrace', '-PjvmArgs="-XX:MaxPermSize=512m"')
                .withPluginClasspath()
                .withGradleDistribution(versionToURI(gradleVersion))
                .build()

        then:
            result.task(":build").outcome == SUCCESS
        where:
            gradleVersion << ['2.8','2.14','3.2.1']
    }

    URI versionToURI(String version) {
        "https://services.gradle.org/distributions/gradle-${version}-all.zip".toURI()
    }
}