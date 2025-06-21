package com.sk.revisit.components;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;

import com.sk.revisit.activities.BaseActivity;
import com.sk.revisit.helper.FileHelper;
import com.sk.revisit.webview.MyWebView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class UrlBarComponent extends Component {
    private static final int THREAD_POOL_SIZE = 4;
    private static final String HTML_EXTENSION = ".html";
    
    private final AppCompatAutoCompleteTextView urlAutoCompleteTextView;
    private final MyWebView mainWebView;
    private final String rootPath;
    private final Executor executor;

    public UrlBarComponent(@NonNull BaseActivity activity, 
                          @NonNull AppCompatAutoCompleteTextView urlAutoCompleteTextView, 
                          @NonNull MyWebView mainWebView, 
                          @NonNull String rootPath) {
        super(activity);
        this.urlAutoCompleteTextView = urlAutoCompleteTextView;
        this.mainWebView = mainWebView;
        this.rootPath = rootPath;
        this.executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        init();
    }

    private void init() {
        setupUrlBarListeners();
        setupAutoComplete(new File(rootPath));
    }

    private void setupUrlBarListeners() {
        // Handle focus change
        urlAutoCompleteTextView.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) return;
            loadUrlFromInput();
        });

        // Handle editor action (Enter key)
        urlAutoCompleteTextView.setOnEditorActionListener((v, actionId, event) -> {
            loadUrlFromInput();
            return true;
        });
    }

    private void loadUrlFromInput() {
        try {
            String url = urlAutoCompleteTextView.getText().toString().trim();
            if (!url.isEmpty()) {
                mainWebView.loadUrl(url);
            }
        } catch (Exception e) {
            activity.alert("Error loading URL: " + e.getMessage());
        }
    }

    public void setupAutoComplete(@NonNull File directory) {
        CompletableFuture.supplyAsync(() -> {
            try {
                return FileHelper.search(directory, HTML_EXTENSION);
            } catch (IOException e) {
                activity.alert("Error searching files: " + e.getMessage());
                return new ArrayList<String>();
            }
        }, executor).thenAccept(fileNames -> {
            if (fileNames != null && !fileNames.isEmpty()) {
                ArrayAdapter<String> adapter = new CustomCompletionAdapter(activity, fileNames);
                activity.runOnUiThread(() -> urlAutoCompleteTextView.setAdapter(adapter));
            }
        });
    }

    public void setText(@NonNull String text) {
        activity.runOnUiThread(() -> urlAutoCompleteTextView.setText(text));
    }

    public void release() {
        if (executor instanceof java.util.concurrent.ExecutorService) {
            ((java.util.concurrent.ExecutorService) executor).shutdown();
        }
    }
}

class CustomCompletionAdapter extends ArrayAdapter<String> {
    private static final int LAYOUT_RESOURCE = android.R.layout.simple_dropdown_item_1line;
    
    private final List<String> originalList;
    private final List<String> filteredList;

    public CustomCompletionAdapter(@NonNull Context context, @NonNull List<String> data) {
        super(context, LAYOUT_RESOURCE);
        this.originalList = new ArrayList<>(data);
        this.filteredList = new ArrayList<>(data);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(LAYOUT_RESOURCE, parent, false);
        }
        
        TextView textView = convertView.findViewById(android.R.id.text1);
        String item = getItem(position);
        if (item != null) {
            textView.setText(item);
        }
        
        return convertView;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return new UrlFilter();
    }

    @Nullable
    @Override
    public String getItem(int position) {
        return position >= 0 && position < filteredList.size() ? filteredList.get(position) : null;
    }

    @Override
    public int getCount() {
        return filteredList.size();
    }

    private class UrlFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            
            if (constraint == null || constraint.length() == 0) {
                results.values = new ArrayList<>(originalList);
                results.count = originalList.size();
            } else {
                String searchText = constraint.toString().toLowerCase();
                List<String> filtered = new ArrayList<>();
                
                for (String item : originalList) {
                    if (item.toLowerCase().contains(searchText)) {
                        filtered.add(item);
                    }
                }
                
                results.values = filtered;
                results.count = filtered.size();
            }
            
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredList.clear();
            
            if (results != null && results.count > 0) {
                filteredList.addAll((List<String>) results.values);
            } else {
                filteredList.addAll(originalList);
            }
            
            notifyDataSetChanged();
        }
    }
}
