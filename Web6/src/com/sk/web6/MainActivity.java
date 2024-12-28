package com.sk.web6;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ToggleButton;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends Activity {
    public ArrayList<String> log;
    public WebView webView1;
    public EditText urlBox,filePath;
    public Context main;
    public ToggleButton append,update;
    public String settingsPath="/sdcard/.web5/settings.txt";
    public SettingsManager sm;
    LinearLayout ll1,ll2;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        main = this;
        log= new ArrayList<String>();
        urlBox = findViewById(R.id.urlBox);
        filePath = findViewById(R.id.filePath);
        append = findViewById(R.id.append);
        ll2 = findViewById(R.id.ll2);
        sm = new SettingsManager(this,urlBox,filePath,append);
        sm.loadSettings(settingsPath);
        //loadSettings(settingsPath);
        
        webView1 = findViewById(R.id.webView1);
        webView1.setWebViewClient(new WebViewClient6(this,"/sdcard/.web5/",log,update));
        webView1.setWebChromeClient(new WebChromeClient());
        webView1.layout(0,0,1200,600);
        
        //webView1.setAddStatesFromChildren(true);
        
        WebViewUtils3.configureWebViewUnsafe(webView1);
    }
    
    public void loadUrl1(View v){
        try{
            webView1.loadUrl(urlBox.getText().toString());
            alert("url loaded");
        }catch(Exception e){
            alert("invalid url "+e.toString());
        }
        //webView1.reload();
    }
    
    public void saveLog1(View v){
        saveLog2(filePath.getText().toString(),append.isChecked());
        alert("log saved");
    }
    
    public void saveLog2(String path,boolean appendt){
        try{
            FileWriter fw = new FileWriter(path,appendt);
            for(String s:log){
                fw.write(s+'\n');
            }
            fw.close();
            //String op=appendt? "true":"false";
            //alert("log saved , append="+op);
        }catch(IOException e){
            alert("cannot save log.invalid file path "+e.toString());
        }
    }
    
    public void alert(String msg){
        Toast.makeText(main,msg,Toast.LENGTH_SHORT).show();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        sm.saveSettings(settingsPath);
        saveLog2(filePath.getText().toString(),append.isChecked());
        //webViewClient.done();
    }
    
    public void hide(){
        
        ll2.setVisibility(0);
        urlBox.setVisibility(0);
    }
    
    @Override
    public void onBackPressed(){
        if(webView1.canGoBack()){
            webView1.goBack();
        }
    }
}