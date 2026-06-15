package com.menergy.creditledger;

import android.app.Activity;
import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {

    private EditText etName, etAmount;
    private RadioGroup rgType;
    private TextView tvTotalPay, tvTotalReceive;
    private ListView listView;
    private Button btnSave;

    private final ArrayList<LedgerModel> ledgerList = new ArrayList<>();
    private LedgerAdapter adapter;
    public DatabaseHelper myDB;

    private long totalPay = 0L;
    private long totalReceive = 0L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myDB = new DatabaseHelper(this);

        etName = findViewById(R.id.etName);
        etAmount = findViewById(R.id.etAmount);
        rgType = findViewById(R.id.rgType);
        tvTotalPay = findViewById(R.id.tvTotalPay);
        tvTotalReceive = findViewById(R.id.tvTotalReceive);
        listView = findViewById(R.id.listView);
        btnSave = findViewById(R.id.btnSave);

        adapter = new LedgerAdapter(this, ledgerList);
        listView.setAdapter(adapter);

        loadDataFromDatabase();

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveLedger();
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                confirmDelete(ledgerList.get(position));
                return true;
            }
        });
    }

    private void saveLedger() {
        String name = etName.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etName.setError("အမည်ထည့်ပါ");
            etName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(amountStr)) {
            etAmount.setError("ပမာဏထည့်ပါ");
            etAmount.requestFocus();
            return;
        }

        long amount;
        try {
            amount = Long.parseLong(amountStr);
        } catch (NumberFormatException e) {
            etAmount.setError("ဂဏန်းမှန်ထည့်ပါ");
            etAmount.requestFocus();
            return;
        }

        if (amount <= 0) {
            etAmount.setError("0 ထက်ကြီးသော ပမာဏထည့်ပါ");
            etAmount.requestFocus();
            return;
        }

        int selectedId = rgType.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "ပေးရန် / ရရန် တစ်ခုရွေးပါ", Toast.LENGTH_SHORT).show();
            return;
        }

        String type = (selectedId == R.id.rbPay) ? "Pay" : "Receive";
        String currentDate = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                .format(new Date());

        boolean inserted = myDB.insertData(name, amount, type, currentDate);

        if (inserted) {
            Toast.makeText(this, "သိမ်းဆည်းပြီးပါပြီ", Toast.LENGTH_SHORT).show();
            clearInputs();
            loadDataFromDatabase();
        } else {
            Toast.makeText(this, "သိမ်းဆည်းရာတွင် အမှားရှိနေပါသည်", Toast.LENGTH_SHORT).show();
        }
    }

    public void loadDataFromDatabase() {
        ledgerList.clear();
        totalPay = 0L;
        totalReceive = 0L;

        Cursor cursor = null;
        try {
            cursor = myDB.getAllData();

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ID));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NAME));
                    long amount = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_AMOUNT));
                    String type = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TYPE));
                    String date = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DATE));

                    if ("Pay".equalsIgnoreCase(type)) {
                        totalPay += amount;
                    } else {
                        totalReceive += amount;
                    }

                    ledgerList.add(new LedgerModel(id, name, amount, type, date));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        tvTotalPay.setText(formatMoney(totalPay));
        tvTotalReceive.setText(formatMoney(totalReceive));
        adapter.notifyDataSetChanged();
    }

    private void confirmDelete(final LedgerModel model) {
        new AlertDialog.Builder(this)
                .setTitle("ဖျက်မည်လား")
                .setMessage("ဤစာရင်းကို အမှန်တကယ် ဖျက်မည်လား?")
                .setPositiveButton("ဖျက်မည်", (dialog, which) -> {
                    int deletedRows = myDB.deleteData(model.getId());
                    if (deletedRows > 0) {
                        Toast.makeText(MainActivity.this, "စာရင်းဖျက်ပြီးပါပြီ", Toast.LENGTH_SHORT).show();
                        loadDataFromDatabase();
                    } else {
                        Toast.makeText(MainActivity.this, "ဖျက်ရာတွင် အမှားရှိနေပါသည်", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("မဖျက်ပါ", null)
                .show();
    }

    private void clearInputs() {
        etName.setText("");
        etAmount.setText("");
        rgType.check(R.id.rbPay);
        etName.requestFocus();
    }

    private String formatMoney(long amount) {
        return String.format(Locale.getDefault(), "%,d MMK", amount);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (myDB != null) {
            myDB.close();
        }
    }
}
