package com.sk.revisit.managers;

import android.content.Context;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MyLogManager {

    File file;
    String filePath;
    FileWriter fw;
    Context c;

    MyLogManager(Context c, String filePath) {
        this.filePath = filePath;
        this.c = c;
        this.file = new File(this.filePath);
        try {
            this.fw = new FileWriter(this.file);
        } catch (Exception e) {
            alert(e.toString());
        }
    }

    public void log(String msg) {
        try {
            fw.write(msg + '\n');
        } catch (IOException e) {
            alert(e.toString());
        }
    }

    void alert(String msg) {
        Toast.makeText(c, msg, Toast.LENGTH_LONG).show();
    }
}