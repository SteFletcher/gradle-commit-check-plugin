# Client-side Git Commit Message Checker  
[![Build Status](https://travis-ci.org/SteFletcher/gradle-commit-check-plugin.svg?branch=master)](https://travis-ci.org/SteFletcher/gradle-commit-check-plugin)
[![Coverage Status](https://coveralls.io/repos/github/SteFletcher/gradle-commit-check-plugin/badge.svg?branch=master)](https://coveralls.io/github/SteFletcher/gradle-commit-check-plugin?branch=master)


## Description
Gradle plugin to validate commit message format client-side before a push. 

## Usage
Build script snippet for use in all Gradle versions:
```groovy
buildscript {
  repositories {
    maven {
      url "https://plugins.gradle.org/m2/"
    }
  }
  dependencies {
    classpath "gradle.plugin.com.stefletcher.gradle:gradle-commit-check-plugin:1.0.39"
  }
}

apply plugin: "com.stefletcher.gradle-commit-check-plugin"
```

Build script snippet for new, incubating, plugin mechanism introduced in Gradle 2.1:
```groovy
plugins {
  id "com.stefletcher.gradle-commit-check-plugin" version "1.0.39"
}
```
## How it works
It works by installing a commit hook into the following location:
```bash
<PROJECT-ROOT>/.git/hooks/commit-msg
```

Once installed the hook contains:

```bash
#!/bin/sh

commitMessage=`cat $1`
a=`echo "$commitMessage" | grep '<YOUR-EXPRESSION>'`
if [ $? -eq 0 ]; then
 exit 0
fi

echo "Commit message must conform to: <YOUR-EXPRESSION>
Error with: $commitMessage."
exit 1
```

The pattern <YOUR-EXPRESSION> is configured by adding the following to your build.gradle file:

```groovy
gitCommitFormat {
    expression = <YOUR-EXPRESSION> 
    //For example:  expression = /^[A-Za-z0-9]* -[A-Za-z0-9 ]*/
    template = '''Commit message must conform to: $expression\n Error with: $commitMessage.'''
}
```

