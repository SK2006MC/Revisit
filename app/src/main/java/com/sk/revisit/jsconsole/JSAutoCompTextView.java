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
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.sk.revisit.log.FileLogger;

public class JSAutoCompTextView extends AppCompatAutoCompleteTextView {
    private static final String TAG = "JSAutoCompTextView";
    private static final int JS_EVALUATION_TIMEOUT_MS = 200;
    private static final List<String> JS_KEYWORDS = Arrays.asList(
            "var", "let", "const", "function", "if", "else", "for", "while", "return",
            "try", "catch", "finally", "switch", "case", "break", "default", "new",
            "this", "typeof", "instanceof", "delete", "void", "debugger"
    );
    private static final String OBJECT_PROPERTY_QUERY = 
            "(function() { try { return Object.getOwnPropertyNames(%s); } catch (e) { return []; } })();";

    private WebView webView;
    private JSAdapter adapter;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final StringBuilder currentObjectBuilder = new StringBuilder();
    private String currentObject = "";
    private FileLogger fileLogger;

    public JSAutoCompTextView(@NonNull Context context) {
        super(context, null);
        initializeLogger(context);
    }

    public JSAutoCompTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeLogger(context);
    }

    public JSAutoCompTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs, androidx.appcompat.R.attr.autoCompleteTextViewStyle);
        initializeLogger(context);
    }

    private void initializeLogger(Context context) {
        String logFilePath = context.getObbDir() + "/app.log";
        fileLogger = new FileLogger(logFilePath);
    }

    public void setWebView(WebView webView) {
        this.webView = webView;
        Log.d(TAG, "WebView set");
        fileLogger.log("WebView set");
        initializeAutoComplete();
    }

    private void initializeAutoComplete() {
        if (webView == null) {
            Log.w(TAG, "WebView is null, cannot initialize auto-complete");
            return;
        }

        setupTextWatcher();
        setupAdapter();
    }

    private void setupTextWatcher() {
        addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed for this implementation
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count == 0) return; // Text was deleted, not inserted
                
                handleTextInsertion(s, start, count);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Not needed for this implementation
            }
        });
    }

    private void handleTextInsertion(CharSequence text, int start, int count) {
        char insertedChar = text.charAt(start);
        Log.d(TAG, "Text inserted: " + insertedChar);
        fileLogger.log("Text inserted: " + insertedChar);
        
        if (isObjectTerminator(insertedChar)) {
            resetObjectContext();
            updateCompletions("this");
        } else if (insertedChar == '.') {
            currentObject = currentObjectBuilder.toString();
            updateCompletions(currentObject);
        } else {
            currentObjectBuilder.append(insertedChar);
        }
    }

    private boolean isObjectTerminator(char ch) {
        return " ({[\\/];,'\"%$&*-=+]})\n\r".indexOf(ch) != -1;
    }

    private void resetObjectContext() {
        currentObject = "";
        currentObjectBuilder.setLength(0);
    }

    private void setupAdapter() {
        adapter = new JSAdapter(getContext(), android.R.layout.simple_dropdown_item_1line);
        setAdapter(adapter);
    }

    private void updateCompletions(String objectName) {
        List<String> properties = getObjectProperties(objectName);
        adapter.updateCompletions(properties);
    }

    private List<String> getObjectProperties(String objectName) {
        if (webView == null) {
            Log.w(TAG, "WebView is null, cannot get properties");
            return new ArrayList<>();
        }

        String query = String.format(OBJECT_PROPERTY_QUERY, objectName);
        String result = evaluateJavaScriptSynchronously(query);
        
        return parseJavaScriptArray(result);
    }

    private List<String> parseJavaScriptArray(String jsonResult) {
        List<String> properties = new ArrayList<>();
        
        if (jsonResult == null || jsonResult.equals("null")) {
            return properties;
        }

        try {
            // Remove quotes from JSON string if present
            String cleanResult = jsonResult.replaceAll("^\"|\"$", "");
            JSONArray jsonArray = new JSONArray(cleanResult);
            
            for (int i = 0; i < jsonArray.length(); i++) {
                String property = jsonArray.getString(i).trim();
                if (!property.isEmpty()) {
                    properties.add(property);
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing JavaScript response: " + e.getMessage());
        }
        
        return properties;
    }

    private String evaluateJavaScriptSynchronously(String jsCode) {
        final String[] result = {null};
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
                fileLogger.log("JavaScript evaluation failed: " + e.getMessage());
                synchronized (lock) {
                    completed[0] = true;
                    lock.notify();
                }
            }
        });

        synchronized (lock) {
            try {
                if (!completed[0]) {
                    lock.wait(JS_EVALUATION_TIMEOUT_MS);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Log.w(TAG, "JavaScript evaluation interrupted");
                fileLogger.log("JavaScript evaluation interrupted");
            }
        }

        return result[0];
    }

    private static class JSAdapter extends ArrayAdapter<String> {
        private final List<String> keywords;
        private List<String> completions;
        private List<String> filteredItems;
        private boolean includeKeywords = true;

        public JSAdapter(@NonNull Context context, int resource) {
            super(context, resource);
            this.keywords = new ArrayList<>(JS_KEYWORDS);
            this.completions = new ArrayList<>();
            this.filteredItems = new ArrayList<>();
        }

        public void updateCompletions(List<String> newCompletions) {
            this.completions = newCompletions != null ? newCompletions : new ArrayList<>();
            getFilter().filter("");
        }

        public void setIncludeKeywords(boolean include) {
            this.includeKeywords = include;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(android.R.layout.simple_dropdown_item_1line, parent, false);
            }
            
            TextView textView = convertView.findViewById(android.R.id.text1);
            String item = getItem(position);
            textView.setText(item != null ? item : "");
            
            return convertView;
        }

        @NonNull
        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();
                    List<String> filtered = new ArrayList<>();

                    if (constraint != null) {
                        String searchText = constraint.toString().toLowerCase();
                        
                        // Filter completions
                        for (String completion : completions) {
                            if (completion.toLowerCase().contains(searchText)) {
                                filtered.add(completion);
                            }
                        }
                        
                        // Add keywords if enabled
                        if (includeKeywords) {
                            for (String keyword : keywords) {
                                if (keyword.toLowerCase().contains(searchText) && !filtered.contains(keyword)) {
                                    filtered.add(keyword);
                                }
                            }
                        }
                    } else {
                        // Show all completions when no constraint
                        filtered.addAll(completions);
                        if (includeKeywords) {
                            filtered.addAll(keywords);
                        }
                    }

                    results.values = filtered;
                    results.count = filtered.size();
                    return results;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    filteredItems.clear();
                    if (results != null && results.count > 0) {
                        @SuppressWarnings("unchecked")
                        List<String> items = (List<String>) results.values;
                        filteredItems.addAll(items);
                    }
                    notifyDataSetChanged();
                }
            };
        }

        @Nullable
        @Override
        public String getItem(int position) {
            return position >= 0 && position < filteredItems.size() ? filteredItems.get(position) : null;
        }

        @Override
        public int getCount() {
            return filteredItems.size();
        }
    }
}
