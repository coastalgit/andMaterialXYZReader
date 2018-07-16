package com.example.xyzreader.ui;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.text.Html;
import android.text.Layout;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.xyzreader.R;
import com.example.xyzreader.common.Constants;
import com.example.xyzreader.data.ArticleLoader;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = ArticleDetailFragment.class.getSimpleName();

    private static final String ARG_ITEM_ID = "item_id";
    private static final int BODY_SPLIT_VALUE = 1000;

    private Cursor mCursor;
    private long mItemId;
    private View mRootView;
    private ImageView mPhotoView;

    @SuppressLint("SimpleDateFormat")
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
    // Use default locale format
    @SuppressLint("SimpleDateFormat")
    private final SimpleDateFormat outputFormat = new SimpleDateFormat();
    // Most time functions can only handle 1902 - 2037
    private final GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2,1,1);

    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private LinearLayout mMetaBar;
    @SuppressWarnings("FieldCanBeLocal")
    private FloatingActionButton mFAB;
    private Snackbar mSnackbar;
    @SuppressWarnings("FieldCanBeLocal")
    private String mBodyPart1, mBodyPart2;
    private TextView mBodyView1, mBodyView2;
    private Button mBtnLoadMore;

    private Typeface mTypeface_regular;
    private Typeface mTypeface_light;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleDetailFragment() {
    }

    public static ArticleDetailFragment newInstance(long itemId) {
        Bundle arguments = new Bundle();
        arguments.putLong(ARG_ITEM_ID, itemId);
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItemId = getArguments().getLong(ARG_ITEM_ID);
        }

        setHasOptionsMenu(true);
    }

    private ArticleDetailActivity getActivityCast() {
        return (ArticleDetailActivity) getActivity();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // In support library r8, calling initLoader for a fragment in a FragmentPagerAdapter in
        // the fragment's onCreate may cause the same LoaderManager to be dealt to multiple
        // fragments because their mIndex is -1 (haven't been added to the activity yet). Thus,
        // we do this in onActivityCreated.
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_article_detail, container, false);

        mTypeface_regular = Typeface.createFromAsset(getActivity().getAssets(), Constants.Fonts.FONT_MONTSERRAT_REGULAR);
        mTypeface_light = Typeface.createFromAsset(getActivity().getAssets(), Constants.Fonts.FONT_MONTSERRAT_LIGHT);

        mPhotoView = mRootView.findViewById(R.id.photo);
        //mPhotoContainerView = mRootView.findViewById(R.id.photo_container);

        mCollapsingToolbarLayout = mRootView.findViewById(R.id.list_collapsing_toolbar);
        mMetaBar = mRootView.findViewById(R.id.meta_bar);
        //mStatusBarColorDrawable = new ColorDrawable(0);

        mFAB = mRootView.findViewById(R.id.share_fab);
        mFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setText("")
                        .getIntent(), getString(R.string.action_share)));
            }
        });

        mBodyView1 = mRootView.findViewById(R.id.article_body1);
        mBodyView1.setTypeface(mTypeface_regular);
        mBodyView2 = mRootView.findViewById(R.id.article_body2);
        mBodyView2.setTypeface(mTypeface_regular);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mBodyView1.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);
            mBodyView2.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);
        }

        mBtnLoadMore = mRootView.findViewById(R.id.btn_article_loadmore);

        bindViews();

        return mRootView;
    }

    private Date parsePublishedDate() {
        try {
            String date = mCursor.getString(ArticleLoader.Query.PUBLISHED_DATE);
            return dateFormat.parse(date);
        } catch (ParseException ex) {
            Log.e(TAG, ex.getMessage());
            Log.i(TAG, "passing today's date");
            return new Date();
        }
    }

    private void bindViews() {
        if (mRootView == null) {
            return;
        }

        TextView titleView = mRootView.findViewById(R.id.article_title);
        titleView.setTypeface(mTypeface_regular);

        TextView bylineView = mRootView.findViewById(R.id.article_byline);
        bylineView.setTypeface(mTypeface_light);

        mBtnLoadMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackBarShow(getString(R.string.loading), false);
                mBtnLoadMore.setVisibility(View.GONE);
                mBodyView2.setVisibility(View.VISIBLE);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mBodyView2.setText(mBodyPart2);
                    }
                }, 1000);
            }
        });

        if (mCursor != null) {
            mRootView.setAlpha(0);
            mRootView.setVisibility(View.VISIBLE);
            mRootView.animate().alpha(1);
            titleView.setText(mCursor.getString(ArticleLoader.Query.TITLE));
            Date publishedDate = parsePublishedDate();
            if (!publishedDate.before(START_OF_EPOCH.getTime())) {
                bylineView.setText(Html.fromHtml(
                        DateUtils.getRelativeTimeSpanString(
                                publishedDate.getTime(),
                                System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                                DateUtils.FORMAT_ABBREV_ALL).toString()
                                + " by <font color='#ffffff'>"
                                + mCursor.getString(ArticleLoader.Query.AUTHOR)
                                + "</font>"));

            } else {
                // If date is before 1902, just show the string
                bylineView.setText(Html.fromHtml(
                        outputFormat.format(publishedDate) + " by <font color='#ffffff'>"
                                + mCursor.getString(ArticleLoader.Query.AUTHOR)
                                + "</font>"));

            }

            // tidy up the string a bit...
            String body = mCursor.getString(ArticleLoader.Query.BODY);
            String body2 = body.replace("\r\n\r\n", "{rn}");
            String body3 = body2.replace("\r\n", " ");
            body = body3.replace("{rn}", "\r\n\r\n");
            //Log.d(TAG, "bindViews: body length:"+String.valueOf(body.length()));
            if (body.length() > BODY_SPLIT_VALUE){
                mBodyPart1 = body.substring(0, BODY_SPLIT_VALUE);
                mBodyPart2 = body.substring(BODY_SPLIT_VALUE, body.length() - 1);
                mBtnLoadMore.setVisibility(View.VISIBLE);
            }
            else {
                mBodyPart1 = body;
                mBodyPart2 = "";
                mBtnLoadMore.setVisibility(View.INVISIBLE);
                mBodyView2.setVisibility(View.GONE);
            }

            mBodyView1.setText(mBodyPart1);

            if (getActivityCast() != null) {
                Glide.with(getActivityCast().getApplicationContext())
                        .asBitmap()
                        .load(mCursor.getString(ArticleLoader.Query.PHOTO_URL))
                        .listener(new RequestListener<Bitmap>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                                // TODO: 12/07/2018
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Bitmap bitmap, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                                    public void onGenerated(@NonNull Palette palette) {
                                        int defColour = ContextCompat.getColor(getActivityCast(), R.color.bluegrey_900);
                                        mCollapsingToolbarLayout.setContentScrimColor(palette.getMutedColor(defColour));
                                        mMetaBar.setBackgroundColor(palette.getMutedColor(defColour));
                                    }
                                });
                                return false;
                            }
                        }).into(mPhotoView);
            }
        } 
        else {
            mRootView.setVisibility(View.GONE);
            mBtnLoadMore.setVisibility(View.GONE);
            titleView.setText("N/A");
            bylineView.setText("N/A" );
            mBodyView1.setText("N/A");
        }

    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newInstanceForItemId(getActivity(), mItemId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (!isAdded()) {
            if (cursor != null) {
                cursor.close();
            }
            return;
        }

        mCursor = cursor;
        if (mCursor != null && !mCursor.moveToFirst()) {
            Log.e(TAG, "Error reading item detail cursor");
            mCursor.close();
            mCursor = null;
        }

        bindViews();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
        bindViews();
    }

    //region Snackbar
    @SuppressWarnings("SameParameterValue")
    private void snackBarShow(final String message, final boolean asIndefinite){
        final int bgcolor = ContextCompat.getColor(getActivityCast(), R.color.deeporange_A200);
        mSnackbar = Snackbar.make(mRootView, message, asIndefinite?Snackbar.LENGTH_INDEFINITE:Snackbar.LENGTH_SHORT);
        mSnackbar.getView().setBackgroundColor(bgcolor);
        mSnackbar.setActionTextColor(Color.WHITE);
        mSnackbar.show();
    }

    @SuppressWarnings("unused")
    public void snackBarDismiass(){
        if (mSnackbar != null && mSnackbar.isShown())
            mSnackbar.dismiss();
    }
    //endregion Snackbar

}
