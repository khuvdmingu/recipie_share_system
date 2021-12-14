package com.hm.zwap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.hm.zwap.Model.Item;
import com.hm.zwap.utils.Tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdapterListSectioned extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_ITEM = 1;
    private final int VIEW_SECTION = 0;

    private List<Thumbnail> items = new ArrayList<>();
    private ArrayList<Thumbnail> arrayList;
    private Context ctx;
    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, Thumbnail obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public AdapterListSectioned(Context context, List<Thumbnail> items) {
        this.items = items;
        ctx = context;
        arrayList = new ArrayList<Thumbnail>();
        arrayList.addAll(items);
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {
        public ImageView image;
        public TextView name;
        public TextView description;
        public View lyt_parent;

        public OriginalViewHolder(View v) {
            super(v);
            image = (ImageView) v.findViewById(R.id.image);
            name = (TextView) v.findViewById(R.id.name);
            lyt_parent = (View) v.findViewById(R.id.lyt_parent);
            description = (TextView) v.findViewById(R.id.description);
        }
    }

    public static class SectionViewHolder extends RecyclerView.ViewHolder {
        public TextView title_section;

        public SectionViewHolder(View v) {
            super(v);
            title_section = (TextView) v.findViewById(R.id.title_section);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == VIEW_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_people_chat, parent, false);
            vh = new OriginalViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_section, parent, false);
            vh = new SectionViewHolder(v);
        }
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        Thumbnail p = items.get(position);
        if (holder instanceof OriginalViewHolder) {
            OriginalViewHolder view = (OriginalViewHolder) holder;

            view.name.setText(p.name);
            Tools.displayImageRound(ctx, view.image, p.img );
            view.lyt_parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(view, items.get(position), position);
                    }
                }
            });
            view.description.setText(p.description);
        } else {
            SectionViewHolder view = (SectionViewHolder) holder;
            view.title_section.setText(p.name);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return this.items.get(position).section ? VIEW_SECTION : VIEW_ITEM;
    }

    public void insertItem(int index, Thumbnail people){
        items.add(index, people);
        notifyItemInserted(index);
    }
    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        items.clear();
        if (charText.length() == 0) {
            items.addAll(arrayList);
        } else {
            for (Thumbnail data : arrayList) {
                String name = data.name;
                String description = data.description;
                if (name.toLowerCase().contains(charText) || description.toLowerCase().contains(charText)) {
                    items.add(data);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void resetItems(List<Thumbnail> items){
        this.items = items;
        notifyDataSetChanged();
    }

    public List<Thumbnail> getItems(){
        return this.items;
    }

}