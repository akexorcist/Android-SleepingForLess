package com.akexorcist.sleepingforless.view.search;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.akexorcist.sleepingforless.R;
import com.akexorcist.sleepingforless.common.SFLActivity;
import com.akexorcist.sleepingforless.constant.Key;
import com.akexorcist.sleepingforless.network.BloggerManager;
import com.akexorcist.sleepingforless.network.model.PostList;
import com.akexorcist.sleepingforless.util.AnimationUtility;
import com.akexorcist.sleepingforless.util.ContentUtility;
import com.akexorcist.sleepingforless.view.feed.FeedAdapter;
import com.akexorcist.sleepingforless.view.feed.FeedViewHolder;
import com.akexorcist.sleepingforless.view.post.DebugPostActivity;
import com.akexorcist.sleepingforless.view.post.PostActivity;
import com.bowyer.app.fabtransitionlayout.FooterLayout;
import com.squareup.otto.Subscribe;
import com.zl.reik.dilatingdotsprogressbar.DilatingDotsProgressBar;

import org.parceler.Parcels;

public class SearchResultActivity extends SFLActivity implements View.OnClickListener, FeedAdapter.ItemListener, FeedAdapter.LoadMoreListener {
    private Toolbar tbTitle;
    private FloatingActionButton fabMenu;
    private DilatingDotsProgressBar pbSearchResultList;
    private RecyclerView rvSearchResultList;
    private View viewContentShadow;

    private FeedAdapter adapter;
    private PostList postList;
    private SearchRequest request;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);

        if (savedInstanceState == null) {
            restoreIntentData();
        }

        bindView();
        setupView();
        setToolbar();
        callService();
    }

    public void callService() {
        showLoading();
        searchPost(request.getKeyword());
    }

    private void bindView() {
        pbSearchResultList = (DilatingDotsProgressBar) findViewById(R.id.pb_search_result_list_loading);
        rvSearchResultList = (RecyclerView) findViewById(R.id.rv_search_result_list);
        viewContentShadow = findViewById(R.id.view_content_shadow);
        tbTitle = (Toolbar) findViewById(R.id.tb_title);
        fabMenu = (FloatingActionButton) findViewById(R.id.fab_menu);
    }

    private void setupView() {
        viewContentShadow.setVisibility(View.GONE);
        viewContentShadow.setOnClickListener(this);
        fabMenu.setOnClickListener(this);
        adapter = new FeedAdapter();
        adapter.setItemListener(this);
        adapter.setLoadMoreListener(this);
        rvSearchResultList.setLayoutManager(new LinearLayoutManager(this));
        rvSearchResultList.setAdapter(adapter);
    }

    private void setToolbar() {
        setSupportActionBar(tbTitle);
        setTitle(ContentUtility.getInstance().removeLabelFromTitle("Search Result : " + request.getKeyword()));
        tbTitle.setNavigationIcon(R.drawable.vector_ic_back);
        tbTitle.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void restoreIntentData() {
        request = Parcels.unwrap(getIntent().getParcelableExtra(Key.SEARCH_REQUEST));
    }

    private void searchPost(String keyword) {
        BloggerManager.getInstance().searchPost(keyword);
    }

    private void searchMorePost(String nextPageToken) {
        BloggerManager.getInstance().getNextPostList(nextPageToken, false);
    }

    @Subscribe
    public void onPostListSuccess(PostList postList) {
        this.postList = postList;
        setPostList(postList);
        hideLoading();
    }

    @Subscribe
    public void onSearchRequest(SearchRequest request) {
        this.request = request;
        callService();
    }

    @Override
    public void onClick(View v) {
        if (v == fabMenu) {
            onMenuSearchClick();
        }
    }

    @Override
    public void onItemClick(FeedViewHolder holder, PostList.Item item) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(Key.POST_ID, Parcels.wrap(item));
        openActivity(PostActivity.class, bundle);
    }

    @Override
    public void onItemLongClick(FeedViewHolder holder, PostList.Item item) {
//        showBottomSheet();
        Bundle bundle = new Bundle();
        bundle.putParcelable(Key.POST_ID, Parcels.wrap(item));
        openActivity(DebugPostActivity.class, bundle);
    }

    @Override
    public void onLoadMore() {
        searchMorePost(postList.getNextPageToken());
    }

    public void setPostList(PostList postList) {
        if (postList != null) {
            adapter.addPostListItem(postList.getItems());
//            adapter.setLoadMoreAvailable(postList.getNextPageToken() != null);
            adapter.setLoadMoreAvailable(false);
        }
    }

    public void onMenuSearchClick() {
        openActivity(SearchActivity.class);
    }

    private void showLoading() {
        rvSearchResultList.setVisibility(View.GONE);
        pbSearchResultList.showNow();
    }

    private void hideLoading() {
        rvSearchResultList.setVisibility(View.VISIBLE);
        pbSearchResultList.hideNow();
    }

}
