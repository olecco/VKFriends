package com.olecco.android.VKFriends.ui;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.olecco.android.VKFriends.R;
import com.olecco.android.VKFriends.images.ImageCache;
import com.olecco.android.VKFriends.images.ImageFetcher;
import com.olecco.android.VKFriends.vk.VKClient;
import com.olecco.android.VKFriends.vk.model.VKUser;
import com.olecco.android.VKFriends.vk.responses.VKResponse;
import com.olecco.android.VKFriends.vk.responses.VKUserListResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by olecco on 10.08.2014.
 */
public class UsersFragment extends Fragment {

    private static final int REFRESH_ITEM_ID = 1;
    private static final String IMAGE_CACHE_DIR = "thumbs";

    private ListView mUsersList;
    private View mMainProgress;
    private View mUsersEmptyView;
    private UsersAdapter mAdapter;
    private List<VKUser> mUsers = new ArrayList<VKUser>();
    private boolean mUsersLoaded;
    private boolean mRefreshed;

    private ImageFetcher mImageFetcher;

    private VKClient.VKApiListener mInitialLoadListener = new VKClient.VKApiListener() {
        @Override
        public void onMethodExecuted(VKResponse response) {
            mUsers = ((VKUserListResponse) response).getUsers();
            mAdapter.notifyDataSetChanged();
            mUsersLoaded = true;
            hideMainProgress();
        }

        @Override
        public void onError() {
            mUsersLoaded = true;
            hideMainProgress();
            Toast.makeText(getActivity(), R.string.request_error, Toast.LENGTH_SHORT).show();
        }
    };

    private AbsListView.OnScrollListener mUserListScrollListener = new AbsListView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (SCROLL_STATE_FLING == scrollState) {
                mImageFetcher.setPauseWork(true);
            }
            else {
                mImageFetcher.setPauseWork(false);
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) { }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        int imageThumbSize = getResources().getDimensionPixelSize(R.dimen.image_size);
        ImageCache.ImageCacheParams cacheParams =
                new ImageCache.ImageCacheParams(getActivity(), IMAGE_CACHE_DIR);
        cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of app memory

        mImageFetcher = new ImageFetcher(getActivity(), imageThumbSize);
        mImageFetcher.setLoadingImage(R.drawable.empty_image);
        mImageFetcher.addImageCache(getFragmentManager(), cacheParams);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.users_fragment, null);

        mUsersList = (ListView) view.findViewById(R.id.usersList);
        mMainProgress = view.findViewById(R.id.mainProgress);
        mUsersEmptyView = view.findViewById(R.id.usersEmpty);
        mUsersList.setEmptyView(mUsersEmptyView);
        mUsersList.setOnScrollListener(mUserListScrollListener);

        mAdapter = new UsersAdapter();
        mUsersList.setAdapter(mAdapter);

        if (!mRefreshed) {
            startRefresh();
        }

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.add(0, REFRESH_ITEM_ID, 0, R.string.refresh)
            .setIcon(R.drawable.ic_action_refresh)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == REFRESH_ITEM_ID) {
            startRefresh();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (mImageFetcher != null) {
            mImageFetcher.setPauseWork(false);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mImageFetcher != null) {
            mImageFetcher.setPauseWork(true);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        VKClient.getInstance().onActivityResult(requestCode, resultCode, data);
        updateUsers();
    }

    private void startRefresh() {
        mRefreshed = true;
        mUsersLoaded = false;
        if (!VKClient.getInstance().isAuthorized()) {
            VKClient.getInstance().authorize(this);
        }
        else {
            updateUsers();
        }
    }

    private void updateUsers() {
        if (VKClient.getInstance().isAuthorized() && !mUsersLoaded) {
            String userId = VKClient.getInstance().getUserId();
            showMainProgress();
            VKClient.getInstance().getFriends(userId, 0, 0, mInitialLoadListener);
        }
    }

    private void showMainProgress() {
        mUsersList.setVisibility(View.GONE);
        mUsersEmptyView.setVisibility(View.GONE);
        mMainProgress.setVisibility(View.VISIBLE);
    }

    private void hideMainProgress() {
        mUsersList.setVisibility(View.VISIBLE);
        mUsersEmptyView.setVisibility(mAdapter.isEmpty() ? View.VISIBLE : View.GONE);
        mMainProgress.setVisibility(View.GONE);
    }

    private class UsersAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mUsers.size();
        }

        @Override
        public Object getItem(int position) {
            return mUsers.get(position);
        }

        @Override
        public long getItemId(int position) {
            return ((VKUser) getItem(position)).getUserId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            UserHolder holder;
            if (convertView == null) {
                holder = new UserHolder();
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.user_list_item, null);
                holder.userName = (TextView) convertView.findViewById(R.id.userName);
                holder.userImage = (ImageView) convertView.findViewById(R.id.userImage);
                convertView.setTag(holder);
            }
            else {
                holder = (UserHolder) convertView.getTag();
            }

            VKUser user = (VKUser) getItem(position);
            holder.userName.setText(user.getFirstName() + " " + user.getLastName());
            mImageFetcher.loadImage(user.getPhoto100Url(), holder.userImage);

            return convertView;
        }
    }

    private class UserHolder {
        ImageView userImage;
        TextView userName;
    }
}
