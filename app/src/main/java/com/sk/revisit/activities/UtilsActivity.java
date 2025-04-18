package com.sk.revisit.activities;

import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.annotation.Nullable;

import com.sk.revisit.MyUtils;
import com.sk.revisit.databinding.ActivityUtilsBinding;
import com.sk.revisit.managers.MySettingsManager;

import java.util.Locale;
import java.util.List;
import java.util.ArrayList;
import java.io.BufferedWriter;
import java.io.FileWriter;

public class UtilsActivity extends BaseActivity {

    ActivityUtilsBinding binding;
    MyUtils utils;
    StringBuilder currentObjGuess = new StringBuilder("");
    String currentText, prevText,currentObj;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUtilsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        MySettingsManager sm = new MySettingsManager(this);
        utils = new MyUtils(this, sm.getRootStoragePath());

        binding.exeBuildLocalPath.setOnClickListener(v -> {
            try {
                String url = binding.url.getText().toString();
                String localPath = utils.buildLocalPath(Uri.parse(url));
                binding.localpath.setText(localPath);
            } catch (Exception e) {
                alert(e.toString());
            }
        });

        binding.textwatch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.op2.setText(String.format(Locale.ENGLISH,"chars: %s, start: %d, count: %d, after: %d",charSequence,i,i1,i2));
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.op3.setText(String.format(Locale.ENGLISH,"chars: %s, start: %d, before: %d, count: %d",s,start,before,count));
                boolean isTextInserted = (before == 0);
                currentText = s.toString();
                if(isTextInserted) {
                    char insertedTextChar = s.charAt(start);
                    currentObjGuess.append(insertedTextChar);
                    binding.op3.setText(currentObjGuess.toString());

                    try{
                        if(" ({[\\/];,'\"%$&*-=+]})".contains(String.valueOf(insertedTextChar))){
                            currentObj = "";
                            currentObjGuess = new StringBuilder("");
                        }else if (insertedTextChar=='.'){
                            currentObj = currentObjGuess.toString();
                            binding.op4.setText(currentObj);
                        }
                        if (currentObjGuess == null) {
                            currentObjGuess = new StringBuilder();
                        }
                    }catch(Exception e){
                        binding.op5.setText(e.toString());
                    }
                } else {
                    
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    void buildLocalPath(List<String> urls, String saveFilePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(saveFilePath))) {
            for (String url : urls) {
                String localPath = utils.buildLocalPath(Uri.parse(url));
                writer.write(localPath + "\n");
            }
        } catch (Exception e) {
            alert(e.toString());
        }
    }
}
