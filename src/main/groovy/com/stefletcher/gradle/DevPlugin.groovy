package com.stefletcher.gradle

import groovy.io.FileType
import org.gradle.api.Plugin
import org.gradle.api.Project

class DevPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.task('commitMessage') {
            def gitFolder = new File(project.projectDir.absolutePath+'/.git')
//            def list = []
//            gitFolder.eachFileRecurse (FileType.FILES) { file ->
//                list << file
//            }
//            list.each {
//                println it.path
//            }
            
            doLast {
                if(!gitFolder.exists()) {
                    println('fook no git!')
                }else{
                    println('fook git exists!!')
                }
            }
        }
    }
}
