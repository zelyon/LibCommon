package bzh.zelyon.lib.ui.component

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import bzh.zelyon.lib.R
import bzh.zelyon.lib.extension.*
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.round

class CollectionsView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0): RecyclerView(context, attrs, defStyleAttr) {

    var items: MutableList<*> = mutableListOf<Any>()
        set(value) {
            field = value
            refresh()
        }

    var helper: Helper? = null
        set(value) {
            field = value
            recreate()
        }

    var idLayoutItem = R.layout.item_empty
        set(value) {
            field = value
            recreate()
        }

    var idLayoutHeader = R.layout.item_empty
        set(value) {
            field = value
            recreate()
        }

    var idLayoutFooter = R.layout.item_empty
        set(value) {
            field = value
            recreate()
        }

    var idLayoutEmpty = R.layout.item_empty
        set(value) {
            field = value
            recreate()
        }

    var headerHeight: Float? = null
        set(value) {
            field = if (value != -1F) value else null
            recreate()
        }

    var footerHeight: Float? = null
        set(value) {
            field = if (value != -1F) value else null
            recreate()
        }

    var nbColumns = 1
        set(value) {
            field = value
            layoutManager = when (value) {
                1 -> LinearLayoutManager(context)
                else -> GridLayoutManager(context, value).apply {
                    spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                        override fun getSpanSize(position: Int) = if (position == 0 || position == items.size + 1) value else 1
                    }
                }
            }
            refresh()
        }

    var spaceDivider: Int = 0
        set(value) {
            field = value
            addItemDecoration(object : ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: State
                ) {
                    if (spaceDivider > 0) {
                        var position = parent.getChildAdapterPosition(view)
                        if (position != 0 && position != items.size + 1 && !items.isNullOrEmpty()) {
                            position--
                            outRect.top = if (position < nbColumns) value else value / 2
                            outRect.left = if (position % nbColumns == 0) value else value / 2
                            outRect.right = if ((position + 1) % nbColumns == 0) value else value / 2
                            outRect.bottom = if (position > items.size - nbColumns) value else value / 2
                        }
                    }
                }
            })
            refresh()
        }

    var dragNDropEnable = false
        set(value) {
            field = value
            itemTouchHelper.attachToRecyclerView(if (value || swipeEnable) this else null)
        }

    var swipeEnable = false
        set(value) {
            field = value
            itemTouchHelper.attachToRecyclerView(if (value || dragNDropEnable) this else null)
        }

    var fastScrollEnable: Boolean = false
        set(value) {
            field = value
            if (value) {
                addOnItemTouchListener(scrollItemDecorator)
                addItemDecoration(scrollItemDecorator)
            } else {
                removeOnItemTouchListener(scrollItemDecorator)
                removeItemDecoration(scrollItemDecorator)
            }
            refresh()
        }

    @ColorInt
    var thumbEnableColor = context.getColorIntFromAndroidAttr(if (isOreo()) android.R.attr.colorSecondary else android.R.attr.colorAccent)
    @ColorInt
    var thumbDisableColor = context.colorResToColorInt(android.R.color.darker_gray)
    @ColorInt
    var thumbTextColor =  context.colorResToColorInt(android.R.color.white)
    var thumbMinHeight = context.dpToPx(36)
    var thumbWidth = context.dpToPx(4)
    var thumbCorner = context.dpToPx(8)
    var thumbMarginLeft = context.dpToPx(8)
    var thumbMarginRight = context.dpToPx(0)
    var thumbMarginTop = context.dpToPx(0)
    var thumbMarginBottom = context.dpToPx(0)
    var thumbTextSize = context.dpToPx(32)
    var thumbShape = ThumbShape.TEARDROP

    private var thumbHeight = 0F
    private var thumbTop = 0F
    private val thumbBottom get() = thumbTop + thumbHeight
    private val thumbLeft get() = right - thumbMarginRight - thumbWidth
    private val thumbRight get() = thumbLeft + thumbWidth
    private val thumbCenterY get() = thumbTop + thumbHeight/2
    private val thumbMinY get() = top + thumbMarginTop
    private val thumbMaxY get() = bottom - thumbMarginBottom
    private val thumbScrollingHeight get() = height - thumbMarginTop - thumbMarginBottom
    private val thumbMoveHeight get() = computeVerticalScrollRange() - thumbMarginTop - thumbMarginBottom
    private var thumbNeedShow = false
    private var thumbDragging = false

    private val scrollItemDecorator = object : ItemDecoration(), OnItemTouchListener {

        private var motionEventY = 0F

        override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: State) {
            if (thumbNeedShow) {
                val thumbPaint = Paint().apply {
                    color = if (thumbDragging) thumbEnableColor else thumbDisableColor
                    isAntiAlias = true
                }
                canvas.drawRoundRect(thumbLeft, thumbTop, thumbRight, thumbBottom, thumbCorner, thumbCorner, thumbPaint)
                if (thumbDragging) {
                    val position = round(((items.size - 1) * (thumbTop - thumbMarginTop) / thumbScrollingHeight)).toInt()
                    helper?.getIndexScroll(items, position)?.let { index ->
                        val centerX = thumbLeft - thumbMarginLeft - thumbTextSize*1.5f
                        val centerY = when {
                            thumbCenterY - thumbTextSize < thumbMinY -> thumbMinY + thumbTextSize
                            thumbCenterY + thumbTextSize > thumbMaxY -> thumbMaxY - thumbTextSize
                            else -> thumbCenterY
                        }
                        when (thumbShape) {
                            ThumbShape.TEARDROP -> {
                                canvas.drawCircle(centerX, centerY, thumbTextSize, thumbPaint)
                                canvas.save()
                                canvas.rotate(-45F, centerX, centerY)
                                canvas.drawRoundRect(centerX, centerY, centerX + thumbTextSize, centerY + thumbTextSize, thumbCorner, thumbCorner, thumbPaint)
                                canvas.drawRect(centerX, centerY, centerX + thumbCorner, centerY + thumbTextSize, thumbPaint)
                                canvas.drawRect(centerX, centerY, centerX + thumbTextSize, centerY + thumbCorner, thumbPaint)
                                canvas.restore()
                            }
                            ThumbShape.CIRCLE -> canvas.drawCircle(centerX, centerY, thumbTextSize, thumbPaint)
                            ThumbShape.SQUARE -> canvas.drawRect(centerX - thumbTextSize, centerY - thumbTextSize, centerX + thumbTextSize, centerY + thumbTextSize, thumbPaint)
                            ThumbShape.ROUND_SQUARE -> canvas.drawRoundRect(centerX - thumbTextSize, centerY - thumbTextSize, centerX + thumbTextSize, centerY + thumbTextSize, thumbTextSize / 2, thumbTextSize / 2, thumbPaint)
                            ThumbShape.SQUIRCLE -> canvas.drawPath(Path().apply {
                                val thumbTextSizeInt = thumbTextSize.toInt()
                                val thumbTextSizePow3 = thumbTextSize.toDouble().pow(3.0)
                                moveTo(-thumbTextSize, 0f)
                                for (x in -thumbTextSizeInt..thumbTextSizeInt) {
                                    lineTo(x.toFloat(), Math.cbrt(thumbTextSizePow3 - abs(x.toDouble().pow(3.0))).toFloat())
                                }
                                for (x in thumbTextSizeInt downTo -thumbTextSizeInt) {
                                    lineTo(x.toFloat(), (-Math.cbrt(thumbTextSizePow3 - abs(x.toDouble().pow(3.0)))).toFloat())
                                }
                                close()
                                transform(Matrix().apply {
                                    postTranslate(centerX, centerY)
                                })
                            }, thumbPaint)
                        }
                        canvas.drawText(index, centerX, centerY + thumbTextSize / 3, Paint().apply {
                            textSize = thumbTextSize
                            textAlign = Paint.Align.CENTER
                            color = thumbTextColor
                        })
                    }
                }
            }
        }

        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}

        override fun onTouchEvent(recyclerView: RecyclerView, ev: MotionEvent) {
            onInterceptTouchEvent(ev)
        }

        override fun onInterceptTouchEvent(recyclerView: RecyclerView, motionEvent: MotionEvent) =
            if (thumbNeedShow &&
                motionEvent.action == MotionEvent.ACTION_DOWN &&
                motionEvent.x > thumbLeft - thumbMarginLeft &&
                motionEvent.y in thumbTop..thumbBottom) {
                motionEventY = motionEvent.y
                thumbDragging = true
                true
            } else if (thumbNeedShow &&
                motionEvent.action == MotionEvent.ACTION_UP &&
                thumbDragging) {
                motionEventY = 0f
                thumbDragging = false
                invalidate()
                false
            } else if (thumbNeedShow &&
                motionEvent.action == MotionEvent.ACTION_MOVE &&
                thumbDragging &&
                abs(thumbTop - motionEvent.y) >= 2) {
                val totalPossibleOffset = (thumbMoveHeight - thumbScrollingHeight).toInt()
                val scrollingBy = ((motionEvent.y - motionEventY) / thumbScrollingHeight * totalPossibleOffset).toInt()
                if (computeVerticalScrollOffset() + scrollingBy in 0 until totalPossibleOffset) {
                    scrollBy(0, scrollingBy)
                }
                motionEventY = motionEvent.y
                true
            } else false

        fun onScroll() {
            if (thumbMoveHeight > thumbScrollingHeight && items.isNotEmpty()) {
                thumbNeedShow = true
                thumbHeight = max(thumbScrollingHeight.pow(2) / thumbMoveHeight, thumbMinHeight)
                thumbTop = computeVerticalScrollOffset() * thumbScrollingHeight / thumbMoveHeight + thumbMarginTop
            } else if (thumbNeedShow) {
                thumbNeedShow = false
            }
        }
    }

    private var scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            helper?.onScroll(dy <= 0)
            if (fastScrollEnable) {
                scrollItemDecorator.onScroll()
            }
        }
    }

    private val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.Callback() {

        override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: ViewHolder) =
            makeMovementFlags(
                when {
                    dragNDropEnable && nbColumns == 1 -> ItemTouchHelper.UP or ItemTouchHelper.DOWN
                    dragNDropEnable -> ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
                    else -> 0
                },
                if (swipeEnable && nbColumns == 1) ItemTouchHelper.START or ItemTouchHelper.END else 0
            )

        override fun onMove(recyclerView: RecyclerView, viewHolder: ViewHolder, target: ViewHolder): Boolean {
            var sourcePosition = viewHolder.adapterPosition
            var targetPosition = target.adapterPosition
            return if (sourcePosition in 1..items.size && targetPosition in 1..items.size) {
                adapter?.notifyItemMoved(sourcePosition, targetPosition)
                sourcePosition--
                targetPosition--
                if (sourcePosition in items.indices && targetPosition in items.indices) {
                    Collections.swap(items, sourcePosition, targetPosition)
                    helper?.onItemsMove(viewHolder.itemView, items, sourcePosition, targetPosition)
                }
                true
            } else false
        }

        override fun onSwiped(viewHolder: ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition
            items.removeAt(position - 1)
            adapter?.notifyItemRemoved(position)
            adapter?.notifyDataSetChanged()
            helper?.onItemSwipe(viewHolder.itemView, items, position - 1)
        }

        override fun onSelectedChanged(viewHolder: ViewHolder?, actionState: Int) {
            if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                viewHolder?.let {
                    val position = viewHolder.adapterPosition
                    helper?.onItemStartDrag(viewHolder.itemView, items, position - 1)
                }
            }
            super.onSelectedChanged(viewHolder, actionState)
        }

        override fun clearView(recyclerView: RecyclerView, viewHolder: ViewHolder) {
            super.clearView(recyclerView, viewHolder)
            val position = viewHolder.adapterPosition
            if (position != -1) {
                helper?.onItemEndDrag(viewHolder.itemView, items, position - 1)
                adapter?.notifyDataSetChanged()
            }
        }

        override fun isItemViewSwipeEnabled() = true

        override fun isLongPressDragEnabled() = false
    })

    private val itemsAdapter = object : Adapter<ViewHolder>() {

        private val DATA_TYPE = 0
        private val HEADER_TYPE = 1
        private val EMPTY_TYPE = 2
        private val FOOTER_TYPE = 3

        override fun getItemCount() = items.size + if (items.isEmpty()) 3 else 2

        override fun getItemViewType(position: Int) = when {
            position == 0 -> HEADER_TYPE
            position == itemCount - 1 -> FOOTER_TYPE
            items.isEmpty() -> EMPTY_TYPE
            else -> DATA_TYPE
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = object : RecyclerView.ViewHolder(
            LayoutInflater.from(context).inflate(
                when (viewType) {
                    HEADER_TYPE -> idLayoutHeader
                    FOOTER_TYPE -> idLayoutFooter
                    EMPTY_TYPE -> idLayoutEmpty
                    else -> idLayoutItem
                }, parent, false
            )
        ) {}

        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
            when (getItemViewType(position)) {
                HEADER_TYPE -> {
                    headerHeight?.let {
                        viewHolder.itemView.layoutParams.height = it.toInt()
                        viewHolder.itemView.requestLayout()
                    }
                    helper?.onBindHeader(viewHolder.itemView)
                }
                FOOTER_TYPE -> {
                    footerHeight?.let {
                        viewHolder.itemView.layoutParams.height = it.toInt()
                        viewHolder.itemView.requestLayout()
                    }
                    helper?.onBindFooter(viewHolder.itemView)
                }
                EMPTY_TYPE -> {
                    helper?.onBindEmpty(viewHolder.itemView)
                }
                DATA_TYPE -> {
                    helper?.onBindItem(viewHolder.itemView, items, position - 1)
                    viewHolder.itemView.setOnClickListener {
                        helper?.onItemClick(viewHolder.itemView, items, position - 1)
                    }
                    viewHolder.itemView.setOnLongClickListener {
                        helper?.onItemLongClick(viewHolder.itemView, items, position - 1)
                        true
                    }

                    helper?.getDragView(viewHolder.itemView, items, position - 1)?.let { dragView ->
                        dragView.isVisible = items.size > 1
                        dragView.setOnTouchListener { _, event: MotionEvent ->
                            if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                                itemTouchHelper.startDrag(viewHolder)
                            }
                            return@setOnTouchListener true
                        }
                    }
                }
            }
        }
    }

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CollectionsView, defStyleAttr, 0)
        idLayoutItem = typedArray.getResourceId(R.styleable.CollectionsView_id_layout_item, R.layout.item_empty)
        idLayoutHeader = typedArray.getResourceId(R.styleable.CollectionsView_id_layout_header, R.layout.item_empty)
        idLayoutFooter = typedArray.getResourceId(R.styleable.CollectionsView_id_layout_footer, R.layout.item_empty)
        idLayoutEmpty = typedArray.getResourceId(R.styleable.CollectionsView_id_layout_empty, R.layout.item_empty)
        headerHeight = typedArray.getDimension(R.styleable.CollectionsView_header_height, -1F)
        footerHeight = typedArray.getDimension(R.styleable.CollectionsView_footer_height, -1F)
        nbColumns = typedArray.getInt(R.styleable.CollectionsView_nb_colums, 1)
        spaceDivider = typedArray.getDimensionPixelSize(R.styleable.CollectionsView_space_divider, 0)
        dragNDropEnable = typedArray.getBoolean(R.styleable.CollectionsView_drag_n_drop_enable, false)
        swipeEnable = typedArray.getBoolean(R.styleable.CollectionsView_swipe_enable, false)
        fastScrollEnable = typedArray.getBoolean(R.styleable.CollectionsView_fast_scroll_enable, false)
        thumbEnableColor = typedArray.getColor(R.styleable.CollectionsView_thumb_enable_color, context.colorResToColorInt(context.getResIdFromAndroidAttr(android.R.attr.colorAccent)))
        thumbDisableColor = typedArray.getColor(R.styleable.CollectionsView_thumb_disable_color, context.colorResToColorInt(android.R.color.darker_gray))
        thumbTextColor = typedArray.getColor(R.styleable.CollectionsView_thumb_text_color, context.colorResToColorInt(android.R.color.white))
        thumbMinHeight = typedArray.getDimension(R.styleable.CollectionsView_thumb_min_height, context.dpToPx(36))
        thumbWidth = typedArray.getDimension(R.styleable.CollectionsView_thumb_width, context.dpToPx(4))
        thumbCorner = typedArray.getDimension(R.styleable.CollectionsView_thumb_corner, context.dpToPx(8))
        thumbMarginLeft = typedArray.getDimension(R.styleable.CollectionsView_thumb_margin_left, context.dpToPx(8))
        thumbMarginRight = typedArray.getDimension(R.styleable.CollectionsView_thumb_margin_right, 0f)
        thumbMarginTop = typedArray.getDimension(R.styleable.CollectionsView_thumb_margin_top, 0f)
        thumbMarginBottom = typedArray.getDimension(R.styleable.CollectionsView_thumb_margin_bottom, 0f)
        thumbTextSize = typedArray.getDimension(R.styleable.CollectionsView_thumb_text_size, context.dpToPx(32))
        thumbShape = when (typedArray.getInt(R.styleable.CollectionsView_thumb_shape, 0)) {
            0 -> ThumbShape.TEARDROP
            1 -> ThumbShape.CIRCLE
            2 -> ThumbShape.SQUIRCLE
            3 -> ThumbShape.SQUIRCLE
            4 -> ThumbShape.ROUND_SQUARE
            else -> ThumbShape.TEARDROP
        }
        typedArray.recycle()

        adapter = itemsAdapter
        setHasFixedSize(false)
        addOnScrollListener(scrollListener)
    }

    fun refresh() {
        adapter?.notifyDataSetChanged()
    }

    fun recreate() {
        adapter = itemsAdapter
    }

    enum class ThumbShape {
        SQUARE, ROUND_SQUARE, CIRCLE, TEARDROP, SQUIRCLE
    }

    open class Helper {
        open fun onScroll(goUp: Boolean) {}

        open fun onBindHeader(headerView: View) {}
        open fun onBindFooter(footerView: View) {}
        open fun onBindEmpty(emptyView: View) {}

        open fun onBindItem(itemView: View, items: MutableList<*>, position: Int) {}
        open fun onItemClick(itemView: View, items: MutableList<*>, position: Int) {}
        open fun onItemLongClick(itemView: View, items: MutableList<*>, position: Int) {}

        open fun getIndexScroll(items: MutableList<*>, position: Int): String? = null

        open fun getDragView(itemView: View, items: MutableList<*>, position: Int): View? = null
        open fun onItemsMove(itemView: View, items: MutableList<*>, fromPosition: Int, toPosition: Int) {}
        open fun onItemStartDrag(itemView: View, items: MutableList<*>, position: Int) {}
        open fun onItemEndDrag(itemView: View, items: MutableList<*>, position: Int) {}
        open fun onItemSwipe(itemView: View, items: MutableList<*>, position: Int) {}
    }
}