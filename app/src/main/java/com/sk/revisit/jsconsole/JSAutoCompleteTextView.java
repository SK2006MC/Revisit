package com.sk.revisit.jsconsole;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.StyleSpan;
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

public class JSAutoCompleteTextView extends AppCompatAutoCompleteTextView {

	private static final String TAG = "JSAutoCompleteTextView";
	private static final long DEBOUNCE_DELAY = 300;
	private static final int MAX_CACHE_SIZE = 50;
	private static final Set<String> JS_KEYWORDS = new HashSet<>(
			List.of("var", "let", "const", "function", "if", "else", "for", "while", "return",
					"try", "catch", "finally", "switch", "case", "break", "default", "new",
					"this", "typeof", "instanceof", "delete", "void", "debugger"));
	private static final Pattern OBJECT_PATTERN = Pattern.compile("([a-zA-Z0-9_\\[\\]]+(?:\\([^\\)]*\\))?)\\.$");
	private static final String PREFS_NAME = "jsconsole_prefs";
	private static final String HISTORY_KEY = "command_history";

	private final Handler handler = new Handler(Looper.getMainLooper());
	private final ExecutorService executor = Executors.newSingleThreadExecutor();
	private final LruCache<String, List<String>> suggestionCache = new LruCache<>(MAX_CACHE_SIZE);

	private final List<String> commandHistory = new ArrayList<>();
	private int historyIndex = -1;

	private ArrayAdapter<String> adapter;
	private WebView webView;
	private String userInput = "";

	private TextWatcher textWatcher;
	private OnEditorActionListener editorActionListener;

	public JSAutoCompleteTextView(Context context) {
		this(context, null);
	}

	public JSAutoCompleteTextView(Context context, AttributeSet attrs) {
		this(context, attrs, androidx.appcompat.R.attr.autoCompleteTextViewStyle);
	}

	public JSAutoCompleteTextView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_dropdown_item_1line, new ArrayList<>()) {
			@Override
			public android.view.View getView(int position, android.view.View convertView, android.view.ViewGroup parent) {
				android.view.View view = super.getView(position, convertView, parent);
				String suggestion = getItem(position);
				String prefix = getPropertyPrefix(userInput);
				if (suggestion != null && !prefix.isEmpty()) {
					int start = suggestion.toLowerCase().indexOf(prefix);
					if (start >= 0) {
						android.widget.TextView tv = (android.widget.TextView) view;
						SpannableString ss = new SpannableString(suggestion);
						ss.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start, start + prefix.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
						tv.setText(ss);
					}
				}
				return view;
			}
		};
		setAdapter(adapter);
		initTextChangedListener();
		initEditorActionListener();
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		executor.shutdownNow();
		webView = null;
		if (textWatcher != null) removeTextChangedListener(textWatcher);
		setOnEditorActionListener(null);
	}

	public void setWebView(WebView webView) {
		if (webView == null) throw new IllegalArgumentException("WebView cannot be null.");
		this.webView = webView;
	}

	private void initTextChangedListener() {
		textWatcher = new TextWatcher() {
			@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override public void onTextChanged(CharSequence s, int start, int before, int count) {
				userInput = s.toString();
				updateAutocompleteSuggestions(s.toString());
			}
			@Override public void afterTextChanged(Editable s) {}
		};
		addTextChangedListener(textWatcher);
	}

	private void initEditorActionListener() {
		editorActionListener = (v, actionId, event) -> {
			if (event != null) {
				if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
					executeCurrentCommand();
					return true;
				} else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
					showPreviousHistory();
					return true;
				} else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
					showNextHistory();
					return true;
				}
			}
			return false;
		};
		setOnEditorActionListener(editorActionListener);
	}

	private void updateAutocompleteSuggestions(String input) {
		handler.removeCallbacksAndMessages(null);
		handler.postDelayed(() -> {
			if (webView == null) return;
			String obj = getCurrentObj(input);
			if (obj == null) {
				updateAdapter(new ArrayList<>());
				return;
			}
			List<String> cachedSuggestions = suggestionCache.get(obj);
			if (cachedSuggestions != null) {
				updateAdapter(filterSuggestions(cachedSuggestions, input));
				return;
			}
			String jsCode = "(function() { try { return Object.getOwnPropertyNames(" + obj + "); } catch (e) { return []; } })();";
			runJavaScriptAsync(jsCode, result -> {
				List<String> suggestions = getSuggestions(result, input);
				suggestionCache.put(obj, new ArrayList<>(suggestions));
				updateAdapter(suggestions);
			});
		}, DEBOUNCE_DELAY);
	}

	private List<String> filterSuggestions(List<String> suggestions, String input) {
		String prefix = getPropertyPrefix(input);
		List<String> filtered = new ArrayList<>();
		for (String s : suggestions) {
			if (s.toLowerCase().startsWith(prefix)) filtered.add(s);
		}
		for (String keyword : JS_KEYWORDS) {
			if (keyword.toLowerCase().startsWith(prefix)) filtered.add(keyword);
		}
		return filtered;
	}

	private String getPropertyPrefix(String input) {
		int lastDot = input.lastIndexOf('.');
		if (lastDot != -1 && lastDot < input.length() - 1) {
			return input.substring(lastDot + 1).toLowerCase();
		}
		return input.toLowerCase();
	}

	private String getCurrentObj(String code) {
		if (code == null || code.trim().isEmpty()) return null;
		Matcher matcher = OBJECT_PATTERN.matcher(code.trim());
		if (matcher.find()) return matcher.group(1);
		return null;
	}

	private void updateAdapter(List<String> suggestions) {
		handler.post(() -> {
			adapter.clear();
			adapter.addAll(suggestions);
			adapter.notifyDataSetChanged();
			if (suggestions.size() > 0 && !isPopupShowing() && isFocused()) showDropDown();
		});
	}

	private void executeCurrentCommand() {
		String command = getText().toString();
		if (command.trim().isEmpty()) return;
		if (commandHistory.isEmpty() || !command.equals(commandHistory.get(commandHistory.size() - 1))) {
			commandHistory.add(command);
		}
		historyIndex = commandHistory.size();
		runJavaScriptAsync(command, result -> handler.post(() -> {
			Toast.makeText(getContext(), "Result: " + result, Toast.LENGTH_SHORT).show();
			setText("");
		}));
		hideKeyboard();
	}

	private void hideKeyboard() {
		InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null) imm.hideSoftInputFromWindow(getWindowToken(), 0);
	}

	private void showPreviousHistory() {
		if (commandHistory.isEmpty()) return;
		if (historyIndex > 0) historyIndex--;
		setText(commandHistory.get(historyIndex));
		setSelection(getText().length());
	}

	private void showNextHistory() {
		if (commandHistory.isEmpty()) return;
		if (historyIndex < commandHistory.size() - 1) {
			historyIndex++;
			setText(commandHistory.get(historyIndex));
			setSelection(getText().length());
		} else {
			historyIndex = commandHistory.size();
			setText("");
		}
	}

	private void runJavaScriptAsync(String jsCode, java.util.function.Consumer<String> callback) {
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
				}
			});
			synchronized (lock) {
				try {
					if (!completed[0]) lock.wait(500);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					return null;
				}
			}
			return result[0];
		}, executor);
		future.thenAccept(callback);
	}

	private List<String> getSuggestions(String result, String input) {
		List<String> suggestions = new ArrayList<>();
		String prefix = getPropertyPrefix(input);
		try {
			JSONArray jsArray = new JSONArray(result);
			for (int i = 0; i < jsArray.length(); i++) {
				String variable = jsArray.getString(i).trim();
				if (variable.toLowerCase().startsWith(prefix)) suggestions.add(variable);
			}
		} catch (Exception e) {
			Log.e(TAG, "Error parsing JavaScript response: " + e.getMessage());
		}
		for (String keyword : JS_KEYWORDS) {
			if (keyword.toLowerCase().startsWith(prefix)) suggestions.add(keyword);
		}
		return suggestions;
	}

	// --- Persistence and utility ---

	public void saveHistory() {
		Context ctx = getContext();
		StringBuilder sb = new StringBuilder();
		for (String cmd : commandHistory) {
			sb.append(cmd.replace("\n", "\\n")).append("\n");
		}
		ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
				.edit().putString(HISTORY_KEY, sb.toString()).apply();
	}

	public void loadHistory() {
		Context ctx = getContext();
		String saved = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
				.getString(HISTORY_KEY, "");
		if (saved != null && !saved.isEmpty()) {
			String[] lines = saved.split("\n");
			for (String line : lines) {
				if (!line.isEmpty()) commandHistory.add(line.replace("\\n", "\n"));
			}
			historyIndex = commandHistory.size();
		}
	}

	public void clearSuggestions() {
		handler.post(() -> {
			adapter.clear();
			adapter.notifyDataSetChanged();
			dismissDropDown();
		});
	}
}
