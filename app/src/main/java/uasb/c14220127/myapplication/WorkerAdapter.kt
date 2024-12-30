package uasb.c14220127.myapplication

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class WorkerAdapter(private val workerList: List<Worker>, private val context: Context,private val onItemClick: (String) -> Unit) : RecyclerView.Adapter<WorkerAdapter.WorkerViewHolder>() {

    class WorkerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val img: ImageView = itemView.findViewById(R.id.img)
        val degreeTxt: TextView = itemView.findViewById(R.id.degreeTxt)
        val namesTxt: TextView = itemView.findViewById(R.id.namesTxt)
        val specialsTxt: TextView = itemView.findViewById(R.id.specialsTxt)
        val makeBtn: Button = itemView.findViewById(R.id.makeBtn)
        val editBtn: Button = itemView.findViewById(R.id.editBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkerViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.viewholder_listworker, parent, false)
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

        // Null safety for workerId
        val workerId = worker.workerId ?: ""

        // Set click listener on the entire item
        holder.itemView.setOnClickListener {
            if (workerId.isNotEmpty()) {
                val intent = Intent(context, DetailActivity::class.java)
                intent.putExtra("worker_id", workerId)  // Send worker ID to DetailActivity
                context.startActivity(intent)
            }
        }

        // Optional: Button triggers the same action
        holder.makeBtn.setOnClickListener {
            if (workerId.isNotEmpty()) {
                val intent = Intent(context, DetailActivity::class.java)
                intent.putExtra("worker_id", workerId)  // Send worker ID to DetailActivity
                context.startActivity(intent)
            }
        }
        holder.editBtn.setOnClickListener {
            if (workerId.isNotEmpty()) {
                val intent = Intent(context, EditActivity::class.java)
                intent.putExtra("worker_id", workerId)  // Send worker ID to DetailActivity
                context.startActivity(intent)
            }
        }
    }


    override fun getItemCount(): Int {
        return workerList.size
    }
}
