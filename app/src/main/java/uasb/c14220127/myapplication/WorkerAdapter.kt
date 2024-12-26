package uasb.c14220127.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

//WORKER ADAPTER
class WorkerAdapter(private val workerList: List<Worker>) : RecyclerView.Adapter<WorkerAdapter.WorkerViewHolder>() {

    class WorkerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val img: ImageView = itemView.findViewById(R.id.img)
        val degreeTxt: TextView = itemView.findViewById(R.id.degreeTxt)
        val nameTxt: TextView = itemView.findViewById(R.id.nameTxt)
        val specialTxt: TextView = itemView.findViewById(R.id.specialTxt)
        val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBar)
        val scoreTxt: TextView = itemView.findViewById(R.id.scoreTxt)
        val makeBtn: Button = itemView.findViewById(R.id.makeBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.viewholder_listdoctor, parent, false)
        return WorkerViewHolder(view)
    }

    override fun onBindViewHolder(holder: WorkerViewHolder, position: Int) {
        val worker = workerList[position]
        holder.nameTxt.text = worker.name
        holder.degreeTxt.text = worker.degree
        holder.specialTxt.text = worker.specialization
        holder.ratingBar.rating = worker.rating
        holder.scoreTxt.text = worker.rating.toString()
    }

    override fun getItemCount(): Int {
        return workerList.size
    }
}
