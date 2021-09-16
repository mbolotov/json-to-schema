package com.github.mbolotov.jsontoschema.services

import com.intellij.openapi.project.Project
import com.github.mbolotov.jsontoschema.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
