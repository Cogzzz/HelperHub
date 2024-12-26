package uasb.c14220127.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class WorkerAdapter(private val workerList: List<Worker>) : RecyclerView.Adapter<WorkerAdapter.WorkerViewHolder>() {

    class WorkerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Changed IDs to match the layout file
        val img: ImageView = itemView.findViewById(R.id.img)
        val degreeTxt: TextView = itemView.findViewById(R.id.degreeTxt)
        val namesTxt: TextView = itemView.findViewById(R.id.namesTxt)  // Changed from nameTxt
        val specialsTxt: TextView = itemView.findViewById(R.id.specialsTxt)  // Changed from specialTxt
        val makeBtn: Button = itemView.findViewById(R.id.makeBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.viewholder_listdoctor, parent, false)
        return WorkerViewHolder(view)
    }

    override fun onBindViewHolder(holder: WorkerViewHolder, position: Int) {
        val worker = workerList[position]

        // Load image using Glide
        worker.imageUrl?.let { imageUrl ->
            Glide.with(holder.img.context)
                .load(imageUrl)
                .into(holder.img)
        }

        // Set text fields with null safety
        holder.namesTxt.text = worker.name ?: ""
        holder.degreeTxt.text = worker.degree ?: ""
        holder.specialsTxt.text = worker.specialization ?: ""
    }

    override fun getItemCount(): Int {
        return workerList.size
    }
}