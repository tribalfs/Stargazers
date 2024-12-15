package com.tribalfs.stargazers.ui.screens.main.core.navigation

import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.MarginLayoutParams
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.FloatRange
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.tribalfs.stargazers.R
import com.tribalfs.stargazers.ui.screens.main.core.navigation.DrawerItem.Companion.VIEW_TYPE
import dev.oneuiproject.oneui.ktx.dpToPx
import dev.oneuiproject.oneui.ktx.semSetToolTipText
import dev.oneuiproject.oneui.utils.getRegularFont
import dev.oneuiproject.oneui.utils.getSemiBoldFont


class DrawerNavAdapter(
    private val destinations: List<DrawerItem>,
    private val mListener: DrawerListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var mSelectedId = 0

    fun interface DrawerListener {
        fun onDrawerItemSelected(destinationId: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when(viewType){
            DrawerItem.DestinationItem.VIEW_TYPE -> {
                DestinationItemViewHolder(inflater.inflate(R.layout.view_drawer_list_item, parent, false) )
            }
            DrawerItem.DividerItem.VIEW_TYPE -> {
                DividerItemViewHolder(inflater.inflate(R.layout.view_drawer_list_separator, parent, false) )
            }
            DrawerItem.Button.VIEW_TYPE -> {
                ButtonViewHolder(inflater.inflate(R.layout.view_drawer_list_button, parent, false).apply {

                })
            }
            else -> throw IllegalArgumentException()
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            for (payload in payloads.toSet()) {
                when (payload) {
                    is Payload.OFFSET -> {
                        when (holder) {
                            is DestinationItemViewHolder -> {
                                holder.applyOffset(payload.slideOffset)
                            }
                            is ButtonViewHolder -> {
                                holder.applyOffset(payload.slideOffset)
                            }
                            is DividerItemViewHolder -> {
                                holder.applyOffset(payload.slideOffset)
                            }
                        }
                    }
                    else -> Unit
                }
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is DestinationItemViewHolder -> {
                val destination = destinations[position] as DrawerItem.DestinationItem
                holder.apply {
                    setIcon(destination.iconResId)
                    setTitle(destination.title)
                    setSelected(destination.id == mSelectedId)
                    itemView.setOnClickListener {
                        mListener.onDrawerItemSelected(destination.id)
                    }
                    itemView.semSetToolTipText(destination.title)
                }
            }
            is DividerItemViewHolder -> Unit
            is ButtonViewHolder -> {
                val button = destinations[position] as DrawerItem.Button
                holder.itemView.findViewById<AppCompatButton>(R.id.button).apply {
                    setOnClickListener(button.onClick)
                }
            }
        }

    }

    override fun getItemCount(): Int = destinations.size

    override fun getItemViewType(position: Int): Int = destinations[position].VIEW_TYPE

    fun setSelectedDestinationId(selectedId: Int) {
        mSelectedId = selectedId
        notifyItemRangeChanged(0, itemCount)
    }

    fun updateOffset(@FloatRange(0.0, 1.0) slideOffset: Float) {
        Log.d("DrawerNavAdapter", "updateOffset: $slideOffset")
        notifyItemRangeChanged(0, itemCount, Payload.OFFSET(slideOffset))
    }

    class DestinationItemViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        private var mIconView: AppCompatImageView? = null
        private var mTitleView: TextView? = null

        init {
            mIconView = itemView.findViewById(R.id.drawer_item_icon)
            mTitleView = itemView.findViewById(R.id.drawer_item_title)
        }

        fun setIcon(@DrawableRes resId: Int) {
            mIconView!!.setImageResource(resId)
        }

        fun setTitle(title: CharSequence?) {
            mTitleView!!.text = title
        }

        fun setSelected(selected: Boolean) {
            itemView.isSelected = selected
            mTitleView!!.typeface = if (selected) getSemiBoldFont() else getRegularFont()
            mTitleView!!.ellipsize =
                if (selected) TextUtils.TruncateAt.MARQUEE else TextUtils.TruncateAt.END

        }

        fun applyOffset(offset: Float){
            mTitleView!!.alpha = offset
            if (offset == 0f){
                itemView.post {
                    itemView.updateLayoutParams<MarginLayoutParams> {
                        width = 52f.dpToPx(itemView.context.resources)
                    }
                }
            }else{
                if (itemView.width != MATCH_PARENT) {
                    itemView.updateLayoutParams<MarginLayoutParams> {
                        width = MATCH_PARENT
                    }
                }
            }
        }
    }

    class DividerItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        fun applyOffset(offset: Float){
            if (offset == 0f){
                itemView.post {
                    itemView.updateLayoutParams<MarginLayoutParams> {
                        width = 25f.dpToPx(itemView.context.resources)
                    }
                }
            }else{
                if (itemView.width != MATCH_PARENT) {
                    itemView.updateLayoutParams<MarginLayoutParams> {
                        width = MATCH_PARENT
                    }
                }
            }
        }
    }

    class ButtonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        private var button: AppCompatButton = itemView.findViewById(R.id.button)

        fun applyOffset(offset: Float){
            button.alpha = offset
        }
    }
}

sealed class Payload{
    data class OFFSET(val slideOffset: Float): Payload()
}
