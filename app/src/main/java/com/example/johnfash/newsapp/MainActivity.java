package com.example.johnfash.newsapp;

import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<ArrayList<Model>> {
    public static final String LOG_TAG = MainActivity.class.getName();
    private static final int NEWS_LOADER_ID = 1;
    public MyAdapter mAdapter;
    private Uri.Builder builder = new Uri.Builder();
    private final String NEWS_REQUEST_URL = getBuilder().build().toString();
    private RecyclerView recyclerView;
    private TextView EmptyTv;
    private ProgressBar progressBar;

    private Uri.Builder getBuilder() {
        builder.scheme("https").authority("content.guardianapis.com").appendPath("search")
                .appendQueryParameter("order-by", "newest")
                .appendQueryParameter("page-size", "50")
                .appendQueryParameter("show-references", "author")
                .appendQueryParameter("api-key", "test");
        return builder;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.newsList);
        EmptyTv = findViewById(R.id.empty_view);
        progressBar = findViewById(R.id.progress_bar);
        LoaderManager loaderManager = getLoaderManager();
        loaderManager.initLoader(NEWS_LOADER_ID, null, this);
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            EmptyTv.setVisibility(View.GONE);
        } else {
            EmptyTv.setText(R.string.no_network);
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public Loader<ArrayList<Model>> onCreateLoader(int id, Bundle args) {
        return new AsynNews(this, NEWS_REQUEST_URL);
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<Model>> loader, final ArrayList<Model> news) {
        if (news == null) {
            return;
        } else {
            EmptyTv.setVisibility(View.GONE);
            mAdapter = new MyAdapter(getApplicationContext(), news);
            mAdapter.setOnItemClickListener(new MyAdapter.ClickListener() {
                @Override
                public void onItemClick(int position, View v) {
                    String url = news.get(position).getNewsUrl();
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                }
            });
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(), LinearLayoutManager.VERTICAL));
            recyclerView.setAdapter(mAdapter);
            ConnectivityManager cm =
                    (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                progressBar.setVisibility(View.GONE);
            } else {
                EmptyTv.setText(R.string.no_network);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<Model>> loader) {
        recyclerView.removeAllViewsInLayout();
        recyclerView.setAdapter(null);
        mAdapter.notifyDataSetChanged();
    }

    private static class AsynNews extends AsyncTaskLoader<ArrayList<Model>> {
        private String mUrl;

        public AsynNews(Context context, String url) {
            super(context);
            mUrl = url;
        }

        @Override
        protected void onStartLoading() {
            forceLoad();
        }

        @Override
        public ArrayList<Model> loadInBackground() {
            if (mUrl == null) {
                return null;
            }
            URL url = createUrl(mUrl);
            String jsonResponse = "";
            try {
                jsonResponse = makeHttpRequest(url);
            } catch (IOException e) {
            }
            ArrayList<Model> news = extractFeatureFromJson(jsonResponse);
            return news;
        }

        private URL createUrl(String stringUrl) {
            URL url = null;
            try {
                url = new URL(stringUrl);
            } catch (MalformedURLException exception) {
                return null;
            }
            return url;
        }

        private ArrayList<Model> extractFeatureFromJson(String newsJSON) {
            ArrayList<Model> news = new ArrayList<>();

            if (TextUtils.isEmpty(newsJSON)) {
                return null;
            }
            try {
                JSONObject jsonObj = new JSONObject(newsJSON);
                JSONObject response = jsonObj.getJSONObject("response");
                JSONArray results = response.getJSONArray("results");
                if (results.length() > 0) {
                    for (int i = 0; i < results.length(); i++) {
                        JSONObject c = results.getJSONObject(i);
                        JSONArray jA = c.getJSONArray("references");
                        String authorList = "";
                        if (jA.length() > 0) {
                            for (int a = 0; a < jA.length(); a++) {
                                JSONObject refObj = jA.getJSONObject(a);
                                String author = refObj.getString("id");
                                author = author.substring(author.lastIndexOf("/") + 1);
                                if (author.contains("-")) {
                                    String[] split = author.split("-");
                                    author = "";
                                    int splitAdd = 0;
                                    while (splitAdd < split.length) {
                                        author = author + split[splitAdd].substring(0, 1).toUpperCase() + split[splitAdd].substring(1) + " ";
                                        splitAdd++;
                                    }
                                    author = author.trim();
                                } else {
                                    author = author.substring(0, 1).toUpperCase() + author.substring(1);
                                }
                                authorList += author + ", ";
                            }
                            authorList = authorList.substring(0, authorList.lastIndexOf(","));
                        }

                        String sectionName = "#" + c.getString("sectionName");
                        String webPublicationDate = c.getString("webPublicationDate");
                        String webTitle = c.getString("webTitle");
                        String webUrl = c.getString("webUrl");

                        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                        SimpleDateFormat outputFormat = new SimpleDateFormat("d, MMM yyyy");
                        Date date = inputFormat.parse(webPublicationDate);
                        String niceDateStr = (String) DateUtils.getRelativeTimeSpanString(date.getTime(), Calendar.getInstance().getTimeInMillis(), DateUtils.MINUTE_IN_MILLIS);
                        String formattedDate = outputFormat.format(date);
                        news.add(new Model(sectionName, webTitle, formattedDate, niceDateStr, webUrl, authorList));
                    }
                }
            } catch (JSONException e) {

            } catch (ParseException e) {
                e.printStackTrace();
            }
            return news;
        }

        private String makeHttpRequest(URL url) throws IOException {
            String jsonResponse = "";
            if (url == null) {
                return jsonResponse;
            }
            HttpURLConnection urlConnection = null;
            InputStream inputStream = null;
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setReadTimeout(10000 /* milliseconds */);
                urlConnection.setConnectTimeout(15000 /* milliseconds */);
                urlConnection.connect();
                if (urlConnection.getResponseCode() == 200) {
                    inputStream = urlConnection.getInputStream();
                    jsonResponse = readFromStream(inputStream);
                } else {
                    return jsonResponse;
                }
            } catch (IOException e) {
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            }
            return jsonResponse;
        }

        private String readFromStream(InputStream inputStream) throws IOException {
            StringBuilder output = new StringBuilder();
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line = reader.readLine();
                while (line != null) {
                    output.append(line);
                    line = reader.readLine();
                }
            }
            return output.toString();
        }

    }
}

