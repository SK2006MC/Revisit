package com.sk.revisit.components;

import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.appcompat.app.AppCompatActivity;

import com.sk.revisit.databinding.NavJsBinding;
import com.sk.revisit.jsconsole.JSAutoCompleteTextView2;
import com.sk.revisit.jsconsole.JSConsoleLogger;
import com.sk.revisit.jsconsole.JSWebViewManager;

public class JSNavComponent extends Component {

	JSAutoCompleteTextView2 jsAutoCompleteTextView;
	JSConsoleLogger jsConsoleLogger;
	JSWebViewManager jsWebViewManager;
	LinearLayout jsConsoleLayout;
	NavJsBinding binding;

	public JSNavComponent(AppCompatActivity activity, NavJsBinding binding, WebView webView) {
		super(activity);
		this.binding = binding;

		init(webView);
	}

	void init(WebView webView) {
		jsConsoleLayout = binding.consoleLayout;
		jsAutoCompleteTextView = binding.jsInput;

		ScrollView jsConsoleScrollView = binding.consoleScrollView;
		ImageButton executeJsButton = binding.executeJsBtn;

		jsConsoleLogger = new JSConsoleLogger(this.context, jsConsoleLayout, jsConsoleScrollView);
		jsWebViewManager = new JSWebViewManager(this.context, webView, jsConsoleLogger);

		//init js execute button
		executeJsButton.setOnClickListener(v -> {
			String code = jsAutoCompleteTextView.getText().toString();
			jsWebViewManager.executeJS(code, r -> jsConsoleLogger.logConsoleMessage(">" + code + "\n" + r + "\n"));
		});

		executeJsButton.setOnLongClickListener(arg0 -> {
			jsConsoleLayout.removeAllViewsInLayout();
			return true;
		});

		jsAutoCompleteTextView.setWebView(webView);
	}

	public JSConsoleLogger getJsConsoleLogger() {
		return jsConsoleLogger;
	}
}
