package com.sharesmile.share.events;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sharesmile.share.R;
import com.sharesmile.share.events.models.Event;
import com.sharesmile.share.utils.ShareImageLoader;

import java.util.Calendar;
import java.util.Locale;

/**
 * Created by ankitmaheshwari1 on 27/12/15.
 */
public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventViewHolder> {

    private static final String TAG = "EventsAdapter";

    private Context activityContext;
    private EventsDataStore dataStore;

    public EventsAdapter(Context activityContext, EventsDataStore dataStore){
        this.activityContext = activityContext;
        this.dataStore = dataStore;
    }

    @Override
    public EventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.events_card, parent, false);
        // set the view's size, margins, paddings and layout parameters

        EventViewHolder vh = new EventViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(EventViewHolder holder, int position) {

        Event event = dataStore.get(position);

        holder.titleView.setText(event.getName());
        holder.locationView.setText(event.getLocality());
        holder.organiser.setText(event.getOrganiser());

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Integer.parseInt(event.getDate())*1000);

        String month = calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        holder.month.setText(month);
        holder.day.setText(String.valueOf(day));

        ShareImageLoader.getInstance().loadImage(event.getImageUrl(), holder.imageView);
    }

    @Override
    public int getItemCount() {
        return dataStore.getTotalEvents() ;
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {

        public ImageView imageView;
        public TextView titleView;
        public TextView locationView;
        public TextView organiser;
        public TextView day;
        public TextView month;

        public CardView itemView;

        // Provides access to the children views of Events card
        public EventViewHolder(View itemView) {
            super(itemView);
            this.itemView = (CardView) itemView;
            this.imageView = (ImageView) itemView.findViewById(R.id.iv_event_image);
            this.titleView = (TextView) itemView.findViewById(R.id.tv_event_card_name);
            this.locationView = (TextView) itemView.findViewById(R.id.tv_event_card_location);
            this.organiser = (TextView) itemView.findViewById(R.id.tv_event_card_organiser);
            this.day = (TextView) itemView.findViewById(R.id.tv_event_card_date);
            this.month = (TextView) itemView.findViewById(R.id.tv_event_card_month);
        }
    }
}
