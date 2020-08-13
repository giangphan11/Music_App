package phanbagiang.com.musicapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import phanbagiang.com.musicapp.R;
import phanbagiang.com.musicapp.activities.PlayerActivity;
import phanbagiang.com.musicapp.model.MusicFile;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.ViewHolder> {
    private Context mContext;
    private List<MusicFile>mData;

    public MusicAdapter(Context mContext, List<MusicFile> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(this.mContext).inflate(R.layout.music_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.txtTitle.setText(mData.get(position).getTitle());
        holder.txtArtist.setText(mData.get(position).getArtist());
        byte []art=getAlbumArt(mData.get(position).getPath());
        if(art!=null){
            Glide.with(this.mContext)
                    .load(art)
                    .into(holder.imgAlbum);
        }
        else{
            Glide.with(this.mContext)
                    .load(R.drawable.icon0)
                    .into(holder.imgAlbum);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(mContext, PlayerActivity.class);
                intent.putExtra("position",position);
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView imgAlbum;
        TextView txtTitle, txtArtist;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAlbum=itemView.findViewById(R.id.img_album);
            txtTitle=itemView.findViewById(R.id.txtTitle);
            txtArtist=itemView.findViewById(R.id.txtArtist);
        }
    }

    //convert String to
    private byte [] getAlbumArt(String path){
        MediaMetadataRetriever retriever=new MediaMetadataRetriever();
        retriever.setDataSource(path);
        byte []art=retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }
}
