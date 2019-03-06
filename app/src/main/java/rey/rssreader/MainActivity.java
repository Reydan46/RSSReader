package rey.rssreader;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class MainActivity extends AppCompatActivity
{
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    RecyclerView rv_items_rss;
    RSSReader rssreader = null;
    MenuItem menu_edit_user_url = null;
    private String TAG = "RSSLog";
    private String address;
    private ProgressBar progressBar;
    private SharedPreferences sPrefs;
    private boolean verify_permissions = false;
    private boolean check_pos_menu = false;
    private boolean menu_created = false;
    private SubMenu subRssMenu;
    private ArrayList<String> rss_names;
    private DisplayMetrics dm;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Находим прогресс бар
        progressBar = findViewById(R.id.pb_loading);

        // Находим RecyclerView и добавляем к нему ItemDecoration
        rv_items_rss = findViewById(R.id.rv_items_rss);
        rv_items_rss.addItemDecoration(new RecyclerViewItemDecoration(0, 15, 15, 15));

        // Проверяем права доступа
        verifyStoragePermissions(MainActivity.this);


        // Получаем данные об экране
        dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        //
        rss_names = new ArrayList<>();
        rss_names.addAll(Arrays.asList(this.getResources().getStringArray(R.array.string_name_rss)));
    }

    private void verifyStoragePermissions(Activity activity)
    {
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        // Если права не даны
        if (permission != PackageManager.PERMISSION_GRANTED)
        {
            Log.d(TAG, "Wait grant permission!");
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
        // Если права уже получены
        else
        {
            // Поднимаем флаг получения прав
            verify_permissions = true;
            Log.d(TAG, "Permission granted!");
            run();
        }
    }

    private void run()
    {
        Log.d(TAG, "run()");
        // Продолжаем только при наличии флага о получения прав
        if (!verify_permissions)
        {
            Log.d(TAG, "Wait to check permissions!");
            return;
        }
        // Продолжаем только при наличии флага о позиции выбранного в меню
        if (!check_pos_menu)
        {
            Log.d(TAG, "Wait to check pos menu!");
            return;
        }

        // Если rssreader уже инициализирован, то очищаем его содержимое
        if (rssreader != null)
        {
            rssreader.ClearAndStop();
        }

        rssreader = new RSSReader(MainActivity.this, rv_items_rss, dm.widthPixels);
        rssreader.setAddress(address);
        rssreader.setProgressBar(progressBar);
        rssreader.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Создаём динамическое меню из rss_names
        // Название подменю
        subRssMenu = menu.addSubMenu(getString(R.string.menu_rss_feed));
        for (int i = 0; i < rss_names.size(); i++)
        {
            // Пункт подменю из rss_names
            subRssMenu.add(Menu.NONE, 1000 + i, Menu.NONE, rss_names.get(i));
        }
        // Пункт подменю "Свой адрес"
        subRssMenu.add(Menu.NONE, 2000, Menu.NONE, getString(R.string.menu_user_rss));
        // Устанавливаем возможность выбора галкой одного из пунктов меню
        subRssMenu.setGroupCheckable(Menu.NONE, true, true);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        if (menu_edit_user_url == null)
        {
            menu_edit_user_url = menu.findItem(R.id.menu_edit_user_url);
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "Permission granted!");
        // При получении прав, поднимаем флаг о получении прав
        verify_permissions = true;
        run();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item)
    {
        // Получаем ID выбранного элемента меню
        int id = item.getItemId();

        switch (id)
        {
            case R.id.menu_about:
                AlertDialog.Builder dialog;
                // Создаём диалог
                dialog = new AlertDialog.Builder(MainActivity.this);
                // Заголовок
                dialog.setTitle(getString(R.string.menu_main_about_title));
                // Сообщение
                dialog.setMessage(getString(R.string.menu_main_about_message));
                // Кнопка "Да"
                dialog.setPositiveButton(getString(R.string.menu_main_about_ok), null);
                // Диалог может быть отменён
                dialog.setCancelable(true);
                // Показываем диалог
                dialog.show();
                return true;
            case R.id.menu_exit:
                // Эмулируем нажатие на HOME, сворачивая приложение
                Intent i = new Intent(Intent.ACTION_MAIN);
                i.addCategory(Intent.CATEGORY_HOME);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                return true;
            case R.id.menu_refresh:
                run();
                return true;
            case R.id.menu_edit_user_url:
                editUserURL();
                return true;
        }

        ArrayList<String> rss_urls = new ArrayList<>(Arrays.asList(this.getResources().getStringArray(R.array.string_urls_rss)));
        for (int i = 0; i < subRssMenu.size(); i++)
        {
            if (id == i + 1000)
            {
                item.setChecked(true);
                sPrefs.edit().putInt("pos_rss", i).apply();
                address = rss_urls.get(i);
                check_pos_menu = true;

                Objects.requireNonNull(getSupportActionBar()).setSubtitle(rss_names.get(i));
                Toast.makeText(MainActivity.this, rss_names.get(i), Toast.LENGTH_LONG).show();
                run();
                menu_edit_user_url.setVisible(false);
                return true;
            }
        }

        if (id == 2000)
        {
            item.setChecked(true);
            sPrefs.edit().putInt("pos_rss", subRssMenu.size() - 1).apply();


            sPrefs = getSharedPreferences("preferences", MODE_PRIVATE);
            address = sPrefs.getString("url_rss", "");

            if (address.equals(""))
            {
                editUserURL();
            }
            else
            {
                getUserURL(address);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void editUserURL()
    {
        final AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(getString(R.string.title_user_rss));
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setPositiveButton("Ок", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                address = input.getText().toString();

                getUserURL(address);
            }
        });
        builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.cancel();
                onOptionsItemSelected(subRssMenu.getItem(0));
            }
        });
        builder.show();
    }

    private void getUserURL(String address)
    {
        try
        {
            URL url = new URL(address);
            sPrefs.edit().putString("url_rss", address).apply();

            check_pos_menu = true;

            Objects.requireNonNull(getSupportActionBar()).setSubtitle(url.getHost());
            Toast.makeText(MainActivity.this, url.getHost(), Toast.LENGTH_LONG).show();
            run();
            menu_edit_user_url.setVisible(true);
        }
        catch (MalformedURLException e)
        {
            AlertDialog.Builder alert;
            // Создаём диалог
            alert = new AlertDialog.Builder(MainActivity.this);
            // Заголовок
            alert.setTitle(getString(R.string.title_error_get_url));
            // Сообщение
            alert.setMessage(getString(R.string.text_error_get_url));
            // Кнопка "Да"
            alert.setPositiveButton(getString(R.string.menu_main_about_ok), new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    onOptionsItemSelected(subRssMenu.getItem(0));
                }
            });
            // Показываем диалог
            alert.show();
            e.printStackTrace();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        super.onPrepareOptionsMenu(menu);
        if (!menu_created)
        {
            sPrefs = getSharedPreferences("preferences", MODE_PRIVATE);
            int pos_rss = sPrefs.getInt("pos_rss", 0);
//            SubMenu subm=menu.findItem(R.id.switch_menu).getSubMenu();

            onOptionsItemSelected(subRssMenu.getItem(pos_rss));
            menu_created = true;
        }
        return true;
    }
}
