package org.mozilla.reference.browser.assist;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.mozilla.reference.browser.IntentReceiverActivity;
import org.mozilla.reference.browser.R;

import java.net.MalformedURLException;
import java.net.URL;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static android.content.ClipDescription.MIMETYPE_TEXT_HTML;
import static android.content.ClipDescription.MIMETYPE_TEXT_PLAIN;

public class Assist extends Activity {
    private static final String LOGTAG = "QwantAssist";

    public static final int MAX_SUGGEST_TEXT_LENGTH = 30;

    AutoCompleteTextView search_text;
    WebView webview;
    TextView clipboard_text;
    LinearLayout home_layout;
    LinearLayout clipboard_layout;
    SuggestAdapter suggest_adapter;
    HistoryAdapter history_adapter;
    LinearLayout history_layout;
    RecyclerView history_list;
    Intent new_tab_intent;
    CharSequence clipboard_full_text;
    boolean clipboard_is_url = false;

    // Geoloc permission
    final int QWANT_PERMISSIONS_REQUEST_FINE_LOCATION = 0;
    private String permission_request_origin;
    private GeolocationPermissions.Callback permission_request_callback;

    @SuppressLint("SetJavaScriptEnabled")
    @Override protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOGTAG, "OnCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.assist_main);

        search_text = findViewById(R.id.search_text);
        home_layout = findViewById(R.id.home_layout);

        history_list = findViewById(R.id.history_list);
        history_layout = findViewById(R.id.history_layout);
        history_adapter = new HistoryAdapter(this, history_layout);
        history_list.setLayoutManager(new LinearLayoutManager(this));
        TextView link_erase_history = findViewById(R.id.link_erase_history);
        link_erase_history.setOnClickListener(v -> history_adapter.clear_history());
        history_list.setAdapter(history_adapter);

        suggest_adapter = new SuggestAdapter(this, R.layout.assist_suggestlist_item, history_adapter);

        // Intent for opening url in browser
        new_tab_intent = new Intent(this, IntentReceiverActivity.class);
        new_tab_intent.setPackage(getPackageName());
        new_tab_intent.setAction(Intent.ACTION_VIEW);

        webview = findViewById(R.id.webview);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setUserAgentString("Mozilla/5.0 (Android; Mobile) Gecko/68.0 Firefox/68.0.2 QwantMobile/3.4");

        // MAPS SETTINGS
        webview.getSettings().setGeolocationEnabled(true);
        webview.getSettings().setBuiltInZoomControls(true);
        // local storage emulation
        webview.getSettings().setAppCacheEnabled(true);
        webview.getSettings().setDatabaseEnabled(true);
        webview.getSettings().setDomStorageEnabled(true);

        webview.setWebViewClient(new WebViewClient() {
            // If we get out of qwant.com, it opens in the browser, else stay in webview
            @Override public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Uri uri = Uri.parse(url);
                String host = uri.getHost();
                if (host != null && host.contains("qwant.com")) {
                    return false;
                }
                new_tab_intent.setData(uri);
                startActivity(new_tab_intent);
                return true;
            }
            // Show webview and hide home on first user request
            @Override public void onPageFinished(WebView view, String url) {
                if (webview.getVisibility() == View.INVISIBLE /* && !webview.getUrl().contains("preload") */) {
                    webview.setVisibility(View.VISIBLE);
                    Log.d("QwantFocus", "Clearing focus");
                    search_text.clearFocus();
                    search_text.dismissDropDown();
                    webview.requestFocus();
                    home_layout.setVisibility(View.INVISIBLE);
                }
            }
        });
        webview.setWebChromeClient(new WebChromeClient() {
            // Geoloc permission prompt for maps
            public void onGeolocationPermissionsShowPrompt(final String origin, final GeolocationPermissions.Callback callback) {
                permission_request_origin = null;
                permission_request_callback = null;
                final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                Log.d(LOGTAG, "test GPS");
                if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    Log.d(LOGTAG, "GPS disabled");
                    final AlertDialog.Builder builder = new AlertDialog.Builder(Assist.this);
                    builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", (dialog, id) -> startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
                        .setNegativeButton("No", (dialog, id) -> dialog.cancel());
                    final AlertDialog alert = builder.create();
                    alert.show();
                    callback.invoke(origin, false, false);
                } else {
                    Log.d(LOGTAG, "GPS ok, check permission");
                    if (ContextCompat.checkSelfPermission(Assist.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        Log.d(LOGTAG, "permission not ok");
                        if (ActivityCompat.shouldShowRequestPermissionRationale(Assist.this, Manifest.permission.READ_CONTACTS)) {
                            Log.d(LOGTAG, "permission show rationale");
                            new AlertDialog.Builder(Assist.this)
                                .setMessage("We can not provide location without this permission")
                                .setNeutralButton("Understood ...", (dialogInterface, i) -> {
                                    permission_request_origin = origin;
                                    permission_request_callback = callback;
                                    ActivityCompat.requestPermissions(Assist.this, new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, QWANT_PERMISSIONS_REQUEST_FINE_LOCATION);
                                }).show();
                        } else {
                            Log.d(LOGTAG, "request permission");
                            permission_request_origin = origin;
                            permission_request_callback = callback;
                            ActivityCompat.requestPermissions(Assist.this, new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, 0);
                        }
                    } else {
                        Log.d(LOGTAG, "permission ok");
                        callback.invoke(origin, true, true);
                    }
                }
            }
        });
        // Hide webview and preload SERP for speed of next user request (cache)
        webview.setVisibility(View.INVISIBLE);
        // webview.loadUrl("https://www.qwant.com/?widget=1&q=a&preload=true");

        ImageView cancel_cross = findViewById(R.id.widget_search_bar_cross);
        cancel_cross.setVisibility(View.INVISIBLE);
        cancel_cross.setOnClickListener((e) -> {
            reset_searchbar();
        });

        ImageView img_magnifier = findViewById(R.id.widget_search_bar_magnifier);
        img_magnifier.setOnClickListener((e) -> launch_search());

        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard_layout = findViewById(R.id.clipboard_layout);
        clipboard_text = findViewById(R.id.clipboard_text);
        clipboard.addPrimaryClipChangedListener(() -> reload_clipboard(clipboard));
        reload_clipboard(clipboard);
        LinearLayout clipboard_text_layout = findViewById(R.id.clipboard_text_layout);
        clipboard_text_layout.setOnClickListener(v -> {
            if (clipboard_is_url) {
                new_tab_intent.setData(Uri.parse(clipboard_full_text.toString()));
                startActivity(new_tab_intent);
            } else {
                search_text.setText(clipboard_full_text);
                search_text.setSelection(search_text.getText().length());
                launch_search();
            }
        });

        search_text.setAdapter(suggest_adapter);
        search_text.setDropDownBackgroundResource(R.drawable.white_rectangle);
        // On keyboard validation (button "enter")
        search_text.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                launch_search();
                return true;
            }
            return false;
        });
        // On click on suggest item
        search_text.setOnItemClickListener((adapter, view, position, id) -> {
            SuggestItem selected_item = suggest_adapter.getItem(position);
            if (selected_item != null) {
                search_text.setText(selected_item.display_text);
                search_text.setSelection(search_text.getText().length());
                launch_search();
            }
        });
        search_text.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count == 0) {
                    cancel_cross.setVisibility(View.INVISIBLE);
                    webview.setVisibility(View.INVISIBLE);
                    home_layout.setVisibility(View.VISIBLE);
                } else {
                    cancel_cross.setVisibility(View.VISIBLE);
                }
            }
        });
        search_text.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                if (search_text.getText().length() == 0) {
                    webview.setVisibility(View.INVISIBLE);
                    home_layout.setVisibility(View.VISIBLE);
                } else {
                    search_text.showDropDown();
                }
            } else {
                search_text.dismissDropDown();
            }
        });
        reset_searchbar();
    }

    // Geoloc permission callback
    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == QWANT_PERMISSIONS_REQUEST_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(LOGTAG, "permission granted");
                permission_request_callback.invoke(permission_request_origin, true, true);
            } else {
                Log.d(LOGTAG, "permission refused");
                permission_request_callback.invoke(permission_request_origin, false, false);
            }
        } else {
            Log.e(LOGTAG, "Rejecting invalid RequestPermissionResult with unknown code: " + requestCode);
            permission_request_callback.invoke(permission_request_origin, false, false);
        }
    }

    // We reset the widget when user comes from a click on the homescreen widget
    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        reset_searchbar();
    }

    @Override public void onBackPressed() {
        Log.d(LOGTAG, "onBackPressed");
        if (home_layout.getVisibility() == View.INVISIBLE) {
            home_layout.setVisibility(View.VISIBLE);
            reset_searchbar();
        } else {
            super.onBackPressed();
        }
    }

    @Override protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(LOGTAG, "Save history");
        history_adapter.write_on_disk();
    }

    @Override protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(LOGTAG, "Restore history");
        history_adapter.read_from_disk();
    }

    void reset_searchbar() {
        search_text.setText("");
        search_text.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.showSoftInput(search_text, 0);
    }

    void reload_clipboard(ClipboardManager clipboard) {
        clipboard_is_url = false;
        if (clipboard.hasPrimaryClip() &&
        (clipboard.getPrimaryClipDescription().hasMimeType(MIMETYPE_TEXT_PLAIN) || clipboard.getPrimaryClipDescription().hasMimeType(MIMETYPE_TEXT_HTML))) {
            ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
            Log.d(LOGTAG, "clipboard description data: " + clipboard.getPrimaryClip().getDescription().toString());
            String clipboard_text = null;
            Uri clipboard_uri = item.getUri();
            if (clipboard_uri != null) {
                clipboard_is_url = true;
                clipboard_text = clipboard_uri.toString();
                Log.d(LOGTAG, "clipboard is url: " + clipboard_text);
            } else if (item.getText() != null) {
                clipboard_text = item.getText().toString().trim();
                try {
                    URL url = new URL(clipboard_text);
                    clipboard_is_url = true;
                    Log.d(LOGTAG, "clipboard is url: " + clipboard_text);
                } catch (MalformedURLException e) {
                    Log.d(LOGTAG, "clipboard is text: " + clipboard_text);
                }
            }

            if (clipboard_text != null && clipboard_text.length() > 0) {
                CharSequence display_text = (clipboard_text.length() > Assist.MAX_SUGGEST_TEXT_LENGTH) ?
                        clipboard_text.subSequence(0, Assist.MAX_SUGGEST_TEXT_LENGTH) : clipboard_text;
                this.clipboard_text.setText(display_text);
                this.clipboard_full_text = clipboard_text;
                this.clipboard_layout.setVisibility(View.VISIBLE);
                return ;
            }
        }
        // If we get there, no clipboard value is usable so we hide it
        Log.d(LOGTAG, "Final clipboard value is null");
        this.clipboard_layout.setVisibility(View.GONE);
    }

    void launch_search() {
        this.launch_search(search_text.getText().toString());
    }

    public void launch_search(String query) {
        search_text.setText(query);
        if (query.length() > 0) {
            // home_layout.requestFocus(); // While webview is loading. Webview take focus after load
            home_layout.setVisibility(View.INVISIBLE);
            webview.loadUrl(getString(R.string.homepage) + "?widget=1&q=" + query);
            // Force hide keyboard
            InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(search_text.getWindowToken(), 0);
            Log.d("QwantFocus", "Clearing focus");
            search_text.clearFocus();
            search_text.dismissDropDown();
            webview.requestFocus();
            // Record history
            history_adapter.add_history_item(query.trim());
        }
    }
}
