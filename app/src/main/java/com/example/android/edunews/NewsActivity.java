package com.example.android.edunews;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class NewsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<News>> {

    public static final String LOG_TAG = NewsActivity.class.getName();
    private static final int NEWS_LOADER_ID = 1;
    private static final String GUARDIAN_API_URL = "https://content.guardianapis.com/search";
    private static final String MAX_PAGE_SIZE_SET_BY_GUARDIAN = "200";

    private TextView noNewsOrNoInternetTextView;
    private NewsAdapter newsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        ListView newsListView = (ListView) findViewById(R.id.list);

        noNewsOrNoInternetTextView = (TextView) findViewById(R.id.no_news_or_no_internet_view);
        newsListView.setEmptyView(noNewsOrNoInternetTextView);

        newsAdapter = new NewsAdapter(this, new ArrayList<News>());
        newsListView.setAdapter(newsAdapter);

        newsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                News currentNews = newsAdapter.getItem(position);
                try {
                    Uri newsUri = Uri.parse(currentNews.getUrl());
                    Intent givenNewsWebsiteIntent = new Intent(Intent.ACTION_VIEW, newsUri);
                    startActivity(givenNewsWebsiteIntent);
                } catch (NullPointerException nullpointerException) {
                    Log.e(LOG_TAG, "No Url found");
                }
            }
        });

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected()) {
            LoaderManager loaderManager = getLoaderManager();
            loaderManager.initLoader(NEWS_LOADER_ID, null, this);
        } else {
            View progressIndicator = findViewById(R.id.progress_indicator);
            progressIndicator.setVisibility(View.GONE);
            noNewsOrNoInternetTextView.setText(R.string.no_internet_connection);
        }
    }

    @Override
    public Loader<List<News>> onCreateLoader(int i, Bundle bundle) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        String chosenYear = sharedPrefs.getString(
                getString(R.string.settings_year_key),
                getString(R.string.settings_year_default)
        );

        String chosenSection = sharedPrefs.getString(
                getString(R.string.settings_section_key),
                getString(R.string.settings_section_default)
        );

        String orderBy = sharedPrefs.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default)
        );

        Uri baseUri = Uri.parse(GUARDIAN_API_URL);

        Uri.Builder uriBuilder = baseUri.buildUpon();

        uriBuilder.appendQueryParameter("show-fields", "all");
        uriBuilder.appendQueryParameter("section", chosenSection);
        uriBuilder.appendQueryParameter("order-by", orderBy);
        uriBuilder.appendQueryParameter("from-date", chosenYear + "-01-01");

        uriBuilder.appendQueryParameter("page-size", MAX_PAGE_SIZE_SET_BY_GUARDIAN);
        uriBuilder.appendQueryParameter("api-key", "d851c653-799c-4661-a220-68f7661653c6");

        return new NewsLoader(this, uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(Loader<List<News>> loader, List<News> newsArticles) {
        View progressIndicator = findViewById(R.id.progress_indicator);
        progressIndicator.setVisibility(View.GONE);
        noNewsOrNoInternetTextView.setText(R.string.no_news);
        newsAdapter.clear();

        if (newsArticles != null && !newsArticles.isEmpty()) {
            newsAdapter.addAll(newsArticles);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<News>> loader) {
        newsAdapter.clear();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
