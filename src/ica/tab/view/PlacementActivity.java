package ica.tab.view;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class PlacementActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView textview = new TextView(this);
        textview.setText("This is Employee tab");
        setContentView(textview);
    }
}