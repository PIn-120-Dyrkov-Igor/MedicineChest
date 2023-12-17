package com.example.medicinechest;

import android.graphics.Color;
import android.view.View;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class MyViewBinder implements SimpleAdapter.ViewBinder {

    @Override
    public boolean setViewValue(View view, Object data, String textRepresentation) {
        if (view.getId() == R.id.textView3) {
            String isUse = (String) data;

            //Устанавливаем цвет текста в зависимости от значения is_use
            if ("Не добавлен в аптечку".equals(isUse)) {
                ((TextView) view).setTextColor(Color.RED);
            } else if ("Добавлен в аптечку".equals(isUse)) {
                ((TextView) view).setTextColor(Color.GREEN);
            }
            return false;
        }
        return false;
    }
}
