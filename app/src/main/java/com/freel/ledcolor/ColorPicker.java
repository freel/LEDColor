package com.freel.ledcolor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;


public class ColorPicker extends ActionBarActivity implements ColorRing.MyCallback {
    /**
     * Отображение отправляемой команды(рабочее поле)
     */
    private TextView textView;

    /**
     * Цветовое кольцо
     */
    private ColorRing ring;

    /**
     * Сохраненные настройки приложения
     */
    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_picker);
        ring = (ColorRing) findViewById(R.id.ring);
        // Инициализация обратного вызова из класса
        ring.registerCallBack(this);

        textView = (TextView)findViewById(R.id.urlView);

        sp = PreferenceManager.getDefaultSharedPreferences(this);

    }

    /**
     * Обработка обратного вызова
     */
    @Override
    public void callBackReturn() {
        /**
         * Формирование url с командой, в зависимости от контекста
         * ring.url в ColorRing см. initCallback()
         */
        String url = "http://" + sp.getString("ledServer", "192.168.1.1") + "/" + ring.url;
        //отображение
        textView.setText(url);
        //выполняется запрос
        HttpAsyncTask httpAsyncTask = new HttpAsyncTask();
        httpAsyncTask.execute(url);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Добавляет элементы в меню
        getMenuInflater().inflate(R.menu.menu_color_picker, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Добавляет обработку при нажатии на элемент меню
        int id = item.getItemId();

        // Открывает активити настроек
        if (id == R.id.action_settings) {
            Intent settingsActivity = new Intent(getBaseContext(),
                    PrefActivity.class);
            startActivity(settingsActivity);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Класс для работы с асинхронной отправкой данных
     */
    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        public String doInBackground(String... params) {
            String inputLine = null;
            try {
                URL url = new URL(params[0]);
                URLConnection conn = url.openConnection();

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                while ((inputLine = br.readLine()) != null) {
                    return inputLine;
                }
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return inputLine;
        }

        @Override
        protected void onPostExecute(String result) {
        }
    }

}
