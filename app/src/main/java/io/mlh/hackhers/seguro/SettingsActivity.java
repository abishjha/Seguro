package io.mlh.hackhers.seguro;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        onSwitchClick();
    }

    // Set up on click listener for share location switch
    private void onSwitchClick(){
        Switch locSwitch = findViewById(R.id.shareLocationSwitch);

        locSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // do something, the isChecked will be
                // true if the switch is in the On position
                if(!isChecked)
                    showDialog();
            }
        });

    }

    private void showDialog(){

    }
}
