package rey.rssreader;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

class RSSItem
{
    private String url;
    private String date = "";
    private String image;
    private String title;
    private String annotation;
    private String author;
    private String TAG = "RSSLog";

    String getURL()
    {
        return url;
    }

    void setURL(String url)
    {
        this.url = url;
        Log.d(TAG, "URL: " + this.url);
    }

    String getDate()
    {
        return date;
    }

    void setDate(String date)
    {
//                                                            Wed, 06 Feb 2019 13:17:40 +0300
        SimpleDateFormat format = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
        Date d_date = new Date();
        try
        {
            d_date = format.parse(date);
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
        this.date = sdf.format(d_date.getTime());

        Log.d(TAG, "Date: " + this.date);
    }

    String getImage()
    {
        return image;
    }

    void setImage(String image)
    {
        this.image = image;
        Log.d(TAG, "Image: " + this.image);
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
        Log.d(TAG, "Title: " + this.title);
    }

    String getAnnotation()
    {
        return annotation;
    }

    void setAnnotation(String annotation)
    {
        this.annotation = annotation;
        this.annotation = this.annotation.replaceAll("<!--noindex-->", "").replaceAll("<!--/noindex-->", "");
        this.annotation = this.annotation.replaceAll("<a.+?>", "").replaceAll("</a>", "");
//        this.annotation = this.annotation.replaceAll("<iframe.+?iframe>", "(тут должно быть видео)");
        this.annotation = (this.annotation.equals("")) ? "< < < Пусто > > >" : this.annotation;
        Log.d(TAG, "Annotation:" + this.annotation);
    }

    void fix()
    {
        if (this.image != null)
        {
            this.annotation = this.annotation.replaceAll("<img.+?" + this.image.substring(this.image.length() - 20,this.image.length()-4) + ".+?>", "");
            Log.d(TAG, "Find image: " + this.image.substring(this.image.length() - 20));
            Log.d(TAG, "Fix annotation: " + this.annotation);
        }
    }

    String getAuthor()
    {
        return author;
    }

    void setAuthor(String author)
    {
        this.author = author;
        Log.d(TAG, "Author:" + this.author);
    }
}
