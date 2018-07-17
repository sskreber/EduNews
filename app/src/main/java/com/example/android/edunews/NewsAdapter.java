package com.example.android.edunews;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class NewsAdapter extends ArrayAdapter<News> {

    protected NewsAdapter(Context context, List<News> newsArticles) {
        super(context, 0, newsArticles);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.news_list_item, parent, false);
        }

        News currentNews = getItem(position);

        // TITLE
        TextView titleView = (TextView) listItemView.findViewById(R.id.title);
        titleView.setText(currentNews.getTitle());

        // SECTION
        TextView sectionView = (TextView) listItemView.findViewById(R.id.section);
        sectionView.setText(currentNews.getSection());

        // PUBLICATION DATE
        TextView publicationDateView = (TextView) listItemView.findViewById(R.id.publication_date);
        publicationDateView.setText(currentNews.getPublicationDate());

        // AUTHOR OR SOURCE (e.g. Editorial)
        TextView authorOrSourceView = (TextView) listItemView.findViewById(R.id.author_or_source);
        if (currentNews.getAuthorOrSource() != null) {
            authorOrSourceView.setText(currentNews.getAuthorOrSource());
        } else {
            authorOrSourceView.setVisibility(View.GONE);
        }
        return listItemView;
    }
}
