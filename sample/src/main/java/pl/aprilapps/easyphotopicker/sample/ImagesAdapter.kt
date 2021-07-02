package pl.aprilapps.easyphotopicker.sample

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import pl.aprilapps.easyphotopicker.MediaFile

/**
 * Created by Jacek Kwiecień on 08.11.2016.
 */
class ImagesAdapter(
    private val imagesFiles: List<MediaFile>
) : RecyclerView.Adapter<ImagesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(inflater.inflate(R.layout.view_image, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Picasso.get()
            .load(imagesFiles[position].file)
            .fit()
            .centerCrop()
            .into(holder.imageView)
    }

    override fun getItemCount(): Int = imagesFiles.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.image_view)
    }
}