package adapters

import Models.FaqList
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sharesmile.share.R
import com.sharesmile.share.core.IFragmentController
import kotlinx.android.synthetic.main.faq_list_item.view.*
import java.util.*

/**
 * Created by Shine on 21/07/16.
 */
class FaqAdapter(controller: IFragmentController) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    public var dataList: ArrayList<FaqList.Faq>? = null
        set(value) {
            dataList = value
            notifyDataSetChanged()
        }

    private var ITEM_FAQ_QUESTION = 1;
    private var ITEM_FAQ_FEEDBACK = 2;


    private var fragmentController: IFragmentController

    init {
        fragmentController = controller
    }

    override fun getItemCount(): Int {
        return (dataList?.size ?: -1) + 1;
    }

    override fun getItemViewType(position: Int): Int {
        return if (position < dataList?.size ?: 0) ITEM_FAQ_QUESTION else ITEM_FAQ_FEEDBACK

    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder? {

        when (viewType) {
            ITEM_FAQ_QUESTION -> {
                return FaqHolder(LayoutInflater.from(parent?.context).inflate(R.layout.faq_list_item, parent, false));
            }
            ITEM_FAQ_FEEDBACK -> {
                return FaqUserFeedbackHolder(LayoutInflater.from(parent?.context).inflate(R.layout.faq_item_user_input, parent, false))
            }
            else -> {
                //not possible
                return null;
            }
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {


        when (holder) {
            is FaqHolder -> holder.bindData(dataList!!.get(position))

            is FaqUserFeedbackHolder -> holder.bindData()
            else -> {
                //not possible

            }
        }

    }

    inner class FaqHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {

        public fun bindData(data: FaqList.Faq) {

            itemView.qa.text = data.question;
            itemView.answer.text = data.answer;
        }
    }

    inner class FaqUserFeedbackHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {


        fun bindData() {

            /*   itemView.submit.setOnClickListener(View.OnClickListener {
                   if (TextUtils.isEmpty(itemView.user_response.text)) {
                       Toast.makeText(itemView.context, "Please enter question", Toast.LENGTH_SHORT).show()
                       return@OnClickListener
                   }

                   var requestObject: JSONObject = JSONObject();
                   requestObject.put("question", itemView.user_response.text);
                   fragmentController.replaceFragment(OnScreenFragment(), true)

                   if (MainApplication.isLogin()) {
                       requestObject.put("user_id", MainApplication.getInstance().userID);

                   }

                   NetworkDataProvider.doPostCallAsync(Urls.getFaqUrl(), requestObject, object : NetworkAsyncCallback<CustomJSONObject>() {
                       override fun onNetworkFailure(ne: NetworkException?) {
                       }

                       override fun onNetworkSuccess(wrapper: CustomJSONObject?) {
                       }

                   })
               })*/
        }
    }
}