package com.sk.revisit.components;

import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.sk.revisit.activities.BaseActivity;
import com.sk.revisit.databinding.NavJsBinding;
import com.sk.revisit.jsconsole.JSAutoCompTextView;
import com.sk.revisit.jsconsole.JSConsoleLogger;
import com.sk.revisit.jsconsole.JSWebViewManager;

public class JSNavComponent extends Component {

    JSAutoCompTextView jsAutoCompleteTextView;
    JSConsoleLogger jsConsoleLogger;
    JSWebViewManager jsWebViewManager;
    LinearLayout jsConsoleLayout;
    NavJsBinding binding;

    public JSNavComponent(BaseActivity activity, NavJsBinding binding, WebView webView) {
        super(activity);
        this.binding = binding;

        jsConsoleLayout = binding.consoleLayout;
        jsAutoCompleteTextView = binding.jsInput;

        ScrollView jsConsoleScrollView = binding.consoleScrollView;
        jsConsoleLogger = new JSConsoleLogger(context, jsConsoleLayout, jsConsoleScrollView);


        init(webView);
    }

    void init(WebView webView) {

        ImageButton executeJsButton = binding.executeJsBtn;

        jsWebViewManager = new JSWebViewManager(context, webView, jsConsoleLogger);

        //init js execute button
        executeJsButton.setOnClickListener(v -> {
            String code = jsAutoCompleteTextView.getText().toString();
            jsWebViewManager.executeJS(code, r -> jsConsoleLogger.logConsoleMessage(">" + code + "\n" + r + "\n"));
        });

        executeJsButton.setOnLongClickListener(arg0 -> {
            jsConsoleLayout.removeAllViewsInLayout();
            return true;
        });

        executeJsButton.setTooltipText("Click to execute.\nLong click to clear logs.");

        jsAutoCompleteTextView.setWebView(webView);
    }

    public JSConsoleLogger getJsConsoleLogger() {
        return jsConsoleLogger;
    }
}
