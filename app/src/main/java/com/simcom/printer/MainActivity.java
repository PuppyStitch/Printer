package com.simcom.printer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.simcom.printer.poscommand.PrintCMD;
import com.simcom.printer.ui.main.MainFragment;
import com.simcom.printer.ui.main.UpgradeActivity;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, MainFragment.newInstance())
                    .commitNow();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_check_version:
                PrintApplication.getInstance().getPrinterPort().sendMsg(PrintCMD.getVersionInfo());
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                byte[] bs = PrintApplication.getInstance().getPrinterPort().readMsg();
                byte[] bs1 = new byte[5];
                for (int i = 1; i < 6; i++) {
                    bs1[i - 1] = bs[i];
                }
                String s = new String(bs1);
                Toast.makeText(this, "当前版本为：" + s, Toast.LENGTH_SHORT).show();
                break;
            case R.id.item_update_version:
                startActivity(new Intent(this, UpgradeActivity.class));
                break;
        }
        return true;
    }
}