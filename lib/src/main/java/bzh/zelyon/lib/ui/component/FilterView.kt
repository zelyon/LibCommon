package bzh.zelyon.lib.ui.component

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import bzh.zelyon.lib.R
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.view_filter.view.*

class FilterView<T> @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0): FrameLayout(context, attrs, defStyleAttr) {

    private var items = listOf<Item<T>>()
    private var appliedValues = listOf<T>()
    private var selectedValues = mutableListOf<T>()

    init {
        View.inflate(context, R.layout.view_filter, this)
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.FilterView, defStyleAttr, 0)
        title.text = typedArray.getString(R.styleable.FilterView_title)
        typedArray.recycle()
    }

    fun load(items: List<Item<T>>, selectedAndApplyValue: List<T>) {
        this.items = items
        this.appliedValues = selectedAndApplyValue
        this.selectedValues = selectedAndApplyValue.toMutableList()
        reload()
    }

    fun getSelectedAndApply(): List<T> {
        appliedValues = selectedValues
        return appliedValues
    }

    fun restore() {
        selectedValues = appliedValues.toMutableList()
        reload()
    }

    fun clear() {
        selectedValues.clear()
        reload()
    }

    private fun reload() {
        chip_group.removeAllViews()
        for (item in items) {
            val chip = Chip(context).apply {
                text = item.label
                tag = item.value
                isCheckable = true
                isChecked = selectedValues.contains(item.value)
                chipIcon = item.image
                setOnCheckedChangeListener { _, _ ->
                    if (selectedValues.contains(item.value)) {
                        selectedValues.remove(item.value)
                    } else {
                        selectedValues.add(item.value)
                    }
                }
            }
            chip_group.addView(chip)
        }
    }

    class Item<T> (val value: T, val label: String, val image: Drawable)
}