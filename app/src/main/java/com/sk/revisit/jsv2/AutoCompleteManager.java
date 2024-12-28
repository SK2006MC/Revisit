// AutoCompleteManager.java
package com.sk.revisit.jsv2;

import android.util.Log;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Manages autocomplete suggestions for JavaScript code.
 */
public class AutoCompleteManager {
    private static final String TAG = "AutoCompleteManager";

    private static final Set<String> JS_KEYWORDS = new HashSet<>(
            List.of("var", "let", "const", "function", "if", "else", "for", "while", "return",
                    "try", "catch", "finally", "switch", "case", "break", "default", "new",
                    "this", "typeof", "instanceof", "delete", "void", "debugger"));

    /**
     * Parses JavaScript response and extracts autocomplete suggestions.
     *
     * @param result    The JavaScript execution result.
     * @param userInput The current user input.
     * @return List of suggestions.
     */
    public List<String> getSuggestions(String result, String userInput) {
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
}