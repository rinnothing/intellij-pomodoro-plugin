package com.github.rinnothing.intellijpomodoroplugin.toolWindow

import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.content.ContentFactory
import com.github.rinnothing.intellijpomodoroplugin.MyBundle
import com.github.rinnothing.intellijpomodoroplugin.services.TimeCountingService
import com.intellij.ui.components.JBTextField
import javax.swing.JButton
import com.intellij.openapi.application.ApplicationManager
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.Content
import com.intellij.ui.table.JBTable
import javax.swing.JTable
import javax.swing.ListSelectionModel
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.table.DefaultTableModel


class MyToolWindowFactory : ToolWindowFactory {

    init {
        thisLogger().warn("Don't forget to remove all non-needed sample code files with their corresponding registration entries in `plugin.xml`.")
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val service: TimeCountingService = project.service<TimeCountingService>()
        val myToolWindow = MyToolWindow(toolWindow)
        val content = ContentFactory.getInstance().createContent(myToolWindow.getContent(), null, false)
        toolWindow.contentManager.addContent(content)
        ApplicationManager.getApplication().invokeLater(myToolWindow::update)

        val table = createTable(service)
        val contentAnother = ContentFactory.getInstance().createContent(table, null, false)
        toolWindow.contentManager.addContent(contentAnother)
    }

    private fun createTable(service: TimeCountingService): JTable {
        val content = service.getFileTimes()

        val columnNames = arrayOf(MyBundle.message("pomodoroName"), MyBundle.message("pomodoroTimeSpent"))

//        val rowData:Array<Array<String>> = Array<Array<String>>()
//        for (file in content) {
//            rowData.add(arrayOf(rowData[rowData.size - 1].add(file.key), rowData[rowData.size - 1].add(service.formatTime(file.value.totalTimeSpent))))
//        }
        val rowData = arrayOf(
            arrayOf("John", "25", "New York"),
            arrayOf("Alice", "30", "London"),
            arrayOf("Bob", "40", "Paris")
        )

        val tableModel = DefaultTableModel(rowData, columnNames)
        val table = JBTable(tableModel)
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)

        return table
    }

    override fun shouldBeAvailable(project: Project) = true

    class MyToolWindow(toolWindow: ToolWindow) {

        private val service: TimeCountingService = toolWindow.project.service<TimeCountingService>()
        private var pomodoroPauseResume: JButton = JButton(MyBundle.message("pomodoroResume"))
        private var pomodoroTimeField: JBTextField = JBTextField(MyBundle.message("pomodoroTimeDefault"))
        private var timeString: String = MyBundle.message("pomodoroTimeDefault")

        fun getContent() = JBPanel<JBPanel<*>>().apply {
            add(JBLabel(MyBundle.message("pomodoroLabel")))
            add(JBLabel(MyBundle.message("pomodoroTime")))

            pomodoroTimeField.document.addDocumentListener(object:
                DocumentListener{
                override fun changedUpdate(e: DocumentEvent?) {
                    timeString = pomodoroTimeField.text
                }
                override fun insertUpdate(e: DocumentEvent?) {
                    timeString = pomodoroTimeField.text
                }
                override fun removeUpdate(e: DocumentEvent?) {
                    timeString = pomodoroTimeField.text
                }
            })
            add(pomodoroTimeField)

            add(JButton(MyBundle.message("pomodoroStart")).apply {
                addActionListener {
                    service.startCommonTimer(timeString)
                }
            })
            add(pomodoroPauseResume.apply {
                addActionListener {
                    service.pauseResumeTimer()
                }})
            add(JButton(MyBundle.message("pomodoroStats")).apply {
                addActionListener {
                    //todo
                }
            })
        }


        fun update() {
            if (service.isRunning()) {
                pomodoroTimeField.text = service.getTimeLeftFormatted()
            }
            if (service.isRunning()) {
                pomodoroPauseResume.text = MyBundle.message("pomodoroPause")
            } else {
                pomodoroPauseResume.text = MyBundle.message("pomodoroResume")
            }

            ApplicationManager.getApplication().invokeLater(this::update)
        }
    }
}
