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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class UserDetailActivity extends AppCompatActivity {

    private DbHelper mDBHelper;
    private SQLiteDatabase mDb;
    private int user_id;
    private String name;

    private int use_id;
    private int med_id;
    private String time;
    private int medcount;
    private int timecount;

    private String medName;
    private String medForm;



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);

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

        // Получаем Intent, который запустил этот экран
        Intent intent = getIntent();

        // Получаем идентификатор пользователя из Intent
        int userId = intent.getIntExtra("user_id", -1);
        user_id = userId;

        //Получаем данные из БД по выбранному Id
        Cursor cursor = mDb.rawQuery("SELECT * FROM userschedule WHERE user_id = " + userId + "", null);
        cursor.moveToFirst();

        //Пробегаем по всем клиентам
        while (!cursor.isAfterLast()) {
            name = (cursor.getString(1));
            cursor.moveToNext();
        }
        cursor.close();
        updateList();

        //Выводим идентификатор пользователя на экран
        EditText userIdTextView = findViewById(R.id.editViewTitle);
        userIdTextView.setText(name);

        //Сохранение изменения имени
        Button saveName = findViewById(R.id.saveButton);
        saveName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!name.equals(userIdTextView.getText().toString())){
                    // Создаем объект ContentValues для хранения новых данных
                    ContentValues values = new ContentValues();
                    values.put("name", userIdTextView.getText().toString());

                    // Выполняем обновление записи в базе данных
                    mDb.update("userschedule", values, "user_id=?", new String[]{String.valueOf(user_id)});


                    Toast.makeText(getApplicationContext(), "Данные изменены!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Удаление текущего пользователя
        Button deleteUser = findViewById(R.id.deleteButton);
        deleteUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Создаем SnackBar
                Snackbar snackbar = Snackbar.make(v, "Удалить текущего пользователя?", Snackbar.LENGTH_LONG)
                        .setAction("Да", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                int deletedRows = mDb.delete("userschedule", "user_id=?", new String[]{String.valueOf(userId)});

                                if (deletedRows > 0) {
                                    finish(); //Если нужно закрыть текущую активность после перехода
                                    Toast.makeText(getApplicationContext(), "Пользователь " + name + " удален!", Toast.LENGTH_SHORT).show();
                                } else {
                                    //Обработка ошибки удаления, например, показ сообщения
                                    Toast.makeText(getApplicationContext(), "Ошибка при удалении пользователя", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                // Показываем SnackBar
                snackbar.show();
            }
        });

        //Добавление нового приема
        Button addRecordButton = findViewById(R.id.addRecordButton);
        addRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Создаем объект ContentValues для хранения данных
                ContentValues values = new ContentValues();
                values.put("user_id", userId);
                values.put("time", "12:00");
                values.put("med_id", 0);
                values.put("medcount", 0);
                values.put("timecount", 0);

                // Вставляем данные в таблицу
                long newRowId = mDb.insert("timeuse", null, values);

                updateList();
            }
        });

        // Получаем ссылку на ListView
        ListView listView = findViewById(R.id.listView);
        //Устанавливаем слушатель событий на ListView
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Получаем данные о выбранном элементе
                HashMap<String, Object> selectedItem = (HashMap<String, Object>) parent.getItemAtPosition(position);
                int useId = (int)  selectedItem.get("use_id");

                // Создаем Intent для перехода на новый экран
                Intent intent = new Intent(UserDetailActivity.this, UserDetailMedicationActivity.class);

                // Передаем user_id в новый экран
                intent.putExtra("use_id", useId);

                // Запускаем новый экран
                startActivity(intent);
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
        ArrayList<HashMap<String, Object>> allUses = new
                ArrayList<HashMap<String, Object>>();

        // Список параметров конкретного клиента
        HashMap<String, Object> allUse;

        // Отправляем запрос в БД
        Cursor cursor = mDb.rawQuery("SELECT * FROM timeuse WHERE user_id = " + user_id + "", null);
        cursor.moveToFirst();

        //Пробегаем по всем клиентам
        while (!cursor.isAfterLast()) {
            allUse = new HashMap<String, Object>();
            // Заполняем клиента
            allUse.put("use_id", cursor.getInt(0));
            allUse.put("user_id", cursor.getInt(1));
            int medication = cursor.getInt(2);

            if(medication >= 0){
                //Получаем данные из БД по выбранному Id
                Cursor cursor1 = mDb.rawQuery("SELECT * FROM allmedication WHERE _id = " + medication + "", null);
                cursor1.moveToFirst();

                //Пробегаем по всем клиентам
                while (!cursor1.isAfterLast()) {
                    medName = (cursor1.getString(1));
                    medForm = (cursor1.getString(2));
                    cursor1.moveToNext();
                }
                cursor1.close();
            }
            allUse.put("med_Name", medName);
            allUse.put("med_Form", medForm);

            allUse.put("med_id", cursor.getInt(2));
            allUse.put("time", cursor.getString(3));
            allUse.put("medcount", cursor.getInt(4));
            allUse.put("timecount", cursor.getInt(5));

            // Закидываем в список
            allUses.add(allUse);
            //Переходим к следующему клиенту
            cursor.moveToNext();
        }
        cursor.close();

        // Какие параметры клиента мы будем отображать всоответствующих
        // элементах из разметки adapter_item.xml
        String[] from = { "med_Name", "time", "medcount"};
        int[] to = { R.id.textView, R.id.textView2, R.id.textView3};

        //Создаем адаптер
        SimpleAdapter adapter = new SimpleAdapter(this, allUses, R.layout.adapter_user_medication, from, to);
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);
    }
}
