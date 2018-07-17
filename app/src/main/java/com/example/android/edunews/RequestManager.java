package com.example.android.edunews;

import android.text.TextUtils;
import android.util.Log;

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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static com.example.android.edunews.NewsActivity.LOG_TAG;


public final class RequestManager {

    private static final String PUBLICATION_DATE_AND_TIME_SEPARATOR = "T";
    private static final String TITLE_PLUS_OPTIONAL_AUTHOR_OR_SOURCE_SEPARATOR = "|";
    private static final String PUBLICATION_DATE_PARTS_SEPARATOR = "-";

    private static final int TITLE_STRING_TITLE_PART_INDEX = 0;
    private static final int TITLE_STRING_OPTIONAL_AUTHOR_OR_SOURCE_PART_INDEX = 1;

    private static final int ORIGINAL_PUBLICATION_TIME_INDEX_OF_DATE_PART = 0;

    private static final int TRIMMED_PUBLICATION_DATE_LIST_YEAR_INDEX = 0;
    private static final int TRIMMED_PUBLICATION_DATE_LIST_MONTH_INDEX = 1;
    private static final int TRIMMED_PUBLICATION_DATE_LIST_DAY_INDEX = 2;

    private RequestManager() {
    }

    private static List<News> extractNewsFromJson(String newsJSON) {
        if (TextUtils.isEmpty(newsJSON)) {
            return null;
        }

        List<News> newsArticles = new ArrayList<>();

        try {
            JSONObject baseJsonResponse = new JSONObject(newsJSON);

            JSONObject jsonObjectWithinBaseResponse = baseJsonResponse.getJSONObject("response");
            JSONArray newsArray = jsonObjectWithinBaseResponse.getJSONArray("results");

            for (int i = 0; i < newsArray.length(); i++) {

                JSONObject currentNews = newsArray.getJSONObject(i);

                String title = currentNews.getString("webTitle");
                String section = currentNews.getString("sectionName");
                String publicationDate = americanizeDateFormat(trimPublicationTimeToDateOnly(currentNews.getString("webPublicationDate")));
                String url = currentNews.getString("webUrl");

                if (authorOrSourceExistsAtEndOfTitle(title)) {
                    newsArticles.add(new News(returnTitleWithoutAuthorOrSourceAtItsEnd(title), section, publicationDate, url, getAuthorOrSourceFromEndOfTitle(title)));
                } else {
                    newsArticles.add(new News(title, section, publicationDate, url));
                }
            }
        } catch (JSONException e) {
            Log.e("RequestManager", "Problem parsing news JSON results", e);
        }

        return newsArticles;
    }


    public static List<News> fetchNewsData(String requestUrl) {
        URL url = createUrlFromString(requestUrl);
        String jsonResponse = null;

        try {
            jsonResponse = makeHttpGetRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making HTTP request.", e);
        }

        return extractNewsFromJson(jsonResponse);
    }


    private static URL createUrlFromString(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem creating URL", e);
        }
        return url;
    }

    private static String makeHttpGetRequest(URL url) throws IOException {
        String jsonResponse = "";

        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection httpUrlConnection = null;
        InputStream inputStream = null;
        try {
            httpUrlConnection = (HttpURLConnection) url.openConnection();
            httpUrlConnection.setRequestMethod("GET");
            httpUrlConnection.setReadTimeout(10000);
            httpUrlConnection.setConnectTimeout(15000);
            httpUrlConnection.connect();

            if (httpUrlConnection.getResponseCode() == 200) {
                inputStream = httpUrlConnection.getInputStream();
                jsonResponse = readFromInputStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + httpUrlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving JSON results.", e);
        } finally {
            if (httpUrlConnection != null) {
                httpUrlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    private static String readFromInputStream(InputStream inputStream) throws IOException {
        StringBuilder outputExtractedFromStream = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                outputExtractedFromStream.append(line);
                line = reader.readLine();
            }
        }
        return outputExtractedFromStream.toString();
    }

    private static String getAuthorOrSourceFromEndOfTitle(String title) {
        String[] titlePlusAuthorOrSourceParts = title.split(Pattern.quote(TITLE_PLUS_OPTIONAL_AUTHOR_OR_SOURCE_SEPARATOR));

        return titlePlusAuthorOrSourceParts[TITLE_STRING_OPTIONAL_AUTHOR_OR_SOURCE_PART_INDEX];
    }

    private static String returnTitleWithoutAuthorOrSourceAtItsEnd(String title) {
        String[] titlePlusAuthorOrSourceParts = title.split(Pattern.quote(TITLE_PLUS_OPTIONAL_AUTHOR_OR_SOURCE_SEPARATOR));

        return titlePlusAuthorOrSourceParts[TITLE_STRING_TITLE_PART_INDEX];
    }

    private static boolean authorOrSourceExistsAtEndOfTitle(String title) {
        return (title.contains(TITLE_PLUS_OPTIONAL_AUTHOR_OR_SOURCE_SEPARATOR));
    }

    private static String trimPublicationTimeToDateOnly(String publicationDateAndTime) {
        String publicationDate;

        if (publicationDateAndTime.contains(PUBLICATION_DATE_AND_TIME_SEPARATOR)) {
            String[] publicationDateAndTimeParts = publicationDateAndTime.split(PUBLICATION_DATE_AND_TIME_SEPARATOR);
            publicationDate = publicationDateAndTimeParts[ORIGINAL_PUBLICATION_TIME_INDEX_OF_DATE_PART];
        } else {
            publicationDate = publicationDateAndTime;
        }
        return publicationDate;
    }

    private static String americanizeDateFormat(String publicationTimeTrimmedToDateOnly) {
        String[] dateParts = publicationTimeTrimmedToDateOnly.split(Pattern.quote(PUBLICATION_DATE_PARTS_SEPARATOR));

        String[] americanizedDateParts = {displayMonthWithNounInsteadOfNumber(dateParts[TRIMMED_PUBLICATION_DATE_LIST_MONTH_INDEX]),
                PUBLICATION_DATE_PARTS_SEPARATOR,
                dateParts[TRIMMED_PUBLICATION_DATE_LIST_DAY_INDEX],
                PUBLICATION_DATE_PARTS_SEPARATOR,
                dateParts[TRIMMED_PUBLICATION_DATE_LIST_YEAR_INDEX]};

        String americanizedDate = "";
        for (String americanizedDatePiece : americanizedDateParts) {
            americanizedDate += americanizedDatePiece;
        }

        return americanizedDate;
    }

    private static String displayMonthWithNounInsteadOfNumber(String publicationMonth) {
        switch (publicationMonth) {
            case "01":
                publicationMonth = "Jan";
                break;
            case "02":
                publicationMonth = "Feb";
                break;
            case "03":
                publicationMonth = "Mar";
                break;
            case "04":
                publicationMonth = "Apr";
                break;
            case "05":
                publicationMonth = "May";
                break;
            case "06":
                publicationMonth = "June";
                break;
            case "07":
                publicationMonth = "July";
                break;
            case "08":
                publicationMonth = "Aug";
                break;
            case "09":
                publicationMonth = "Sept";
                break;
            case "10":
                publicationMonth = "Oct";
                break;
            case "11":
                publicationMonth = "Nov";
                break;
            case "12":
                publicationMonth = "Dec";
                break;
            default:
                return publicationMonth;
        }
        return publicationMonth;
    }
}
