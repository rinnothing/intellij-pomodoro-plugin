package com.github.rinnothing.intellijpomodoroplugin.services

import com.esotericsoftware.kryo.util.IntMap.Entries
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.github.rinnothing.intellijpomodoroplugin.MyBundle
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditorManager
import java.time.Instant
import com.intellij.openapi.editor.CaretModel
import com.intellij.openapi.editor.Editor
import com.github.rinnothing.intellijpomodoroplugin.listeners.CaretListener
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.PopupChooserBuilder
import java.awt.Toolkit
import java.awt.event.ActionEvent
import java.util.*
import javax.swing.JDialog
import javax.swing.JLabel
import kotlin.collections.HashMap
import javax.swing.Timer

@Service(Service.Level.PROJECT)
class TimeCountingService(project: Project) {
    private val project = project
    private var running: Boolean = false
    private var lastUpdated: Instant = Instant.now()
    private var timeSpent: Long = 0
    private var timeLeft: Long = 0

    private var chosenFile: String? = null
    private var filesTimeSpent: HashMap<String, FileTime> = HashMap()

    class FileTime {
        var totalTimeSpent: Long = 0
        var lastTimerTimeSpent: Long = 0
    }

    init {
        thisLogger().info(MyBundle.message("projectService", project.name))
        thisLogger().warn("Don't forget to remove all non-needed sample code files with their corresponding registration entries in `plugin.xml`.")

        ApplicationManager.getApplication().invokeLater(this::updateFiles)
    }

    private fun update() {
        if (running) {
            timeSpent += Instant.now().epochSecond - lastUpdated.epochSecond
            timeLeft -= Instant.now().epochSecond - lastUpdated.epochSecond

            if (chosenFile != null) {
                val tmp = filesTimeSpent[chosenFile]
                if (tmp != null) {
                    tmp.totalTimeSpent += Instant.now().epochSecond - lastUpdated.epochSecond
                    tmp.lastTimerTimeSpent += Instant.now().epochSecond - lastUpdated.epochSecond
                }
            }

            if (timeLeft <= 0) {
                running = false
                displayPopup()
            }
        }
        lastUpdated = Instant.now()
    }

    fun startCommonTimer(timeSeconds: Long) {
        running = true
        timeLeft = timeSeconds
        timeSpent = 0
        lastUpdated = Instant.now()
        resetLastTimer()
        update()
    }

    fun startCommonTimer(time: String) {
        startCommonTimer(parseTimeFormatted(time))
    }

    fun pauseResumeTimer() {
        if (running) {
            pauseCommonTimer()
        } else {
            resumeCommonTimer()
        }
    }

    fun pauseCommonTimer() {
        running = false
        update()
    }

    fun resumeCommonTimer() {
        running = true
        update()
    }

    fun getTimeLeft(): Long {
        update()
        return timeLeft
    }

    fun formatTime(time: Long): String {
        if (time < 10) {
            return "0:0$time"
        } else if (time < 60) {
            return "0:$time"
        }
        else {
            return (time / 60).toString() + ":" + (time % 60)
        }
    }

    fun getTimeLeftFormatted(): String {
        return formatTime(getTimeLeft())
    }

    fun parseTimeFormatted(s: String): Long {
        var i = 0
        var seconds: Long = 0
        var minutes: Long = 0
        if (s.contains(":")) {
            while (i < s.length && s[i] != ':') {
                minutes *= 10
                minutes += s[i] - '0'
                ++i
            }
            ++i
        }

        while (i < s.length) {
            seconds *= 10
            seconds += s[i] - '0'
            ++i
        }
        seconds += minutes * 60

        return seconds
    }

    private fun resetLastTimer() {
        for (fileTime in filesTimeSpent) {
            fileTime.value.lastTimerTimeSpent = 0
        }
    }

    fun changeCurrentFile(fileName: String) {
        update()
        chosenFile = fileName
    }

    fun isRunning(): Boolean {
        return running
    }

    fun updateFiles() {
        val files = FileEditorManager.getInstance(project).selectedFiles
        for (file in files) {
            changeCurrentFile(file.name)
        }

        ApplicationManager.getApplication().invokeLater(this::updateFiles)
    }

    fun getFileTimes(): MutableSet<MutableMap.MutableEntry<String, TimeCountingService.FileTime>> {
        return filesTimeSpent.entries
    }

    fun displayPopup() {
//        val content = JLabel(MyBundle.message("pomodoroPopup"))
//        val popup = JBPopupFactory.getInstance().createComponentPopupBuilder(content, null)
//            .setResizable(true)
//            .setMovable(true)
//            .setCancelOnClickOutside(true)
//            .createPopup()
//
//        val editor = FileEditorManager.getInstance(project).selectedTextEditor
//        if (editor == null) {
//            return
//        }
//        popup.showInBestPositionFor(editor)

        val dialog = JDialog()
        dialog.isUndecorated = true
        dialog.contentPane.add(JLabel(MyBundle.message("pomodoroPopup")))
        dialog.pack()

        val screenSize = Toolkit.getDefaultToolkit().screenSize
        val dialogSize = dialog.size
        dialog.setLocation(
            (screenSize.width - dialogSize.width) / 2,
            (screenSize.height - dialogSize.height) / 2
        )

        dialog.isVisible = true

        val timer = Timer(2000) { e: ActionEvent? -> dialog.dispose() }
        timer.isRepeats = false
        timer.start()
    }
}
