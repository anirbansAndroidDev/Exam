package ica.tab.view;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class StudyMaterialActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView textview = new TextView(this);
        textview.setText("This is Apple tab");
        setContentView(textview);
    }
}