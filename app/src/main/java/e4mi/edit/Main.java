package e4mi.edit;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;

public class Main extends Activity {

    private EditText editText;
    private ParcelFileDescriptor file;
    private String savedText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setNavigationBarColor(0x00000000);
            }
            setContentView(R.layout.layout);
            editText = findViewById(R.id.editText);

            Intent intent = getIntent();
            String action = intent.getAction();
            if (!(Intent.ACTION_VIEW.equals(action) || Intent.ACTION_EDIT.equals(action)) || intent.getData() == null) {
                throw new Exception("NO FILE");
            }

            ParcelFileDescriptor readFile = getContentResolver().openFileDescriptor(intent.getData(), "r");
            if (readFile == null) {
                throw new Exception("NO FILE");
            }

            savedText = readFile(readFile);
            editText.setText(savedText);
            readFile.close();

            file = getContentResolver().openFileDescriptor(intent.getData(), "w");

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            saveFile();
            super.onBackPressed();
        }
    }

    @Override
    protected void onStop() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ECLAIR) {
            saveFile();
        }
        super.onStop();
    }

    protected String readFile(ParcelFileDescriptor file) throws IOException {
        StringBuilder sb = new StringBuilder();
        FileInputStream inputStream = new FileInputStream(file.getFileDescriptor());
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        for (String line; (line = reader.readLine()) != null; )
            sb.append(line).append('\n');
        return sb.toString();
    }

    protected void saveFile() {
        try {
            if (file == null || savedText.equals(editText.getText().toString())) {
                return;
            }
            FileOutputStream outputStream = new FileOutputStream(file.getFileDescriptor());
            FileChannel channel = outputStream.getChannel();
            channel.truncate(0);
            String text = editText.getText().toString();
            outputStream.write(text.getBytes());
            savedText = text;
            Toast.makeText(this, "saved", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}