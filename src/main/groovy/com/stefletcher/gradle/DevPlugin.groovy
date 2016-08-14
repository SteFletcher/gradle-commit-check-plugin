package com.stefletcher.gradle

import groovy.io.FileType
import org.gradle.api.Plugin
import org.gradle.api.Project

class DevPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.task('commitMessage') {
            def gitFolder = new File(project.projectDir.absolutePath+'/.git')

            doLast {
                if(!gitFolder.exists()) {
//                    Do nothing...
                }else{
//                    println('fook git exists!!')
                    def hooks = new File(gitFolder.absolutePath+'/hooks')
                    hooks.mkdir()
                    def source = this.getClass().getResource('/commit-msg')
                    def destination = new File(hooks.absolutePath+'/commit-msg')
                    destination << source.text
                    destination.setExecutable(true)

                    def list = []
                    gitFolder.eachFileRecurse (FileType.FILES) { file ->
                        list << file
                    }
                    list.each {
                        println it.path
                    }
                }
            }
        }
    }
}
