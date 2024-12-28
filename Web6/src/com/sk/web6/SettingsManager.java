package com.sk.web6;

import android.content.Context;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class SettingsManager {

    private Properties settings;
    private EditText urlBox;
    private EditText filePath;
    private ToggleButton append;
    public Context main;
    
    public SettingsManager(Context c,EditText urlBox, EditText filePath, ToggleButton append) {
        this.urlBox = urlBox;
        this.filePath = filePath;
        this.append = append;
        this.settings = new Properties();
        this.main = c;
    }

    public void loadSettings(String path) {
        try (FileReader fr = new FileReader(path)) {
            settings.load(fr);
            urlBox.setText(settings.getProperty("urlBox", ""));
            filePath.setText(settings.getProperty("filePath", ""));
            append.setSelected(Boolean.parseBoolean(settings.getProperty("append", "false"))); 
        } catch(Exception e){
            alert("err "+e.toString());
        }
    }

    public void saveSettings(String path) {
        try (FileWriter fw = new FileWriter(path)) {
            settings.setProperty("urlBox", urlBox.getText().toString());
            settings.setProperty("filePath", filePath.getText().toString());
            settings.setProperty("append", Boolean.toString(append.isSelected()));
            settings.store(fw, "Application Settings"); 
            //alert("Setting saved"); 
        } catch (IOException e) {
            alert("Error saving setting: " + e.toString());
        }
    }

    public void alert(String msg){
        Toast.makeText(main,msg,Toast.LENGTH_LONG).show();
    }
}
