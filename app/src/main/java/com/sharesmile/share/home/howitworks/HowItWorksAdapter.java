package com.sharesmile.share.home.howitworks;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sharesmile.share.R;
import com.sharesmile.share.home.howitworks.model.HowItWorksRowItem;
import com.sharesmile.share.core.ShareImageLoader;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ankitmaheshwari on 1/31/18.
 */

public class HowItWorksAdapter extends RecyclerView.Adapter<HowItWorksAdapter.ViewHolder> {

    private static final String TAG = "HowItWorksAdapter";

    private List<HowItWorksRowItem> list;

    public HowItWorksAdapter(List<HowItWorksRowItem> list) {
        this.list = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.rv_how_it_works_row_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        viewHolder.bindData(list.get(i));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.tv_how_it_works_title)
        TextView title;

        @BindView(R.id.iv_how_it_works_illustration)
        ImageView illustration;

        @BindView(R.id.tv_how_it_works_content)
        TextView content;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bindData(HowItWorksRowItem item){
            title.setText(item.getTitle());
            content.setText(item.getContent());
            ShareImageLoader.getInstance().loadImage(item.getImageUrl(), illustration);
        }

    }
}
