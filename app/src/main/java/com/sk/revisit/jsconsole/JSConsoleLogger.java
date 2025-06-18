// JSConsoleLogger.java
package com.sk.revisit.jsconsole;

import android.content.Context;
import android.webkit.ConsoleMessage;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.sk.revisit.R;

/**
 * Handles JavaScript console logging inside the UI.
 */
public class JSConsoleLogger {
    private final LinearLayout consoleLayout;
    private final ScrollView consoleScrollView;
    private final Context context;

    public JSConsoleLogger(Context context, LinearLayout consoleLayout, ScrollView consoleScrollView) {
        this.context = context;
        this.consoleLayout = consoleLayout;
        this.consoleScrollView = consoleScrollView;
    }

    public void logConsoleMessage(String message, ConsoleMessage.MessageLevel level) {
        TextView textView = new TextView(context);
        textView.setText(message);
        textView.setTextColor(getColorForLogLevel(level));
        //textView.setTextIsSelectable(true);

        consoleLayout.addView(textView);
        consoleScrollView.fullScroll(ScrollView.FOCUS_DOWN);
    }

    public void logConsoleMessage(ConsoleMessage consoleMessage) {
        String formattedMessage =
                consoleMessage.message()
                        + ":at "
                        + consoleMessage.lineNumber()
                        + " in "
                        + consoleMessage.sourceId();

        logConsoleMessage(formattedMessage, consoleMessage.messageLevel());
    }

    private int getColorForLogLevel(ConsoleMessage.MessageLevel level) {
        switch (level) {
            case DEBUG:
                return ContextCompat.getColor(context, R.color.debug);
            case ERROR:
                return ContextCompat.getColor(context, R.color.err);
            case WARNING:
                return ContextCompat.getColor(context, android.R.color.holo_orange_dark);
            case LOG:
                return ContextCompat.getColor(context, R.color.log);
            case TIP:
                return ContextCompat.getColor(context, R.color.tip);
            default:
                return ContextCompat.getColor(context, R.color.white);
        }
    }

    public void logConsoleMessage(String r) {
        logConsoleMessage(r, ConsoleMessage.MessageLevel.LOG);
    }
}