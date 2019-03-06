package rey.rssreader;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.*;

public class WebViewActivity extends AppCompatActivity
{
    WebView wv_page;
    private Bundle bundle;

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.post_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        switch (id)
        {
            case android.R.id.home:
                finish();
                return true;
            case R.id.menu_share:
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String body_text = bundle.getString("Title") + " " + bundle.getString("URL");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, body_text);
                startActivity(Intent.createChooser(sharingIntent, "Поделиться"));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        wv_page = findViewById(R.id.wv_page);
        wv_page.getSettings().setJavaScriptEnabled(true);
        wv_page.getSettings().setDomStorageEnabled(true);
        wv_page.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        wv_page.setWebChromeClient(new WebChromeClient());

        wv_page.setWebViewClient(new WebViewClient()
        {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request)
            {
                return super.shouldOverrideUrlLoading(view, request);
            }
        });

        bundle = getIntent().getExtras();
        if (bundle != null)
        {
            if (getSupportActionBar() != null)
            {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle(bundle.getString("Title"));
                getSupportActionBar().setSubtitle(bundle.getString("Date"));
            }
            wv_page.loadUrl(bundle.getString("URL"));
        }
    }

}
