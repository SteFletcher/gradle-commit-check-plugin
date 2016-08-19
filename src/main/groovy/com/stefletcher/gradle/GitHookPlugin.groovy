package com.stefletcher.gradle

import groovy.io.FileType
import org.gradle.api.Plugin
import org.gradle.api.Project

import java.util.regex.Pattern


class GitHookPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.extensions.create("gitCommitFormat", MessageRegExp)

        project.task('commitMessage') {
            def gitFolder = new File(project.projectDir.absolutePath+'/.git')

            doLast {
                def String x = project.gitCommitFormat.expression
                if(x.charAt(x.length()-1)=="\$"){
                    x=x.substring(0, x.length()-1)
                    x=x+"\\\$"
                }
                if(!gitFolder.exists() || project.gitCommitFormat.expression == '') {
//                    Do nothing...
                }else{
                    def hooks = new File(gitFolder.absolutePath+'/hooks')

                    def source = this.getClass().getResource('/commit-msg')
                    def destination = new File(hooks.absolutePath+'/commit-msg')
                    println(x)
                    destination << source.text.replaceAll("EXPR_HERE", x)
                    destination.setExecutable(true)
                }
            }
        }
    }
}

class MessageRegExp {
    def expression = null;
}
