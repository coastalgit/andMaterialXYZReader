package com.example.xyzreader.ui;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
    private static final String TAG = "ArticleDetailFragment";

    public static final String ARG_ITEM_ID = "item_id";
    private static final float PARALLAX_FACTOR = 1.25f;
    private static final int BODY_SPLIT_VALUE = 1000;

    private Cursor mCursor;
    private long mItemId;
    private View mRootView;
    private int mMutedColor = 0xFF333333;
    //private ObservableScrollView mScrollView;
    //private ScrollView mScrollView;
    //private DrawInsetsFrameLayout mDrawInsetsFrameLayout;
    //private ColorDrawable mStatusBarColorDrawable;

    private int mTopInset;
    private ImageView mPhotoView;
    private int mScrollY;
    //private boolean mIsCard = false;
    //private int mStatusBarFullOpacityBottom;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
    // Use default locale format
    private SimpleDateFormat outputFormat = new SimpleDateFormat();
    // Most time functions can only handle 1902 - 2037
    private GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2,1,1);

    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private LinearLayout mMetaBar;
    private FloatingActionButton mFAB;
    private Snackbar mSnackbar;
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

        //mIsCard = getResources().getBoolean(R.bool.detail_is_card);
        //mStatusBarFullOpacityBottom = getResources().getDimensionPixelSize(
        //        R.dimen.detail_card_top_margin);
        setHasOptionsMenu(true);
    }

    public ArticleDetailActivity getActivityCast() {
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

/*
        mDrawInsetsFrameLayout = (DrawInsetsFrameLayout)
                mRootView.findViewById(R.id.draw_insets_frame_layout);
        mDrawInsetsFrameLayout.setOnInsetsCallback(new DrawInsetsFrameLayout.OnInsetsCallback() {
            @Override
            public void onInsetsChanged(Rect insets) {
                mTopInset = insets.top;
            }
        });
*/

        //mScrollView = (ObservableScrollView) mRootView.findViewById(R.id.scrollview);
        //mScrollView = (ScrollView) mRootView.findViewById(R.id.scrollview);
//        mScrollView.setCallbacks(new ObservableScrollView.Callbacks() {
//            @Override
//            public void onScrollChanged() {
//                mScrollY = mScrollView.getScrollY();
//                getActivityCast().onUpButtonFloorChanged(mItemId, ArticleDetailFragment.this);
//                mPhotoContainerView.setTranslationY((int) (mScrollY - mScrollY / PARALLAX_FACTOR));
//                //updateStatusBar();
//            }
//        });

        mPhotoView = (ImageView) mRootView.findViewById(R.id.photo);
        //mPhotoContainerView = mRootView.findViewById(R.id.photo_container);

        mCollapsingToolbarLayout = (CollapsingToolbarLayout) mRootView.findViewById(R.id.list_collapsing_toolbar);
        mMetaBar = (LinearLayout) mRootView.findViewById(R.id.meta_bar);
        //mStatusBarColorDrawable = new ColorDrawable(0);

        mFAB = (FloatingActionButton) mRootView.findViewById(R.id.share_fab);
        mFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setText("Some sample text")
                        .getIntent(), getString(R.string.action_share)));
            }
        });

        mBodyView1 = (TextView) mRootView.findViewById(R.id.article_body1);
        mBodyView1.setTypeface(mTypeface_regular);
        mBodyView2 = (TextView) mRootView.findViewById(R.id.article_body2);
        mBodyView2.setTypeface(mTypeface_regular);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mBodyView1.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);
            mBodyView2.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);
        }

        mBtnLoadMore = (Button) mRootView.findViewById(R.id.btn_article_loadmore);

        bindViews();
        //updateStatusBar();
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

        TextView titleView = (TextView) mRootView.findViewById(R.id.article_title);
        titleView.setTypeface(mTypeface_regular);

        TextView bylineView = (TextView) mRootView.findViewById(R.id.article_byline);
        bylineView.setTypeface(mTypeface_light);

        mBtnLoadMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackBarShow("Loading", false);
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

/*
            ImageLoaderHelper.getInstance(getActivity()).getImageLoader()
                    .get(mCursor.getString(ArticleLoader.Query.PHOTO_URL), new ImageLoader.ImageListener() {
                        @Override
                        public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                            Bitmap bitmap = imageContainer.getBitmap();
                            if (bitmap != null) {
                                Palette p = Palette.generate(bitmap, 12);
                                mMutedColor = p.getDarkMutedColor(0xFF333333);
                                mPhotoView.setImageBitmap(imageContainer.getBitmap());
                                mRootView.findViewById(R.id.meta_bar)
                                        .setBackgroundColor(mMutedColor);
                                //updateStatusBar();
                            }
                        }

                        @Override
                        public void onErrorResponse(VolleyError volleyError) {

                        }
                    });
*/


            //bodyView.setText(Html.fromHtml(mCursor.getString(ArticleLoader.Query.BODY).replaceAll("(\r\n|\n)", "<br />")));
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

//            Picasso.get()
//                    .load(mCursor.getString(ArticleLoader.Query.PHOTO_URL))
//                    .into(mPhotoView);

/*
            Picasso.with(getActivityCast())
                    //get()
                    .load(mCursor.getString(ArticleLoader.Query.PHOTO_URL))
                    .into(mPhotoView,
                            new Callback() {
                                @Override
                                public void onSuccess() {
                                    Log.d(TAG, "onSuccess: ");
                                    mBodyView1.setText(mBodyPart1);
                                }

                                @Override
                                public void onError() {
                                    Log.d(TAG, "onError: ");
                                    snackBarShow("Failed to load image", false);
                                }

                            }
                    );
*/

/*
            if (getActivityCast() != null) {
                Glide.with(getActivityCast().getApplicationContext())
                        .asBitmap()
                        .load(mCursor.getString(ArticleLoader.Query.PHOTO_URL))

                        .listener(new RequestListener<Bitmap>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                Palette.from(resource).generate(new Palette.PaletteAsyncListener() {
                                    @Override
                                    public void onGenerated(@NonNull Palette palette) {
                                        if (palette.getDominantSwatch() != null) {
                                            cvDetailLand.setCardBackgroundColor(palette.getDominantSwatch().getRgb());
                                            articleTitleLand.setTextColor(palette.getDominantSwatch().getBodyTextColor());
                                        }
                                    }
                                });

                                return false;
                            }
                        })

                        .into(mPhotoView);
            }
*/

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
                                    public void onGenerated(Palette palette) {
                                        if (palette != null) {
                                            int defColour = ContextCompat.getColor(getActivityCast(), R.color.bluegrey_900);
                                            mCollapsingToolbarLayout.setContentScrimColor(palette.getMutedColor(defColour));
                                            mMetaBar.setBackgroundColor(palette.getMutedColor(defColour));
                                        }
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
    public void snackBarShow(final String message, final boolean asIndefinite){
        final int bgcolor = ContextCompat.getColor(getActivityCast(), R.color.deeporange_A200);
        mSnackbar = Snackbar.make(mRootView, message, asIndefinite?Snackbar.LENGTH_INDEFINITE:Snackbar.LENGTH_SHORT);
        mSnackbar.getView().setBackgroundColor(bgcolor);
        mSnackbar.setActionTextColor(Color.WHITE);
        mSnackbar.show();
    }

    public void snackBarDismiass(){
        if (mSnackbar != null && mSnackbar.isShown())
            mSnackbar.dismiss();
    }
    //endregion Snackbar

}
