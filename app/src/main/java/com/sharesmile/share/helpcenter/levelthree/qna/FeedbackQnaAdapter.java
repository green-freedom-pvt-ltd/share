package com.sharesmile.share.helpcenter.levelthree.qna;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sharesmile.share.R;
import com.sharesmile.share.core.Logger;
import com.sharesmile.share.helpcenter.levelthree.qna.model.Qna;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ankitmaheshwari on 8/29/17.
 */

public class FeedbackQnaAdapter extends RecyclerView.Adapter<FeedbackQnaAdapter.FeedbackQnaViewHolder>{

    private static final String TAG = "FeedbackQnaAdapter";

    private List<Qna> qnaList;

    @Override
    public FeedbackQnaViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.feedback_qna_item, parent, false);
        return new FeedbackQnaViewHolder(view);
    }

    public void setData(List<Qna> data) {
        this.qnaList = data;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(FeedbackQnaViewHolder holder, int position) {
        holder.bindData(qnaList.get(position));
    }

    @Override
    public int getItemCount() {
        return qnaList != null ? qnaList.size() : 0;
    }

    public class FeedbackQnaViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.tv_feedback_question)
        TextView question;

        @BindView(R.id.tv_feedback_answer)
        TextView answer;

        public FeedbackQnaViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bindData(Qna qna){
            Logger.d(TAG, "bindData: " + qna.getQuestion());
            question.setText(qna.getQuestion());
            answer.setText(qna.getAnswer());
        }
    }

}
