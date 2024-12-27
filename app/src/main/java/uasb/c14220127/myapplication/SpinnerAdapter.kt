package uasb.c14220127.myapplication

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

class SpinnerAdapter(
    private val context: Context,
    private val items: List<SpinnerItem>
) : BaseAdapter() {

    override fun getCount(): Int = items.size

    override fun getItem(position: Int): SpinnerItem = items[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.spinner_item, parent, false)
        val icon = view.findViewById<ImageView>(R.id.item_icon)
        val text = view.findViewById<TextView>(R.id.item_text)

        val item = getItem(position)
        icon.setImageResource(item.iconResId)
        text.text = item.text

        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return getView(position, convertView, parent)
    }
}

data class SpinnerItem(val iconResId: Int, val text: String)