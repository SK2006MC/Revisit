package com.sk.revisit.components;

import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.sk.revisit.webview.MyWebView;

public class UrlBarComponent extends Component {
	private final EditText urlEditText;
	private final MyWebView mainWebView;

	public UrlBarComponent(AppCompatActivity activity, EditText mUrlEditText, MyWebView mainWebView) {
		super(activity);
		urlEditText = mUrlEditText;
		this.mainWebView = mainWebView;
		init();
	}

	void init(){
		//init urlEditText
		urlEditText.setOnFocusChangeListener((view, hasFocus) -> {
			if (hasFocus) return;
			try {
				mainWebView.loadUrl(urlEditText.getText().toString());
			} catch (Exception e) {
				alert(e.toString());
			}
		});

		urlEditText.setOnEditorActionListener((v, actionId, event) -> {
			try {
				mainWebView.loadUrl(urlEditText.getText().toString());
			} catch (Exception e) {
				alert(e.toString());
			}
			return true;
		});
	}

	public void setText(String text) {
		activity.runOnUiThread(() -> urlEditText.setText(text));
	}
}