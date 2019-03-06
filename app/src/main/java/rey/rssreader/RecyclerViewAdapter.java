package rey.rssreader;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.CustomViewHolder>
{
    private ArrayList<RSSItem> RSSItems;
    private Context context;
    private int display_width;
//    private static final String TAG = "rssLog";

    RecyclerViewAdapter(Context context, ArrayList<RSSItem> RSSItems, int display_width)
    {
        this.context = context;
        this.RSSItems = RSSItems;
        this.display_width = display_width;
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.recyclerview_row_layout, parent, false);
        return new CustomViewHolder(view);
    }

    @SuppressLint({"ClickableViewAccessibility", "SetJavaScriptEnabled", "SetTextI18n"})
    @Override
    public void onBindViewHolder(@NonNull final CustomViewHolder CVHolder, int position)
    {
        final RSSItem currentRSSItem = RSSItems.get(position);

        class ModHTML
        {
            @JavascriptInterface
            public void processHTML(String html)
            {
                Log.d("URL_data_Title",html);
            }
        }
        CVHolder.wv_title.getSettings().setJavaScriptEnabled(true);
        CVHolder.wv_title.addJavascriptInterface(new ModHTML(), "out");
        CVHolder.wv_title.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url)
            {
                CVHolder.wv_title.loadUrl("javascript:window.out.processHTML('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
            }
        });

        CVHolder.wv_title.loadDataWithBaseURL(null, "<b style=\"font-size: 19pt;\">"+currentRSSItem.getTitle()+"</b></body>",
                                              "text/html; charset=utf-8", "UTF-8", null);
        CVHolder.wv_title.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if (event.getAction() == MotionEvent.ACTION_UP)
                {
                    ViewInWV(currentRSSItem);
                }
                return false;
            }
        });

        CVHolder.wv_annotation.loadDataWithBaseURL(null, "<style>" +
                "img,iframe {display: inline; height: auto;max-width: 100%;} " +
                "html {overflow-wrap: break-word; word-wrap: break-word; -webkit-hyphens: auto; -ms-hyphens: auto; -moz-hyphens: auto; hyphens: auto;}" +
                "</style>"
                + currentRSSItem.getAnnotation(), "text/html; charset=utf-8", "UTF-8", null);
        CVHolder.wv_annotation.getSettings().setJavaScriptEnabled(true);
        CVHolder.wv_annotation.getSettings().setDomStorageEnabled(true);
//        CVHolder.wv_annotation.setOnTouchListener(new View.OnTouchListener()
//        {
//            @Override
//            public boolean onTouch(View v, MotionEvent event)
//            {
//                if (event.getAction() == MotionEvent.ACTION_UP)
//                {
//                    ViewInWV(currentRSSItem);
//                }
//                return false;
//            }
//        });

        CVHolder.tv_date_and_author.setText(currentRSSItem.getDate()+((currentRSSItem.getAuthor() != null) ? " / "+ currentRSSItem.getAuthor() : ""));

        Picasso picasso = Picasso.get();
//        picasso.setIndicatorsEnabled(true);
        picasso.load(currentRSSItem.getImage()).resize(display_width, 0).into(CVHolder.iv_img);

        CVHolder.cv_row.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                ViewInWV(currentRSSItem);
            }
        });
    }

    private void ViewInWV(RSSItem currentRSSItem)
    {
        Intent intent = new Intent(context, WebViewActivity.class);
        intent.putExtra("URL", currentRSSItem.getURL());
        intent.putExtra("Title", currentRSSItem.getTitle());
        intent.putExtra("Date", currentRSSItem.getDate());
        intent.putExtra("Image", currentRSSItem.getImage());
        context.startActivity(intent);
    }


    @Override
    public int getItemCount()
    {
        if (RSSItems != null)
        {
            return RSSItems.size();
        }
        else
        {
            return 0;
        }
    }

    class CustomViewHolder extends RecyclerView.ViewHolder
    {
        WebView wv_annotation;
        WebView wv_title;
        TextView tv_date_and_author;
        TextView tv_link;
        ImageView iv_img;
        CardView cv_row;

        CustomViewHolder(View itemView)
        {
            super(itemView);
            wv_title = itemView.findViewById(R.id.wv_title);
            wv_annotation = itemView.findViewById(R.id.wv_annotation);
            tv_date_and_author = itemView.findViewById(R.id.tv_date_and_author);
            tv_link = itemView.findViewById(R.id.tv_link);
            iv_img = itemView.findViewById(R.id.iv_img);
            cv_row = itemView.findViewById(R.id.cv_row);
        }
    }
}
