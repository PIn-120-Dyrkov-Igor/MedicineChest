package com.example.medicinechest;

import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class MyListActivity extends AppCompatActivity {
    private DbHelper mDBHelper;
    private SQLiteDatabase mDb;
    private String findText = "";


    public MyListActivity() {}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_list);


        mDBHelper = new DbHelper(this);
        try {
            mDBHelper.updateDataBase();
        } catch (IOException mIOException) {
            throw new Error("UnableToUpdateDatabase");
        }
        try {
            mDb = mDBHelper.getWritableDatabase();
        } catch (SQLException mSQLException) {
            throw mSQLException;
        }

        updateList();

        // Получаем ссылку на ListView
        ListView listView = findViewById(R.id.listView);

        //Устанавливаем слушатель событий на ListView
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Получаем данные о выбранном элементе
                HashMap<String, Object> selectedItem = (HashMap<String, Object>) parent.getItemAtPosition(position);
                int userId = (int) selectedItem.get("_id"); // Предполагается, что "name" это user_id

                // Создаем Intent для перехода на новый экран (замените на ваш класс и экран)
                Intent intent = new Intent(MyListActivity.this, MedicationInfoActivity.class);

                // Передаем user_id в новый экран
                intent.putExtra("_id", userId);

                // Запускаем новый экран
                startActivity(intent);
            }
        });

        EditText editText = findViewById(R.id.editText);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable editable) {
                findText = editText.getText().toString();
                updateList();
            }
        });
    }

    void updateList() {
        //Список клиентов
        ArrayList<HashMap<String, Object>> allMedications = new
                ArrayList<HashMap<String, Object>>();

        // Список параметров конкретного клиента
        HashMap<String, Object> allMedication;

        // Отправляем запрос в БД
        Cursor cursor = mDb.rawQuery("SELECT * FROM allmedication WHERE isactive = 1", null);
        if(!findText.isEmpty()){
            cursor = mDb.rawQuery("SELECT * FROM allmedication WHERE isactive = 1 AND name LIKE '%" + findText + "%';\n", null);
        }
        cursor.moveToFirst();

        // Пробегаем по всем клиентам
        while (!cursor.isAfterLast()) {
            allMedication = new HashMap<String, Object>();
            allMedication.put("_id", cursor.getInt(0));
            // Заполняем клиента
            allMedication.put("name", cursor.getString(1));
            allMedication.put("form", cursor.getString(2));
            String is_use = cursor.getString(5);
            if(is_use == null) is_use = "0";
            if(Integer.parseInt(is_use) == 0){
                is_use = (String) "Не используется";
            }else is_use = (String) "Используется";

            /*allMedication.put("isactive", cursor.getString(5));*/
            allMedication.put("isactive", is_use);
            // Закидываем клиента в список клиентов
            allMedications.add(allMedication);

            // Переходим к следующему клиенту
            cursor.moveToNext();
        }
        cursor.close();

        // Какие параметры клиента мы будем отображать всоответствующих
        // элементах из разметки adapter_item.xml
        String[] from = { "name", "form", "isactive"};
        int[] to = { R.id.textView, R.id.textView2, R.id.textView3};

        //Создаем адаптер
        SimpleAdapter adapter = new SimpleAdapter(this, allMedications, R.layout.adapter_my_list, from, to);
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);

    }

}
