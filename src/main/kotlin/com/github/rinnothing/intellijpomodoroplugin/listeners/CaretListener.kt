package com.github.rinnothing.intellijpomodoroplugin.listeners

import com.github.rinnothing.intellijpomodoroplugin.services.TimeCountingService
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.vfs.VirtualFile



class CaretListener: CaretListener {

    override fun caretPositionChanged(event: CaretEvent) {
        val project = event.editor.project ?: return

        val service: TimeCountingService = project.service<TimeCountingService>()

        val files = FileEditorManager.getInstance(project).selectedFiles
        for (file in files) {
            service.changeCurrentFile(file.name)
        }
    }
}