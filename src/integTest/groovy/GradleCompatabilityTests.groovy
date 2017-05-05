/*
Client-side Git Commit Message Checker

Copyright (c) 2017 Stephen Fletcher

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
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