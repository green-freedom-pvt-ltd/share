package com.sharesmile.share.helpcenter.category;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sharesmile.share.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ankitmaheshwari on 8/26/17.
 */

public class FeedbackCategoryAdapter extends RecyclerView.Adapter<FeedbackCategoryAdapter.CategoryViewHolder>{

    private ItemClickListener mListener;
    private List<FeedbackCategory> mCategoriesList;

    public FeedbackCategoryAdapter(List<FeedbackCategory> categories, ItemClickListener listener){
        this.mCategoriesList = categories;
        this.mListener = listener;
    }

    @Override
    public CategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.feeedback_category_list_item, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CategoryViewHolder holder, int position) {
        holder.bindData(mCategoriesList.get(position));
    }

    @Override
    public int getItemCount() {
        return mCategoriesList != null ? mCategoriesList.size() : 0;
    }


    public class CategoryViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tv_category_label)
        TextView categoryLabel;

        @BindView(R.id.container_feedback_list_item)
        View container;

        public CategoryViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bindData(final FeedbackCategory category){
            categoryLabel.setText(category.getLabel());
            container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null){
                        mListener.onItemClick(category);
                    }
                }
            });
        }
    }

    public interface ItemClickListener {
        void onItemClick(FeedbackCategory category);
    }
}
