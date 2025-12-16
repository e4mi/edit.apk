package e4mi.edit;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.FrameLayout;
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

            FrameLayout layout = new FrameLayout(this);
            editText = new EditText(this);
            editText.setSingleLine(false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                editText.setBackground(null);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                editText.setFitsSystemWindows(true);
            }
            int padding = (int) (4 * getResources().getDisplayMetrics().density);
            editText.setPadding(padding, padding, padding, padding);
            editText.setVerticalScrollBarEnabled(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                editText.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
            }
            editText.setGravity(Gravity.TOP);
            layout.addView(editText);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(layout);

            Intent intent = getIntent();
            String action = intent.getAction();
            if (!(Intent.ACTION_VIEW.equals(action) || Intent.ACTION_EDIT.equals(action)) || intent.getData() == null) {
                throw new RuntimeException("NO FILE");
            }

            ParcelFileDescriptor readFile = getContentResolver().openFileDescriptor(intent.getData(), "r");

            if (readFile == null) {
                throw new RuntimeException("COULD NOT OPEN FILE");
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
        if (file == null || savedText.equals(editText.getText().toString())) {
            return;
        }
        try {
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