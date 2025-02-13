package com.sk.revisit.jsconsole;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.LruCache;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatAutoCompleteTextView;

import com.sk.revisit.log.Log;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class JSAutoCompleteTextView1 extends AppCompatAutoCompleteTextView {
	private static final String TAG = "JSAutoCompleteTextView";
	private static final long DEBOUNCE_DELAY = 300;
	private static final int MAX_CACHE_SIZE = 50; // Number of suggestion lists to cache
	private static final Set<String> JS_KEYWORDS = new HashSet<>(
			List.of("var", "let", "const", "function", "if", "else", "for", "while", "return",
					"try", "catch", "finally", "switch", "case", "break", "default", "new",
					"this", "typeof", "instanceof", "delete", "void", "debugger"));
	private final Handler handler = new Handler(Looper.getMainLooper());
	private final ExecutorService executor = Executors.newSingleThreadExecutor(); // For JS execution
	private final LruCache<String, List<String>> suggestionCache = new LruCache<>(MAX_CACHE_SIZE);
	public ArrayAdapter<String> adapter;
	private WebView webView;
	private Future<?> currentTask; // To cancel pending tasks
	private String userInput = "";

	public JSAutoCompleteTextView1(Context context) {
		super(context);
		init();
	}

	public JSAutoCompleteTextView1(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
		setAdapter(adapter);

		addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				userInput = s.toString();
				if (webView != null) {
					updateAutocompleteSuggestions(s.toString()); // Now calls directly, debounce is within
				}
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		// Handle Enter key
		setOnEditorActionListener((v, actionId, event) -> {
			if (event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
				executeCurrentCommand();
				return true;
			}
			return false;
		});
	}

	public void setWebView(WebView webView) {
		if (webView == null) {
			throw new IllegalArgumentException("WebView cannot be null.");
		}
		this.webView = webView;
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		// Shutdown the executor to prevent memory leaks
		executor.shutdownNow();
		try {
			executor.awaitTermination(500, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		webView = null; // Clear WebView reference to prevent leaks
	}

	// --- JavaScript Execution and Suggestion Retrieval ---
	private String evaluateJavascript(String jsCode) throws InterruptedException, TimeoutException {
		final String[] result = new String[1];
		final boolean[] completed = {false};

		ValueCallback<String> valueCallback = value -> {
			result[0] = value;
			completed[0] = true;
			synchronized (completed) {
				completed.notify();
			}
		};

		handler.post(() -> {
			if (webView != null) {
				webView.evaluateJavascript(jsCode, valueCallback);
			} else {
				Log.e(TAG, "WebView is null");
				synchronized (completed) {
					completed[0] = true;
					completed.notify();
				}
			}
		});

		synchronized (completed) {
			if (!completed[0]) {
				completed.wait(500); // Wait for up to 500ms
				if (!completed[0]) {
					throw new TimeoutException("JavaScript evaluation timed out");
				}
			}
		}

		return result[0];
	}

	private void executeJavaScript(String jsCode, ValueCallback<String> callback) {
		if (webView == null) {
			Log.e(TAG, "WebView is null, cannot execute JavaScript.");
			handler.post(() -> Toast.makeText(getContext(), "WebView is not initialized.", Toast.LENGTH_SHORT).show()); //Inform user
			return;
		}

		executor.submit(() -> { // Execute asynchronously
			try {
				webView.evaluateJavascript(jsCode, callback);
				//We may not get direct result from this callback, so we handle it in the callback itself
			} catch (Exception e) {
				Log.e(TAG, "JavaScript execution error: " + e.getMessage());
				handler.post(() -> Toast.makeText(getContext(), "Error executing JavaScript: " + e.getMessage(), Toast.LENGTH_SHORT).show());
			}
		});
	}

	private List<String> getSuggestions(String result) {
		List<String> suggestions = new ArrayList<>();

		try {
			JSONArray jsArray = new JSONArray(result);
			for (int i = 0; i < jsArray.length(); i++) {
				String variable = jsArray.getString(i).trim();
				if (variable.toLowerCase().startsWith(userInput.toLowerCase())) {
					suggestions.add(variable);
				}
			}
		} catch (Exception e) {
			Log.e(TAG, "Error parsing JavaScript response: " + e.getMessage());
		}

		// Add JavaScript keywords as suggestions
		for (String keyword : JS_KEYWORDS) {
			if (keyword.toLowerCase().startsWith(userInput.toLowerCase())) {
				suggestions.add(keyword);
			}
		}

		return suggestions;
	}

	// --- Autocomplete Updates (Debounced and Cached) ---

	private void updateAutocompleteSuggestions(String input) {
		handler.removeCallbacksAndMessages(null); // Remove all pending callbacks

		handler.postDelayed(() -> { // Debounce
			if (webView == null) return;

			// Check the cache first
			List<String> cachedSuggestions = suggestionCache.get(input);
			if (cachedSuggestions != null) {
				updateAdapter(cachedSuggestions);
				return;
			}

			//TODO parse the userInput js code get the obj the user currently interacting eg:"document. " or "window. " give suggestions for the obj also handle cases like "if(document. " for (document." only get the current object the user interacting
			String obj = getCurrentObj(input);
			if (obj == null) {
				updateAdapter(new ArrayList<>());
				return;
			}
			String jsCode = "(function() { try { return Object.getOwnPropertyNames(" + obj + "); } catch (e) { return []; } })();";

			executeJavaScript(jsCode, result -> {
				List<String> suggestions = getSuggestions(result);
				// Cache the results
				suggestionCache.put(input, new ArrayList<>(suggestions)); // Store a copy
				updateAdapter(suggestions);

			});
		}, DEBOUNCE_DELAY);
	}

	String getCurrentObj(String code) {
		if (code == null || code.trim().isEmpty()) {
			return null; // Or an empty string if preferred
		}

		code = code.trim(); // Remove leading/trailing whitespace for consistency

		java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("([a-zA-Z0-9_\\[\\]]+(?:\\([^\\)]*\\))?)\\.$");  // Modified regex

		java.util.regex.Matcher matcher = pattern.matcher(code);

		if (matcher.find()) {
			return matcher.group(1); // Return the captured object
		}

		return null; // Return null if no object is found
	}

	private void updateAdapter(List<String> suggestions) {
		handler.post(() -> { // Back to UI thread
			adapter.clear();
			adapter.addAll(suggestions);
			adapter.notifyDataSetChanged();
			if (!isPopupShowing() && isFocused()) { // show dropdown only if focused
				showDropDown();
			}

		});
	}

	// --- Keyboard and Command Execution ---

	private void executeCurrentCommand() {
		String command = getText().toString();
		if (command.trim().isEmpty()) return;

		executeJavaScript(command, result -> {
			handler.post(() -> {
				Toast.makeText(getContext(), "Result: " + result, Toast.LENGTH_SHORT).show(); // Show Result
				setText(""); // Clear the input
			});
		});

		// Hide the keyboard
		InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(getWindowToken(), 0);
	}
}