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
    private final AppCompatAutoCompleteTextView urlAppCompatAutoCompleteTextView;
    private final MyWebView mainWebView;
    String rootPath;

    public UrlBarComponent(BaseActivity activity, AppCompatAutoCompleteTextView mUrlAppCompatAutoCompleteTextView, MyWebView mainWebView, String rootPath) {
        super(activity);
        urlAppCompatAutoCompleteTextView = mUrlAppCompatAutoCompleteTextView;
        this.mainWebView = mainWebView;
        this.rootPath = rootPath;
        init();
    }

    void init() {
        //init urlAppCompatAutoCompleteTextView
        urlAppCompatAutoCompleteTextView.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) return;
            try {
                mainWebView.loadUrl(urlAppCompatAutoCompleteTextView.getText().toString());
            } catch (Exception e) {
                alert(e.toString());
            }
        });

        urlAppCompatAutoCompleteTextView.setOnEditorActionListener((v, actionId, event) -> {
            try {
                mainWebView.loadUrl(urlAppCompatAutoCompleteTextView.getText().toString());
            } catch (Exception e) {
                alert(e.toString());
            }
            return true;
        });

        setupAutoComplete(new File(rootPath));
    }

    public void setupAutoComplete(File directory) {
        Executor executor = Executors.newFixedThreadPool(4);
        CompletableFuture.supplyAsync(() -> {
            //List<String> fileNames = new ArrayList<>();
            //FileHelper.searchRecursive(directory, ".html",fileNames);
            try {
                return FileHelper.search(directory, ".html");
            } catch (IOException e) {
                activity.alert(e.toString());
                return null;
            }
        }, executor).thenAccept(fileNames -> {
            ArrayAdapter<String> adapter = new CustomCompletionAdapter(activity,android.R.layout.simple_dropdown_item_1line, fileNames);
            activity.runOnUiThread(() -> urlAppCompatAutoCompleteTextView.setAdapter(adapter));
        });
    }

    public void setText(String text) {
        activity.runOnUiThread(() -> urlAppCompatAutoCompleteTextView.setText(text));
    }
}

class CustomCompletionAdapter extends ArrayAdapter<String> {

    private List<String> org, curr;

    public CustomCompletionAdapter(@NonNull Context context, int resource, List<String> data) {
        super(context, resource);
        org = new ArrayList<>(data);
        curr = new ArrayList<>(data);
    }

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
                    for (String item : org) {
                        if (item.toLowerCase().contains(searchText)) {
                            filteredList.add(item);
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
                    curr.addAll(org);
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
}
