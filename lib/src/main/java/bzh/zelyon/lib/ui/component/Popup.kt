package bzh.zelyon.lib.ui.component

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import java.util.*

class Popup (
    val activity: Activity,
    val icon: Drawable? = null,
    val title: String? = null,
    val message: String? = null,
    val positiveText: String? = null,
    val negativeText: String? = null,
    val neutralText: String? = null,
    val positiveClick: View.OnClickListener? = null,
    val negativeClick: View.OnClickListener? = null,
    val neutralClick: View.OnClickListener? = null,
    val positiveDismiss: Boolean = true,
    val negativeDismiss: Boolean = true,
    val neutralDismiss: Boolean = true,
    val choicesText: Array<String> = arrayOf(),
    val choicesClick: Array<View.OnClickListener> = arrayOf(),
    val onDismissListener: DialogInterface.OnDismissListener? = null,
    val onShowListener: DialogInterface.OnShowListener? = null,
    val customView: View? = null,
    val cancelable: Boolean = true,
    val defaultDate: Date? = null,
    val minDate: Date? = null,
    val maxDate: Date? = null,
    val onDateSetListener: DatePickerDialog.OnDateSetListener? = null,
    val onTimeSetListener: TimePickerDialog.OnTimeSetListener? = null) {

    fun show() {
        dismiss()
        val alertDialogBuilder = AlertDialog.Builder(activity).apply {
            setCancelable(cancelable)
            icon?.let { icon ->
                setIcon(icon)
            }
            title?.let { title ->
                setTitle(title)
            }
            message?.let { message ->
                setMessage(message)
            }
            positiveText?.let { positiveText ->
                setPositiveButton(positiveText) { _, _ ->
                    positiveClick?.onClick(null)
                }
            }
            negativeText?.let { negativeText ->
                setPositiveButton(negativeText) { _, _ ->
                    negativeClick?.onClick(null)
                }
            }
            neutralText?.let { neutralText ->
                setPositiveButton(neutralText) { _, _ ->
                    neutralClick?.onClick(null)
                }
            }
            if (choicesText.isNotEmpty()) {
                setItems(choicesText) { _, which ->
                    choicesClick[which].onClick(null)
                }
            }
        }
        alertDialog = alertDialogBuilder.create().apply {
            customView?.let { customView ->
                if (customView.parent != null) {
                    (customView.parent as ViewGroup).removeAllViews()
                }
                setView(customView)
            }
            onDismissListener?.let { listener ->
                setOnDismissListener(listener)
            }
            setOnShowListener { listener ->
                onShowListener?.onShow(listener)
                if (!positiveDismiss) {
                    getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(positiveClick)
                }
                if (!negativeDismiss) {
                    getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(negativeClick)
                }
                if (!neutralDismiss) {
                    getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(neutralClick)
                }
            }
        }
        alertDialog?.show()
    }

    fun showBottom() {
        dismissBottom()
        bottomSheetDialog = BottomSheetDialog(activity).apply {
            setCancelable(cancelable)
            val layout = LinearLayout(activity)
            layout.orientation = LinearLayout.VERTICAL
            layout.gravity = Gravity.CENTER_HORIZONTAL
            icon?.let { icon ->
                val imageView = ImageView(activity)
                imageView.setImageDrawable(icon)
                layout.addView(imageView, ViewParams(activity, 64, 64).centerHorizontalGravity().margins(12).linear())
            }
            title?.let { title ->
                val textView = TextView(activity)
                textView.gravity = Gravity.CENTER_HORIZONTAL
                textView.textSize = 18F
                textView.typeface = Typeface.DEFAULT_BOLD
                textView.text = title
                layout.addView(textView, ViewParams(activity).margins(12).linear())
            }
            message?.let { message ->
                val textView = TextView(activity)
                textView.gravity = Gravity.CENTER_HORIZONTAL
                textView.textSize = 14F
                textView.text = message
                layout.addView(textView, ViewParams(activity).margins(12).linear())
            }
            if (choicesText.isNotEmpty()) {

                val itemsView = CollectionsView(activity)
                itemsView.items = choicesText.toMutableList()
                itemsView.helper = object : CollectionsView.Helper() {
                    override fun onBindItem(itemView: View, items: MutableList<*>, position: Int) {
                        val item = items[position]
                        if (item is String) {
                            itemView.findViewById<TextView>(android.R.id.text1).text = item
                        }
                    }

                    override fun onItemClick(itemView: View, items: MutableList<*>, position: Int) {
                        choicesClick[position].onClick(null)
                    }
                }

                layout.addView(itemsView, ViewParams(activity).margins(12).linear())
            }
            customView?.let { customView ->
                layout.addView(customView)
            }
            positiveText?.let { positiveText ->
                val materialButton = MaterialButton(activity)
                materialButton.text = positiveText
                materialButton.setOnClickListener {
                    positiveClick?.onClick(null)
                    if (positiveDismiss) {
                        bottomSheetDialog?.dismiss()
                    }
                }
                layout.addView(materialButton, ViewParams(activity).margins(4).linear())
            }
            negativeText?.let { negativeText ->
                val materialButton = MaterialButton(activity)
                materialButton.text = negativeText
                materialButton.setOnClickListener {
                    negativeClick?.onClick(null)
                    if (negativeDismiss) {
                        bottomSheetDialog?.dismiss()
                    }
                }
                layout.addView(materialButton, ViewParams(activity).margins(4).linear())
            }
            neutralText?.let { neutralText ->
                val materialButton = MaterialButton(activity)
                materialButton.text = neutralText
                materialButton.setOnClickListener {
                    neutralClick?.onClick(null)
                    if (neutralDismiss) {
                        bottomSheetDialog?.dismiss()
                    }
                }
                layout.addView(materialButton, ViewParams(activity).margins(4).linear())
            }
            setContentView(layout)
            onDismissListener?.let { listener ->
                setOnDismissListener(listener)
            }
            setOnShowListener { listener ->
                onShowListener?.onShow(listener)
                BottomSheetBehavior.from(layout.parent as View).state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
        bottomSheetDialog?.show()
    }

    fun dateTime() {
        val calendar = Calendar.getInstance()
        calendar.time = defaultDate ?: Date()
        val datePickerDialog = DatePickerDialog(
            activity,
            DatePickerDialog.OnDateSetListener { datePicker, year, month, dayOfMonth ->
                onDateSetListener?.onDateSet(datePicker, year, month, dayOfMonth)
                onTimeSetListener?.let { onTimeSetListener ->
                    val timePickerDialog = TimePickerDialog(
                        activity,
                        onTimeSetListener,
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        true
                    )
                    timePickerDialog.setCancelable(cancelable)
                    icon?.let { icon ->
                        timePickerDialog.setIcon(icon)
                    }
                    title?.let { title ->
                        timePickerDialog.setTitle(title)
                    }
                    message?.let { message ->
                        timePickerDialog.setMessage(message)
                    }
                    positiveText?.let { positiveText ->
                        timePickerDialog.setButton(AlertDialog.BUTTON_POSITIVE, positiveText) { _, _ ->
                            positiveClick?.onClick(null)
                        }
                    }
                    negativeText?.let { negativeText ->
                        timePickerDialog.setButton(AlertDialog.BUTTON_NEGATIVE, negativeText) { _, _ ->
                            negativeClick?.onClick(null)
                        }
                    }
                    neutralText?.let { neutralText ->
                        timePickerDialog.setButton(AlertDialog.BUTTON_NEUTRAL, neutralText) { _, _ ->
                            neutralClick?.onClick(null)
                        }
                    }
                    customView?.let { customView ->
                        timePickerDialog.setContentView(customView)
                    }
                    onDismissListener?.let { listener ->
                        timePickerDialog.setOnDismissListener(listener)
                    }
                    onShowListener?.let { listener ->
                        timePickerDialog.setOnShowListener(listener)
                    }
                    timePickerDialog.show()
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            setCancelable(cancelable)
            icon?.let { icon ->
                setIcon(icon)
            }
            title?.let { title ->
                setTitle(title)
            }
            message?.let { message ->
                setMessage(message)
            }
            positiveText?.let { positiveText ->
                setButton(DialogInterface.BUTTON_POSITIVE, positiveText) { dialog, _ ->
                    positiveClick?.onClick(null)
                    if (positiveDismiss) {
                        dialog.dismiss()
                    }
                }
            }
            negativeText?.let { negativeText ->
                setButton(DialogInterface.BUTTON_NEGATIVE, negativeText) { dialog, _ ->
                    negativeClick?.onClick(null)
                    if (negativeDismiss) {
                        dialog.dismiss()
                    }
                }
            }
            neutralText?.let { neutralText ->
                setButton(DialogInterface.BUTTON_NEUTRAL, neutralText) { dialog, _ ->
                    neutralClick?.onClick(null)
                    if (neutralDismiss) {
                        dialog.dismiss()
                    }
                }
            }
            customView?.let { customView ->
                setContentView(customView)
            }
            onDismissListener?.let { listener ->
                setOnDismissListener(listener)
            }
            onShowListener?.let { listener ->
                setOnShowListener(listener)
            }
            minDate?.let { minDate ->
                datePicker.minDate = minDate.time
            }
            maxDate?.let { maxDate ->
                datePicker.minDate = maxDate.time
            }
        }.show()
    }

    companion object {
        private var alertDialog: AlertDialog? = null
        private var bottomSheetDialog: BottomSheetDialog? = null
        fun dismiss() {
            alertDialog?.dismiss()
        }
        fun dismissBottom() {
            bottomSheetDialog?.dismiss()
        }
    }
}