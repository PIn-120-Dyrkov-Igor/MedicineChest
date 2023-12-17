package com.example.medicinechest;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class MedicationListActivity extends AppCompatActivity {

    private DbHelper mDBHelper;
    private SQLiteDatabase mDb;

    private int userId;

    private int itemPosition = 0;
    private String findText = "";

    public MedicationListActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medication_list);


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

        //Получаем ссылку на ListView
        ListView listView = findViewById(R.id.listView);
        //Устанавливаем слушатель событий на ListView
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Получаем данные о выбранном элементе
                HashMap<String, Object> selectedItem = (HashMap<String, Object>) parent.getItemAtPosition(position);
                // Получаем текущее значение isactive
                String currentIsActive =  selectedItem.get("isactive").toString();
                // Инвертируем значение isactive
                int newIsActive = (currentIsActive == "Не добавлен в аптечку") ? 1 : 0;//Добавлен в аптечку
                // Получаем Id
                userId = (int) selectedItem.get("_id");
                // Обновляем значение isactive в базе данных
                updateIsActiveInDatabase(newIsActive, userId);
                // Обновляем список
                updateList();
                // Отображаем Toast об изменении
                String currentName =  selectedItem.get("name").toString();
                String toastMessage = (newIsActive == 1) ? " - добавлен в аптечку" : " - удален из аптечки";

                Toast.makeText(getApplicationContext(), currentName + toastMessage, Toast.LENGTH_SHORT).show();
            }
        });

        Spinner spinner = findViewById(R.id.spinner);
        //Создаем ArrayAdapter с данными для выпадающего списка
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.medication_types,
                android.R.layout.simple_spinner_item
        );

        //Устанавливаем макет для выпадающего списка
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Устанавливаем адаптер для выпадающего списка
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                //Вызывается при выборе нового элемента
                itemPosition  = position;
                updateList();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
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
        //Список медикаментов
        ArrayList<HashMap<String, Object>> allMedications = new
                ArrayList<HashMap<String, Object>>();

        //Список параметров конкретного медикамента
        HashMap<String, Object> allMedication;

        Cursor cursor = mDb.rawQuery("SELECT * FROM allmedication", null);
        //Отправляем запрос в БД
        if(itemPosition == 0){
            if(findText.isEmpty()){
                cursor = mDb.rawQuery("SELECT * FROM allmedication", null);
            }else {
                cursor = mDb.rawQuery("SELECT * FROM allmedication WHERE name LIKE '%" + findText + "%'", null);
            }

        }else if(itemPosition == 1){
            if(findText.isEmpty()){
                cursor = mDb.rawQuery("SELECT * FROM allmedication ORDER BY isactive DESC", null);
            }else {
                cursor = mDb.rawQuery("SELECT * FROM allmedication WHERE name LIKE '%" + findText + "%' ORDER BY isactive DESC", null);
            }

        }else if(itemPosition == 2){
            if(findText.isEmpty()){
                cursor = mDb.rawQuery("SELECT * FROM allmedication ORDER BY isactive", null);
            }else {
                cursor = mDb.rawQuery("SELECT * FROM allmedication WHERE name LIKE '%" + findText + "%' ORDER BY isactive", null);
            }

        }
        cursor.moveToFirst();

        //Пробегаем по всем медикаментам
        while (!cursor.isAfterLast()) {
            allMedication = new HashMap<String, Object>();
            allMedication.put("_id", cursor.getInt(0));
            // Заполняем медикамент
            allMedication.put("name", cursor.getString(1));
            allMedication.put("form", cursor.getString(2));
            String is_use = cursor.getString(5);
            if(is_use == null) is_use = "0";
            if(Integer.parseInt(is_use) == 0){
                is_use = (String) "Не добавлен в аптечку";
            }else is_use = (String) "Добавлен в аптечку";

            allMedication.put("isactive", is_use);
            //Закидываем медикамент в список медикаментов
            allMedications.add(allMedication);

            //Переходим к следующему медикаменту
            cursor.moveToNext();
        }
        cursor.close();

        //Какие параметры медикаментов мы будем отображать всоответствующих
        //элементах из разметки adapter_item.xml
        String[] from = { "name", "form", "isactive"};
        int[] to = { R.id.textView, R.id.textView2, R.id.textView3};

        //Создаем адаптер
        SimpleAdapter adapter = new SimpleAdapter(this, allMedications, R.layout.adapter_list, from, to);
        adapter.setViewBinder(new MyViewBinder());

        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);

    }

    private void updateIsActiveInDatabase(int newIsActive, int userId) {
        //Выполните SQL-запрос для обновления "isactive" в базе данных
        String updateQuery = "UPDATE allmedication SET isactive = " + newIsActive + " WHERE _id = " + userId;
        mDb.execSQL(updateQuery);
    }


}