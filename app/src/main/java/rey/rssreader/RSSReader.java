package rey.rssreader;


import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ProgressBar;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class RSSReader extends AsyncTask<Void, Void, Void>
{
    private String TAG = "RSSLog";

    private WeakReference<Context> context;
    private WeakReference<RecyclerView> recyclerView;
    private WeakReference<ProgressBar> bar;

    private ArrayList<RSSItem> RSSItems;
    private int display_width;
    private String address;
    private RecyclerViewAdapter adapter;

    /**
     * @param context       Вызываемая Activity
     * @param recyclerView  Элемент для показа содержимого
     * @param display_width Ширина дисплея
     */
    RSSReader(Context context, RecyclerView recyclerView, int display_width)
    {
        this.context = new WeakReference<>(context);
        this.recyclerView = new WeakReference<>(recyclerView);

        this.display_width = display_width;
    }

    void ClearAndStop()
    {
        if (RSSItems != null)
        {
            RSSItems.clear();
        }
        recyclerView.get().setAdapter(adapter);
        cancel(true);
    }

    /**
     * @param address Адресс RSS фида
     */
    void setAddress(String address)
    {
        this.address = address;
    }

    void setProgressBar(ProgressBar bar)
    {
        this.bar = new WeakReference<>(bar);
        this.bar.get().setVisibility(ProgressBar.VISIBLE);
        this.bar.get().setProgress(0);
    }

    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Void aVoid)
    {
        super.onPostExecute(aVoid);
        if (context != null)
        {
            adapter = new RecyclerViewAdapter(context.get(), RSSItems, display_width);
            if (recyclerView != null)
            {
                recyclerView.get().setLayoutManager(new LinearLayoutManager(context.get()));
                recyclerView.get().setAdapter(adapter);
            }
        }
    }

    @Override
    protected Void doInBackground(Void... voids)
    {
        ParseXml(GetData(address));
        bar.get().setVisibility(ProgressBar.INVISIBLE);
        return null;
    }

    private void ParseXml(Document data)
    {
        if (data != null)
        {

            Log.d(TAG, "Parsing...");
            RSSItems = new ArrayList<>();
            Element element_root = data.getDocumentElement();
            Node node_channel = element_root.getElementsByTagName("channel").item(0);
            NodeList nl_items = node_channel.getChildNodes();

            Log.d(TAG, "Length node in Channel: " + nl_items.getLength());
            for (int i = 0; i < nl_items.getLength(); i++)
//            for (int i = 0; i < 21; i++)
            {
                Node currentItem = nl_items.item(i);
                if (currentItem.getNodeName().equalsIgnoreCase("item"))
                {
                    Log.d(TAG, "* Item");
                    RSSItem RSSItem = new RSSItem();
                    NodeList nl_subItems = currentItem.getChildNodes();

                    for (int j = 0; j < nl_subItems.getLength(); j++)
                    {
                        Node note_curSubItem = nl_subItems.item(j);
//                        Log.d(TAG,"Node name:"+note_curSubItem.getNodeName());
                        if (note_curSubItem.getNodeName().equalsIgnoreCase("title"))
                        {
                            RSSItem.setTitle(note_curSubItem.getTextContent());
                        }
                        else if (note_curSubItem.getNodeName().equalsIgnoreCase("link"))
                        {
                            RSSItem.setURL(note_curSubItem.getTextContent());
                        }
                        else if (note_curSubItem.getNodeName().equalsIgnoreCase("pubDate"))
                        {
                            RSSItem.setDate(note_curSubItem.getTextContent());
                        }
                        else if (note_curSubItem.getNodeName().equalsIgnoreCase("media:thumbnail")
                                | note_curSubItem.getNodeName().equalsIgnoreCase("media:content")
                                | note_curSubItem.getNodeName().equalsIgnoreCase("enclosure"))
                        {
                            RSSItem.setImage(note_curSubItem.getAttributes().getNamedItem("url").getTextContent());
                        }
                        else if (note_curSubItem.getNodeName().equalsIgnoreCase("description"))
                        {
                            RSSItem.setAnnotation(note_curSubItem.getTextContent());
                        }
                        else if (note_curSubItem.getNodeName().equalsIgnoreCase("dc:creator"))
                        {

                            RSSItem.setAuthor(note_curSubItem.getTextContent());
                        }
                        else if (note_curSubItem.getNodeName().equalsIgnoreCase("atom:author"))
                        {
                            NodeList nl_subItems_atom = note_curSubItem.getChildNodes();
                            for (int k = 0; k < nl_subItems_atom.getLength(); k++)
                            {
                                Node note_curSubItem_atom = nl_subItems_atom.item(k);
//                                Log.d(TAG, "Node name:" + note_curSubItem_atom.getNodeName());
                                if (note_curSubItem_atom.getNodeName().equalsIgnoreCase("atom:name"))
                                {
                                    RSSItem.setAuthor(note_curSubItem_atom.getTextContent());
                                }

                            }
                        }
                    }
                    RSSItem.fix();
                    RSSItems.add(RSSItem);
                    Log.d(TAG, "Add RSSItem");
                }
            }
        }
    }

    private void SaveStreamToFile(InputStream isstream, String path, String namefile)
    {
        Log.d(TAG, "Save stream to file: " + path + namefile);
        File directory = new File(path);
        if (!directory.exists())
        {
            if (directory.mkdirs())
            {
                Log.d(TAG, "Created dir:" + directory.getName());
            }
            else
            {
                Log.d(TAG, "Error to create dir:" + directory.getName());
            }
        }
        File file = new File(path + namefile);
        try
        {
            OutputStream output = new FileOutputStream(file);
            byte[] buffer = new byte[4 * 1024]; // or other buffer size
            int read;

            while ((read = isstream.read(buffer)) != -1)
            {
                output.write(buffer, 0, read);
            }

            output.flush();
            isstream.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    private Document GetData(String address)
    {
        Log.d(TAG, "GetData()");

        Log.d(TAG, "RSS URL: " + address);

        URL url = null;
        try
        {
            url = new URL(address);
        }
        catch (MalformedURLException e)
        {
            Log.d(TAG, "Error URL!");
            e.printStackTrace();
        }
        InputStream outputStream;
        try
        {
            assert url != null;
            Log.d(TAG, "Try load RSS...");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            InputStream inputStream = connection.getInputStream();

            // Clone stream
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int n;
            while ((n = inputStream.read(buf)) >= 0)
                baos.write(buf, 0, n);
            byte[] content = baos.toByteArray();

            InputStream is1 = new ByteArrayInputStream(content);
            outputStream = new ByteArrayInputStream(content);
            SaveStreamToFile(is1, android.os.Environment.getExternalStorageDirectory().getPath() + "/RSS/", url.getHost() + ".rss");
        }
        catch (Exception e)
        {
            Log.d(TAG, "Error load RSS from URL: " + address);
            try
            {
                Log.d(TAG, "Load from file RSS: " + url.getHost());
                File file = new File(android.os.Environment.getExternalStorageDirectory().getPath() + "/RSS/" + url.getHost() + ".rss");
                outputStream = new FileInputStream(file);
            }
            catch (Exception ex)
            {
                Log.d(TAG, "Error load from file RSS!");
//                Toast toast = Toast.makeText(context, "hi", Toast.LENGTH_SHORT).show();
                ex.getStackTrace();
                return null;
            }
        }
        try
        {
            Log.d(TAG, "Parse RSS to Document!");
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            return builder.parse(outputStream);
        }
        catch (Exception e)
        {
            Log.d(TAG, "Error parse RSS to Document!");
            e.getStackTrace();
            return null;
        }
    }
}
