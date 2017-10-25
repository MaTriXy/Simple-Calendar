package com.simplemobiletools.calendar.dialogs

import android.support.v7.app.AlertDialog
import android.view.ViewGroup
import android.widget.LinearLayout
import com.simplemobiletools.calendar.R
import com.simplemobiletools.calendar.activities.SimpleActivity
import com.simplemobiletools.calendar.adapters.FilterEventTypeAdapter
import com.simplemobiletools.calendar.extensions.dbHelper
import com.simplemobiletools.commons.extensions.*
import kotlinx.android.synthetic.main.dialog_export_events.view.*
import java.io.File

class ExportEventsDialog(val activity: SimpleActivity, val path: String, val callback: (exportPastEvents: Boolean, file: File, eventTypes: HashSet<String>) -> Unit) {

    init {
        val view = (activity.layoutInflater.inflate(R.layout.dialog_export_events, null) as ViewGroup).apply {
            export_events_folder.text = activity.humanizePath(path)
            export_events_filename.setText("events_${System.currentTimeMillis() / 1000}")

            activity.dbHelper.getEventTypes {
                val eventTypes = HashSet<String>()
                it.mapTo(eventTypes, { it.id.toString() })

                activity.runOnUiThread {
                    export_events_types_list.adapter = FilterEventTypeAdapter(activity, it, eventTypes)
                    if (it.size > 1) {
                        export_events_pick_types.beVisible()

                        val margin = activity.resources.getDimension(R.dimen.normal_margin).toInt()
                        (export_events_checkbox.layoutParams as LinearLayout.LayoutParams).leftMargin = margin
                    }
                }
            }
        }

        AlertDialog.Builder(activity)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, null)
                .create().apply {
            activity.setupDialogStuff(view, this, R.string.export_events)
            getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener({
                val filename = view.export_events_filename.value
                when {
                    filename.isEmpty() -> activity.toast(R.string.empty_name)
                    filename.isAValidFilename() -> {
                        val file = File(path, "$filename.ics")
                        if (file.exists()) {
                            activity.toast(R.string.name_taken)
                            return@setOnClickListener
                        }

                        val eventTypes = (view.export_events_types_list.adapter as FilterEventTypeAdapter).getSelectedItemsSet()
                        callback(view.export_events_checkbox.isChecked, file, eventTypes)
                        dismiss()
                    }
                    else -> activity.toast(R.string.invalid_name)
                }
            })
        }
    }
}
