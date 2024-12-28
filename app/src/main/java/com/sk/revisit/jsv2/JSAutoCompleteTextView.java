package com.sk.revisit.jsv2;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.webkit.WebView;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

public class JSAutoCompleteTextView extends androidx.appcompat.widget.AppCompatAutoCompleteTextView {
    private static final String TAG = "JSAutoCompleteTextView";
    private static final long DEBOUNCE_DELAY = 300;
    private final Handler handler = new Handler(Looper.getMainLooper());
    public ArrayAdapter<String> adapter;
    private Runnable pendingAutocompleteTask;
    private JavaScriptExecutor jsExecutor;
    private AutoCompleteManager autoCompleteManager;

    public JSAutoCompleteTextView(Context context) {
        super(context);
        init();
    }

    public JSAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public JSAutoCompleteTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
        setAdapter(adapter);
        autoCompleteManager = new AutoCompleteManager();

        addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (jsExecutor != null) {
                    if (pendingAutocompleteTask != null) {
                        handler.removeCallbacks(pendingAutocompleteTask);
                    }
                    pendingAutocompleteTask = () -> updateAutocompleteSuggestions(s.toString());
                    handler.postDelayed(pendingAutocompleteTask, DEBOUNCE_DELAY);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    public void setWebView(WebView webView) {
        if (webView == null) {
            throw new IllegalArgumentException("WebView cannot be null.");
        }
        this.jsExecutor = new JavaScriptExecutor(webView);
    }

    private void updateAutocompleteSuggestions(String input) {
        if (jsExecutor == null)
            return;

        String jsCode = "(function() { return Object.getOwnPropertyNames(window); })();";

        jsExecutor.execute(jsCode, result -> {
            List<String> suggestions = autoCompleteManager.getSuggestions(result, input);
            handler.post(() -> {
                adapter.clear();
                adapter.addAll(suggestions);
                adapter.notifyDataSetChanged();
            });
        });
    }
}