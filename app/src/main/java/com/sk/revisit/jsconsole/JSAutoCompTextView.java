package com.sk.revisit.jsconsole;

import android.os.Handler;
import android.os.Looper;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class JSAutoCompTextView extends AppCompatAutoCompleteTextView {

    WebView webView;
    TextWatcher textWatcher;
    String currentText, prevText,currentObj,TAG="JSAutoCompTextView";
    StringBuilder currentObjGuess = new StringBuilder("");
    JSAdapter adapter;
    boolean isForObj = false;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private static final List<String> JS_KEYWORDS = new ArrayList<>(
            List.of("var", "let", "const", "function", "if", "else", "for", "while", "return",
                    "try", "catch", "finally", "switch", "case", "break", "default", "new",
                    "this", "typeof", "instanceof", "delete", "void", "debugger"));

    public JSAutoCompTextView(@NonNull Context context) {
        super(context,null);
    }

    public JSAutoCompTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public JSAutoCompTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs,androidx.appcompat.R.attr.autoCompleteTextViewStyle);
    }

    public void setWebView(WebView webView) {
        this.webView = webView;
        init();
    }

    private void init() {
        initTextChangedListener();
        adapter = new JSAdapter(getContext(),android.R.layout.simple_dropdown_item_1line,JS_KEYWORDS);
        setAdapter(adapter);
    }

    private void initTextChangedListener() {
        textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // prevText = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean isTextInserted = (before == 0);
                currentText = s.toString();
                if(isTextInserted) {
                    // String insertedText = currentText.substring(start,count);
                    char insertedTextChar = s.charAt(start);

                    try{
                        if(" ({[\\/];,'\"%$&*-=+]})\n\r".contains(String.valueOf(insertedTextChar))){
                            currentObj = "";
                            currentObjGuess = new StringBuilder();
                            List<String> completions = getProperties("this");
                            adapter.clear();
                            adapter.setCompletions(completions);
                            adapter.provideKeywords(true);
                        //    adapter.notifyDataSetChanged();
                        }else if (insertedTextChar=='.'){
                            currentObj = currentObjGuess.toString();
                            List<String> objMembers = getProperties(currentObj);
                            adapter.clear();
                            adapter.setCompletions(objMembers);
                        //    adapter.notifyDataSetChanged();
                        }else {
                            currentObjGuess.append(insertedTextChar);
                        }
                    }catch(Exception e){
                        Log.e(TAG,e.toString());
                    }
                } else {
                    //
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                //pass
            }
        };

        addTextChangedListener(textWatcher);
    }

    List<String> getProperties(String obj) {
        String query ="(function() { try { return Object.getOwnPropertyNames(" + obj + "); } catch (e) { return []; } })();";
        String result = evaluateJsSync(query);
        Log.d(TAG,"query= "+query);
        Log.d(TAG,"result= "+result);
        List<String> suggestions = new ArrayList<>();
        try {
            JSONArray jsArray = new JSONArray(result);
            for (int i = 0; i < jsArray.length(); i++) {
                String variable = jsArray.getString(i).trim();
                suggestions.add(variable);
            }
            return suggestions;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing JavaScript response: " + e.getMessage());
            return null;
        }
    }

    private String evaluateJsSync(String jsCode) {
        final String[] result = new String[1];
        final Object lock = new Object();
        final boolean[] completed = {false};
        handler.post(() -> {
            try {
                webView.evaluateJavascript(jsCode, value -> {
                    synchronized (lock) {
                        result[0] = value;
                        completed[0] = true;
                        lock.notify();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "JavaScript evaluation failed: " + e.getMessage(), e);
                synchronized (lock) {
                    result[0] = null;
                    completed[0] = true;
                    lock.notify();
                }
            }
        });
        synchronized (lock) {
            try {
                if (!completed[0]) lock.wait(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
        return result[0];
    }


    class JSAdapter extends ArrayAdapter<String> {
        List<String> JS_KEYWORDS,completions,curr;
        private boolean useKeywords = true;

        public JSAdapter(@NonNull Context context, int resource,List<String> data) {
            super(context, resource);
            this.JS_KEYWORDS = new ArrayList<>(data);
            this.completions =  new ArrayList<>();
            this.curr = new ArrayList<>();
        }

        public void setCompletions(List<String> data){
            completions = data;
        }

        // @Override
        // public View getView(int position, View convertView, ViewGroup parent) {
        //     View view = super.getView(position, convertView, parent);
        //     String suggestion = getItem(position);
        //     TextView tv = (TextView) view;
        //     SpannableString ss = new SpannableString(suggestion);
        //     ss.setSpan(new StyleSpan(Typeface.BOLD), start,
        //             start + prefix.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        //     tv.setText(ss);
        //     return view;
        // }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_dropdown_item_1line, parent, false);
            }
            TextView textView = convertView.findViewById(android.R.id.text1);
            textView.setText(getItem(position));
            return convertView;
        }

        @NonNull
        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence charSequence) {
                    FilterResults results = new FilterResults();
                    if (charSequence != null) {
                        String searchText = charSequence.toString().toLowerCase();
                        List<String> filteredList = new ArrayList<>();
                        for (String item : completions) {
                            if (item.toLowerCase().contains(searchText)) {
                                filteredList.add(item);
                            }
                        }
                        if(useKeywords){
                            for (String item : JS_KEYWORDS) {
                                if (item.toLowerCase().contains(searchText)) {
                                    filteredList.add(item);
                                }
                            }   
                        }
                        results.values = filteredList;
                        results.count = filteredList.size();
                    }
                    return results;
                }

                @Override
                protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                    curr.clear();
                    if (filterResults != null && filterResults.count > 0) {
                        curr.addAll((List<String>) filterResults.values);
                        notifyDataSetChanged();
                    } else {
                        curr.addAll(completions);
                        notifyDataSetChanged();
                    }
                }
            };
        }

        @Nullable
        @Override
        public String getItem(int position) {
            return curr.get(position);
        }

        @Override
        public int getCount() {
            return curr.size();
        }

        void add(List<String> items) {
            curr.addAll(items);
        }

        void provideKeywords(boolean s){
            useKeywords = s;
        }
    }
}