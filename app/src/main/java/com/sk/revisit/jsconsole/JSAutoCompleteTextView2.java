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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JSAutoCompleteTextView2 extends AppCompatAutoCompleteTextView {

	private static final String TAG = "JSAutoCompleteTextView";
	private static final long DEBOUNCE_DELAY = 300;
	private static final int MAX_CACHE_SIZE = 50;
	private static final Set<String> JS_KEYWORDS = new HashSet<>(
			List.of("var", "let", "const", "function", "if", "else", "for", "while", "return",
					"try", "catch", "finally", "switch", "case", "break", "default", "new",
					"this", "typeof", "instanceof", "delete", "void", "debugger"));
	private static final Pattern OBJECT_PATTERN = Pattern.compile("([a-zA-Z0-9_\\[\\]]+(?:\\([^\\)]*\\))?)\\.$");
	private final Handler handler = new Handler(Looper.getMainLooper());
	private final ArrayAdapter<String> adapter;
	private final ExecutorService executor = Executors.newSingleThreadExecutor();
	private final LruCache<String, List<String>> suggestionCache = new LruCache<>(MAX_CACHE_SIZE);
	private WebView webView;
	private String userInput = "";

	public JSAutoCompleteTextView2(Context context) {
		this(context, null);
	}

	public JSAutoCompleteTextView2(Context context, AttributeSet attrs) {
		this(context, attrs, androidx.appcompat.R.attr.autoCompleteTextViewStyle);
	}

	public JSAutoCompleteTextView2(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
		setAdapter(adapter);
		initTextChangedListener();
		initEditorActionListener();
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		shutdownExecutor();
		webView = null;
	}

	public void setWebView(WebView webView) {
		this.webView = webView;
		if (webView == null) {
			throw new IllegalArgumentException("WebView cannot be null.");
		}
	}

	private void initTextChangedListener() {
		addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				userInput = s.toString();
				updateAutocompleteSuggestions(s.toString());
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
	}

	private void initEditorActionListener() {
		setOnEditorActionListener((v, actionId, event) -> {
			if (event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
				executeCurrentCommand();
				return true;
			}
			return false;
		});
	}

	private void shutdownExecutor() {
		executor.shutdownNow();
		try {
			if (!executor.awaitTermination(500, java.util.concurrent.TimeUnit.MILLISECONDS)) {
				Log.w(TAG, "Executor shutdown timed out.");
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	private void updateAutocompleteSuggestions(String input) {
		handler.removeCallbacksAndMessages(null);
		handler.postDelayed(() -> {
			if (webView == null) return;

			List<String> cachedSuggestions = suggestionCache.get(input);
			if (cachedSuggestions != null) {
				updateAdapter(cachedSuggestions);
				return;
			}

			String obj = getCurrentObj(input);
			if (obj == null) {
				updateAdapter(new ArrayList<>());
				return;
			}

			String jsCode = "(function() { try { return Object.getOwnPropertyNames(" + obj + "); } catch (e) { return []; } })();";
			executeJavaScript(jsCode, result -> {
				List<String> suggestions = getSuggestions(result);
				suggestionCache.put(input, new ArrayList<>(suggestions));
				updateAdapter(suggestions);
			});
		}, DEBOUNCE_DELAY);
	}

	private String getCurrentObj(String code) {
		if (code == null || code.trim().isEmpty()) {
			return null;
		}

		Matcher matcher = OBJECT_PATTERN.matcher(code.trim());
		if (matcher.find()) {
			return matcher.group(1);
		}

		return null;
	}

	private void updateAdapter(List<String> suggestions) {
		handler.post(() -> {
			adapter.clear();
			adapter.addAll(suggestions);
			adapter.notifyDataSetChanged();
			if (!isPopupShowing() && isFocused()) {
				showDropDown();
			}
		});
	}

	private void executeCurrentCommand() {
		String command = getText().toString();
		if (command.trim().isEmpty()) return;

		executeJavaScript(command, result -> handler.post(() -> {
			Toast.makeText(getContext(), "Result: " + result, Toast.LENGTH_SHORT).show();
			setText("");
		}));

		hideKeyboard();
	}

	private void hideKeyboard() {
		InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null) {
			imm.hideSoftInputFromWindow(getWindowToken(), 0);
		}
	}

	private void executeJavaScript(String jsCode, java.util.function.Consumer<String> callback) {
		if (webView == null) {
			Log.e(TAG, "WebView is null, cannot execute JavaScript.");
			handler.post(() -> Toast.makeText(getContext(), "WebView is not initialized.", Toast.LENGTH_SHORT).show());
			return;
		}

		CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
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
					handler.post(() -> Toast.makeText(getContext(), "JavaScript evaluation failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
				}
			});

			synchronized (lock) {
				try {
					if (!completed[0]) {
						lock.wait(500);
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					return null;
				}
			}
			return result[0];

		}, executor);

		future.thenAccept(callback);
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

		for (String keyword : JS_KEYWORDS) {
			if (keyword.toLowerCase().startsWith(userInput.toLowerCase())) {
				suggestions.add(keyword);
			}
		}

		return suggestions;
	}
}
