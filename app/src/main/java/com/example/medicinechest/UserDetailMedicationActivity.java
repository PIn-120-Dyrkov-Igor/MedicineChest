package com.example.medicinechest;

import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;


public class UserDetailMedicationActivity extends AppCompatActivity {

    private DbHelper mDBHelper;
    private SQLiteDatabase mDb;
    private String findText = "";
    private int use_id;
    private int med_id;
    private String time;
    private int medcount;
    private int timecount;

    private String medName;
    private String medForm;


    private EditText editViewTimeUse;
    private TimePickerDialog timePickerDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail_medication);

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
        int useId = intent.getIntExtra("use_id", -1);
        use_id = useId;

        //Получаем данные из БД по выбранному Id
        Cursor cursor = mDb.rawQuery("SELECT * FROM timeuse WHERE use_id = " + use_id + "", null);
        cursor.moveToFirst();

        //Пробегаем по всем клиентам
        while (!cursor.isAfterLast()) {

            // Заполняем клиента
            med_id = (cursor.getInt(2));
            time = (cursor.getString(3));
            medcount = (cursor.getInt(4));
            timecount = (cursor.getInt(5));

            //Переходим к следующему клиенту
            cursor.moveToNext();
        }
        cursor.close();
        updateList();

        cursor = mDb.rawQuery("SELECT * FROM allmedication WHERE _id = " + med_id + "", null);
        cursor.moveToFirst();

        //Пробегаем по всем клиентам
        while (!cursor.isAfterLast()) {

            // Заполняем клиента
            medName = (cursor.getString(1));
            medForm = (cursor.getString(2));
            //Переходим к следующему клиенту
            cursor.moveToNext();
        }
        cursor.close();

        TextView TitleMed = findViewById(R.id.textViewTitleMed);
        TitleMed.setText(String.valueOf("Текущий препарат: " + medName + " (" + medForm + ")" ));

        EditText CountMed = findViewById(R.id.editViewCountMed);
        CountMed.setText(String.valueOf(medcount));

        EditText CountUse = findViewById(R.id.editViewCountUse);
        CountUse.setText(String.valueOf(timecount));

        EditText TimeUse = findViewById(R.id.editViewTimeUse);
        TimeUse.setText(String.valueOf(time));



        editViewTimeUse = findViewById(R.id.editViewTimeUse);
        // Инициализация TimePickerDialog
        initTimePickerDialog();

        // Обработчик нажатия для вызова TimePickerDialog
        editViewTimeUse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timePickerDialog.show();
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
                int medId = (int) selectedItem.get("_id");
                med_id = medId;
                //Получаем данные из БД по выбранному Id
                Cursor cursor = mDb.rawQuery("SELECT * FROM allmedication WHERE _id = " + med_id + "", null);
                cursor.moveToFirst();

                //Пробегаем по всем клиентам
                while (!cursor.isAfterLast()) {

                    // Заполняем клиента
                    medName = (cursor.getString(1));
                    medForm = (cursor.getString(2));

                    //Переходим к следующему клиенту
                    cursor.moveToNext();
                }
                cursor.close();
                TitleMed.setText(String.valueOf("Текущий препарат: " + medName + " (" + medForm + ")" ));

            }
        });

        EditText editText = findViewById(R.id.editViewFindMed);
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

        //Удаление текущего пользователя
        Button deleteUse = findViewById(R.id.delButton);
        deleteUse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Создаем SnackBar
                Snackbar snackbar = Snackbar.make(v, "Удалить текущую запись?", Snackbar.LENGTH_LONG)
                        .setAction("Да", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                int deletedRows = mDb.delete("timeuse", "use_id=?", new String[]{String.valueOf(useId)});

                                if (deletedRows > 0) {
                                    finish(); //Если нужно закрыть текущую активность после перехода
                                    Toast.makeText(getApplicationContext(), "Запись удалена!", Toast.LENGTH_SHORT).show();
                                } else {
                                    //Обработка ошибки удаления, например, показ сообщения
                                    Toast.makeText(getApplicationContext(), "Ошибка при удалении", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                // Показываем SnackBar
                snackbar.show();
            }
        });

        //Сохранение текущего записи
        Button saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int maxUse = 0;
                //Получаем данные из БД по выбранному Id
                Cursor cursor = mDb.rawQuery("SELECT * FROM allmedication WHERE _id = " + med_id + "", null);
                cursor.moveToFirst();

                //Пробегаем по всем клиентам
                while (!cursor.isAfterLast()) {
                    maxUse = (cursor.getInt(6));

                    //Переходим к следующему клиенту
                    cursor.moveToNext();
                }
                cursor.close();
                int count = Integer.parseInt(CountMed.getText().toString());
                int use = Integer.parseInt(CountUse.getText().toString());
                if(count*use <= maxUse){
                    // Создаем объект ContentValues для хранения новых данных
                    ContentValues values = new ContentValues();
                    values.put("med_id", med_id);
                    values.put("time", TimeUse.getText().toString());
                    values.put("medcount", Integer.parseInt(CountMed.getText().toString()));
                    values.put("timecount", Integer.parseInt(CountUse.getText().toString()));

                    // Выполняем обновление записи в базе данных
                    mDb.update("timeuse", values, "use_id=?", new String[]{String.valueOf(useId)});

                    Toast.makeText(getApplicationContext(), "Данные изменены!", Toast.LENGTH_SHORT).show();

                    finish();
                }else {
                    Toast.makeText(getApplicationContext(), "Требуемое количество меньше фактичечкого", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void initTimePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        // Создание TimePickerDialog
        timePickerDialog = new TimePickerDialog(
                this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        //Обработка выбора времени
                        String selectedTime = String.format("%02d:%02d", hourOfDay, minute);
                        editViewTimeUse.setText(selectedTime);
                    }
                },
                hour,
                minute,
                true //true, если использовать 24-часовой формат времени
        );
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

            allMedication.put("isactive", cursor.getString(5));
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
