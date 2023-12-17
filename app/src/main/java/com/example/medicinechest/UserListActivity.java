package com.example.medicinechest;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class UserListActivity extends AppCompatActivity {
    private DbHelper mDBHelper;
    private SQLiteDatabase mDb;

    public UserListActivity() {}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);


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
                int userId = (int)  selectedItem.get("user_id");

                // Создаем Intent для перехода на новый экран
                Intent intent = new Intent(UserListActivity.this, UserDetailActivity.class);

                // Передаем user_id в новый экран
                intent.putExtra("user_id", userId);

                // Запускаем новый экран
                startActivity(intent);
            }
        });

        Button button = findViewById(R.id.buttonAddMedication);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ваш код обработки нажатия на кнопку
                Cursor cursor = mDb.rawQuery("SELECT MAX(user_id) FROM userschedule", null);

                if (cursor.moveToFirst()) {
                    int maxId = cursor.getInt(0) + 1;
                    // Теперь у вас есть максимальное значение id
                    cursor.close();

                    // Вставляем новую запись в таблицу
                    ContentValues values = new ContentValues();
                    values.put("name", "Новый пользователь №" + maxId); // Замените на ваше значение
                    long newRowId = mDb.insert("userschedule", null, values);
                } else {
                    // Таблица пуста или произошла ошибка
                    cursor.close();
                }
                updateList();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        //Обновить данные каждый раз, когда экран становится видимым
        updateList();
    }


    void updateList() {
        //Список клиентов
        ArrayList<HashMap<String, Object>> allMedications = new
                ArrayList<HashMap<String, Object>>();

        // Список параметров конкретного клиента
        HashMap<String, Object> allMedication;

        // Отправляем запрос в БД
        Cursor cursor = mDb.rawQuery("SELECT * FROM userschedule", null);
        cursor.moveToFirst();

        // Пробегаем по всем клиентам
        while (!cursor.isAfterLast()) {
            allMedication = new HashMap<String, Object>();
            allMedication.put("user_id", cursor.getInt(0));
            // Заполняем клиента
            allMedication.put("name", cursor.getString(1));
            // Закидываем клиента в список клиентов
            allMedications.add(allMedication);

            // Переходим к следующему клиенту
            cursor.moveToNext();
        }
        cursor.close();

        // Какие параметры клиента мы будем отображать всоответствующих
        // элементах из разметки adapter_item.xml
        String[] from = { "name"};
        int[] to = { R.id.textView};

        //Создаем адаптер
        SimpleAdapter adapter = new SimpleAdapter(this, allMedications, R.layout.adapter_user, from, to);
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);

    }
}
