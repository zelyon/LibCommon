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
import androidx.core.widget.NestedScrollView
import bzh.zelyon.lib.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import kotlinx.android.synthetic.main.item_popup_bottom.view.*
import java.util.*

class Popup (
    val activity: Activity,
    private val icon: Drawable? = null,
    private val title: String? = null,
    private val message: String? = null,
    private val positiveText: String? = null,
    private val negativeText: String? = null,
    private val neutralText: String? = null,
    private val positiveClick:() -> Unit = {},
    private val negativeClick:() -> Unit = {},
    private val neutralClick:() -> Unit = {},
    private val positiveDismiss: Boolean = true,
    private val negativeDismiss: Boolean = true,
    private val neutralDismiss: Boolean = true,
    private val choices: List<Choice> = listOf(),
    private val onDismissListener: DialogInterface.OnDismissListener? = null,
    private val onShowListener: DialogInterface.OnShowListener? = null,
    private val customView: View? = null,
    private val cancelable: Boolean = true,
    private val defaultDate: Date? = null,
    private val minDate: Date? = null,
    private val maxDate: Date? = null,
    private val onDateSetListener: DatePickerDialog.OnDateSetListener? = null,
    private val onTimeSetListener: TimePickerDialog.OnTimeSetListener? = null) {

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
                    positiveClick.invoke()
                }
            }
            negativeText?.let { negativeText ->
                setPositiveButton(negativeText) { _, _ ->
                    negativeClick.invoke()
                }
            }
            neutralText?.let { neutralText ->
                setPositiveButton(neutralText) { _, _ ->
                    neutralClick.invoke()
                }
            }
            if (choices.isNotEmpty()) {
                setItems(choices.map { it.label }.toTypedArray()) { _, which ->
                    choices.map { it.callback }[which].invoke()
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
                    getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener{
                        positiveClick.invoke()
                    }
                }
                if (!negativeDismiss) {
                    getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener{
                        negativeClick.invoke()
                    }
                }
                if (!neutralDismiss) {
                    getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener{
                        neutralClick.invoke()
                    }
                }
            }
        }
        alertDialog?.show()
    }

    fun showBottom() {
        dismissBottom()
        bottomSheetDialog = BottomSheetDialog(activity).apply {
            setCancelable(cancelable)
            val nestedScrollView = NestedScrollView(activity)
            nestedScrollView.addView(LinearLayout(activity).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER_HORIZONTAL
                icon?.let { icon ->
                    val imageView = ImageView(activity)
                    imageView.setImageDrawable(icon)
                    addView(imageView, ViewParams(activity, 64, 64).centerHorizontalGravity().margins(12).linear())
                }
                title?.let { title ->
                    val textView = TextView(activity)
                    textView.gravity = Gravity.CENTER_HORIZONTAL
                    textView.textSize = 18F
                    textView.typeface = Typeface.DEFAULT_BOLD
                    textView.text = title
                    addView(textView, ViewParams(activity).margins(12).linear())
                }
                message?.let { message ->
                    val textView = TextView(activity)
                    textView.gravity = Gravity.CENTER_HORIZONTAL
                    textView.textSize = 14F
                    textView.text = message
                    addView(textView, ViewParams(activity).margins(12).linear())
                }
                customView?.let { customView ->
                    addView(customView)
                }
                if (choices.isNotEmpty()) {
                    val collectionsView = CollectionsView(activity)
                    collectionsView.items = choices.map { it.label }.toMutableList()
                    collectionsView.idLayoutItem = R.layout.item_popup_bottom
                    collectionsView.helper = object : CollectionsView.Helper() {
                        override fun onBindItem(itemView: View, items: MutableList<*>, position: Int) {
                            val item = items[position]
                            if (item is String) {
                                itemView.item_popup_bottom_textview.text = item
                            }
                        }

                        override fun onItemClick(itemView: View, items: MutableList<*>, position: Int) {
                            choices.map { it.callback }[position].invoke()
                        }
                    }

                    addView(collectionsView, ViewParams(activity).margins(12, 0).linear())
                }
                positiveText?.let { positiveText ->
                    val materialButton = MaterialButton(activity)
                    materialButton.text = positiveText
                    materialButton.setOnClickListener {
                        positiveClick.invoke()
                        if (positiveDismiss) {
                            datePickerDialog?.dismiss()
                        }
                    }
                    addView(materialButton, ViewParams(activity).margins(4).linear())
                }
                negativeText?.let { negativeText ->
                    val materialButton = MaterialButton(activity)
                    materialButton.text = negativeText
                    materialButton.setOnClickListener {
                        negativeClick.invoke()
                        if (negativeDismiss) {
                            datePickerDialog?.dismiss()
                        }
                    }
                    addView(materialButton, ViewParams(activity).margins(4).linear())
                }
                neutralText?.let { neutralText ->
                    val materialButton = MaterialButton(activity)
                    materialButton.text = neutralText
                    materialButton.setOnClickListener {
                        neutralClick.invoke()
                        if (neutralDismiss) {
                            datePickerDialog?.dismiss()
                        }
                    }
                    addView(materialButton, ViewParams(activity).margins(4).linear())
                }
            })
            setContentView(nestedScrollView)
            onDismissListener?.let { listener ->
                setOnDismissListener(listener)
            }
            setOnShowListener { listener ->
                onShowListener?.onShow(listener)
                BottomSheetBehavior.from(nestedScrollView.parent as View).state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
        bottomSheetDialog?.show()
    }

    fun dateTime() {
        val calendar = Calendar.getInstance()
        calendar.time = defaultDate ?: Date()
        datePickerDialog = DatePickerDialog(
            activity,
            DatePickerDialog.OnDateSetListener { datePicker, year, month, dayOfMonth ->
                onDateSetListener?.onDateSet(datePicker, year, month, dayOfMonth)
                onTimeSetListener?.let { onTimeSetListener ->
                    timePickerDialog = TimePickerDialog(
                        activity,
                        onTimeSetListener,
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        true
                    )
                    timePickerDialog?.setCancelable(cancelable)
                    icon?.let { icon ->
                        timePickerDialog?.setIcon(icon)
                    }
                    title?.let { title ->
                        timePickerDialog?.setTitle(title)
                    }
                    message?.let { message ->
                        timePickerDialog?.setMessage(message)
                    }
                    positiveText?.let { positiveText ->
                        timePickerDialog?.setButton(AlertDialog.BUTTON_POSITIVE, positiveText) { _, _ ->
                            positiveClick.invoke()
                        }
                    }
                    negativeText?.let { negativeText ->
                        timePickerDialog?.setButton(AlertDialog.BUTTON_NEGATIVE, negativeText) { _, _ ->
                            negativeClick.invoke()
                        }
                    }
                    neutralText?.let { neutralText ->
                        timePickerDialog?.setButton(AlertDialog.BUTTON_NEUTRAL, neutralText) { _, _ ->
                            neutralClick.invoke()
                        }
                    }
                    customView?.let { customView ->
                        timePickerDialog?.setContentView(customView)
                    }
                    onDismissListener?.let { listener ->
                        timePickerDialog?.setOnDismissListener(listener)
                    }
                    onShowListener?.let { listener ->
                        timePickerDialog?.setOnShowListener(listener)
                    }
                    timePickerDialog?.show()
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
                    positiveClick.invoke()
                    if (positiveDismiss) {
                        dialog.dismiss()
                    }
                }
            }
            negativeText?.let { negativeText ->
                setButton(DialogInterface.BUTTON_NEGATIVE, negativeText) { dialog, _ ->
                    negativeClick.invoke()
                    if (negativeDismiss) {
                        dialog.dismiss()
                    }
                }
            }
            neutralText?.let { neutralText ->
                setButton(DialogInterface.BUTTON_NEUTRAL, neutralText) { dialog, _ ->
                    neutralClick.invoke()
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
        }
        datePickerDialog?.show()
    }

    class Choice(
        val label: String,
        val callback: () -> Unit,
    )

    companion object {
        private var alertDialog: AlertDialog? = null
        private var bottomSheetDialog: BottomSheetDialog? = null
        private var datePickerDialog: DatePickerDialog? = null
        private var timePickerDialog: TimePickerDialog? = null
        fun dismiss() {
            alertDialog?.dismiss()
        }
        fun dismissBottom() {
            bottomSheetDialog?.dismiss()
        }
        fun dismissDatePicker() {
            datePickerDialog?.dismiss()
        }
        fun dismissTimePicker() {
            timePickerDialog?.dismiss()
        }
    }
}