package com.health.openscale.connector;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    final int REQUEST_CODE = 1;

    final String APP_ID = BuildConfig.APPLICATION_ID.replace(".connector", "");
    final String AUTHORITY = APP_ID + ".provider";
    final String REQUIRED_PERMISSION = APP_ID + ".READ_DATA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_CODE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                TextView text = findViewById(R.id.mainText);
                text.setText("No permission to access openScale data");
            }
            else {
                getProviderData();
            }

        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                REQUIRED_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{REQUIRED_PERMISSION}, REQUEST_CODE);
        } else {
            getProviderData();
        }
    }

    private void getProviderData() {
        Uri metaUri = new Uri.Builder()
                .scheme(ContentResolver.SCHEME_CONTENT)
                .authority(AUTHORITY)
                .path("meta")
                .build();
        Uri usersUri = new Uri.Builder()
                .scheme(ContentResolver.SCHEME_CONTENT)
                .authority(AUTHORITY)
                .path("users")
                .build();
        Uri measurementsUri = new Uri.Builder()
                .scheme(ContentResolver.SCHEME_CONTENT)
                .authority(AUTHORITY)
                .path("measurements")
                .build();

        StringBuilder s = new StringBuilder();

        Cursor cursor = getContentResolver().query(
                metaUri, null, null, null, null);

        try {
            while (cursor.moveToNext()) {
                s.append("====== META ======");
                s.append(System.lineSeparator());

                for (int i = 0; i < cursor.getColumnCount(); ++i) {
                    s.append(" - ").append(cursor.getColumnName(i));
                    s.append(": ").append(cursor.getString(i));
                    s.append(System.lineSeparator());
                }
            }
        }
        finally {
            cursor.close();
        }

        cursor = getContentResolver().query(
                usersUri, null, null, null, null);

        try {
            int user = 0;
            while (cursor.moveToNext()) {
                s.append("====== USER ");
                s.append(++user).append("/").append(cursor.getCount());
                s.append(" ======");
                s.append(System.lineSeparator());

                for (int i = 0; i < cursor.getColumnCount(); ++i) {
                    s.append(" - ").append(cursor.getColumnName(i));
                    s.append(": ").append(cursor.getString(i));
                    s.append(System.lineSeparator());
                }

                long userId = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
                Cursor m = getContentResolver().query(
                        ContentUris.withAppendedId(measurementsUri, userId),
                        null, null, null, null);

                try {
                    int measurement = 0;
                    while (m.moveToNext()) {
                        s.append("++++++ MEASUREMENT ");
                        s.append(++measurement).append("/").append(m.getCount());
                        s.append(" ++++++");
                        s.append(System.lineSeparator());
                        for (int i = 0; i < m.getColumnCount(); ++i) {
                            s.append("  * ").append(m.getColumnName(i));
                            s.append(": ").append(m.getString(i));
                            s.append(System.lineSeparator());
                        }
                    }
                }
                finally {
                    m.close();
                }
            }
        }
        finally {
            cursor.close();
        }
        TextView text = findViewById(R.id.mainText);
        text.setText(s.toString());
    }
}
