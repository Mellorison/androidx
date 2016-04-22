/* This file is auto-generated from BrowseFragment.java.  DO NOT MODIFY. */

/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package android.support.v17.leanback.app;

import static android.support.v7.widget.RecyclerView.NO_POSITION;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.BackStackEntry;
import android.support.v4.app.FragmentTransaction;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.v17.leanback.R;
import android.support.v17.leanback.transition.TransitionHelper;
import android.support.v17.leanback.transition.TransitionListener;
import android.support.v17.leanback.widget.BrowseFrameLayout;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ObjectAdapter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.PageRow;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.PresenterSelector;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowHeaderPresenter;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v17.leanback.widget.ScaleFrameLayout;
import android.support.v17.leanback.widget.TitleView;
import android.support.v17.leanback.widget.VerticalGridView;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewTreeObserver;

import java.util.HashMap;
import java.util.Map;

/**
 * A fragment for creating Leanback browse screens. It is composed of a
 * RowsSupportFragment and a HeadersSupportFragment.
 * <p>
 * A BrowseSupportFragment renders the elements of its {@link ObjectAdapter} as a set
 * of rows in a vertical list. The elements in this adapter must be subclasses
 * of {@link Row}.
 * <p>
 * The HeadersSupportFragment can be set to be either shown or hidden by default, or
 * may be disabled entirely. See {@link #setHeadersState} for details.
 * <p>
 * By default the BrowseSupportFragment includes support for returning to the headers
 * when the user presses Back. For Activities that customize {@link
 * android.support.v4.app.FragmentActivity#onBackPressed()}, you must disable this default Back key support by
 * calling {@link #setHeadersTransitionOnBackEnabled(boolean)} with false and
 * use {@link BrowseSupportFragment.BrowseTransitionListener} and
 * {@link #startHeadersTransition(boolean)}.
 * <p>
 * The recommended theme to use with a BrowseSupportFragment is
 * {@link android.support.v17.leanback.R.style#Theme_Leanback_Browse}.
 * </p>
 */
public class BrowseSupportFragment extends BaseSupportFragment {

    // BUNDLE attribute for saving header show/hide status when backstack is used:
    static final String HEADER_STACK_INDEX = "headerStackIndex";
    // BUNDLE attribute for saving header show/hide status when backstack is not used:
    static final String HEADER_SHOW = "headerShow";
    private static final String IS_PAGE_ROW = "isPageRow";
    private static final String CURRENT_SELECTED_POSITION = "currentSelectedPosition";

    final class BackStackListener implements FragmentManager.OnBackStackChangedListener {
        int mLastEntryCount;
        int mIndexOfHeadersBackStack;

        BackStackListener() {
            mLastEntryCount = getFragmentManager().getBackStackEntryCount();
            mIndexOfHeadersBackStack = -1;
        }

        void load(Bundle savedInstanceState) {
            if (savedInstanceState != null) {
                mIndexOfHeadersBackStack = savedInstanceState.getInt(HEADER_STACK_INDEX, -1);
                mShowingHeaders = mIndexOfHeadersBackStack == -1;
            } else {
                if (!mShowingHeaders) {
                    getFragmentManager().beginTransaction()
                            .addToBackStack(mWithHeadersBackStackName).commit();
                }
            }
        }

        void save(Bundle outState) {
            outState.putInt(HEADER_STACK_INDEX, mIndexOfHeadersBackStack);
        }


        @Override
        public void onBackStackChanged() {
            if (getFragmentManager() == null) {
                Log.w(TAG, "getFragmentManager() is null, stack:", new Exception());
                return;
            }
            int count = getFragmentManager().getBackStackEntryCount();
            // if backstack is growing and last pushed entry is "headers" backstack,
            // remember the index of the entry.
            if (count > mLastEntryCount) {
                BackStackEntry entry = getFragmentManager().getBackStackEntryAt(count - 1);
                if (mWithHeadersBackStackName.equals(entry.getName())) {
                    mIndexOfHeadersBackStack = count - 1;
                }
            } else if (count < mLastEntryCount) {
                // if popped "headers" backstack, initiate the show header transition if needed
                if (mIndexOfHeadersBackStack >= count) {
                    if (!isHeadersDataReady()) {
                        // if main fragment was restored first before BrowseSupportFragment's adapater gets
                        // restored: dont start header transition, but add the entry back.
                        getFragmentManager().beginTransaction()
                                .addToBackStack(mWithHeadersBackStackName).commit();
                        return;
                    }
                    mIndexOfHeadersBackStack = -1;
                    if (!mShowingHeaders) {
                        startHeadersTransitionInternal(true);
                    }
                }
            }
            mLastEntryCount = count;
        }
    }

    /**
     * Listener for transitions between browse headers and rows.
     */
    public static class BrowseTransitionListener {
        /**
         * Callback when headers transition starts.
         *
         * @param withHeaders True if the transition will result in headers
         *        being shown, false otherwise.
         */
        public void onHeadersTransitionStart(boolean withHeaders) {
        }
        /**
         * Callback when headers transition stops.
         *
         * @param withHeaders True if the transition will result in headers
         *        being shown, false otherwise.
         */
        public void onHeadersTransitionStop(boolean withHeaders) {
        }
    }

    private class SetSelectionRunnable implements Runnable {
        static final int TYPE_INVALID = -1;
        static final int TYPE_INTERNAL_SYNC = 0;
        static final int TYPE_USER_REQUEST = 1;

        private int mPosition;
        private int mType;
        private boolean mSmooth;

        SetSelectionRunnable() {
            reset();
        }

        void post(int position, int type, boolean smooth) {
            // Posting the set selection, rather than calling it immediately, prevents an issue
            // with adapter changes.  Example: a row is added before the current selected row;
            // first the fast lane view updates its selection, then the rows fragment has that
            // new selection propagated immediately; THEN the rows view processes the same adapter
            // change and moves the selection again.
            if (type >= mType) {
                mPosition = position;
                mType = type;
                mSmooth = smooth;
                mBrowseFrame.removeCallbacks(this);
                mBrowseFrame.post(this);
            }
        }

        @Override
        public void run() {
            setSelection(mPosition, mSmooth);
            reset();
        }

        private void reset() {
            mPosition = -1;
            mType = TYPE_INVALID;
            mSmooth = false;
        }
    }

    /**
     * Possible set of actions that {@link BrowseSupportFragment} exposes to clients. Custom
     * fragments can interact with {@link BrowseSupportFragment} using this interface.
     */
    public interface FragmentHost {
        /**
         * Clients are required to invoke this callback once their view is created
         * inside {@link Fragment#onStart} method. {@link BrowseSupportFragment} starts the entrance
         * animation only after receiving this callback. Failure to invoke this method
         * will lead to fragment not showing up.
         *
         * @param fragmentAdapter {@link MainFragmentAdapter} used by the current fragment.
         */
        void notifyViewCreated(MainFragmentAdapter fragmentAdapter);

        /**
         * Slides in the title view from top in {@link BrowseSupportFragment}. This will only happen
         * if either a. we are in fully expanded mode OR b. non expanded mode but on the first
         * row. If we make this request in non expanded mode, it will remember that request and
         * show/hide the {@link TitleView} when we move into expanded mode.
         *
         * @param show Boolean indicating whether or not to show the title view.
         */
        void showTitleView(boolean show);
    }

    /**
     * Default implementation of {@link FragmentHost} that is used only by
     * {@link BrowseSupportFragment}.
     */
    private final class FragmentHostImpl implements FragmentHost {
        boolean mShowTitleView = true;

        @Override
        public void notifyViewCreated(MainFragmentAdapter fragmentAdapter) {
            processingPendingEntranceTransition();
        }

        @Override
        public void showTitleView(boolean show) {
            mShowTitleView = show;

            // If fragment host is not the currently active fragment (in BrowseSupportFragment), then
            // ignore the request.
            if (mMainFragmentAdapter == null || mMainFragmentAdapter.getFragmentHost() != this) {
                return;
            }

            // We only honor showTitle request for PageRows.
            if (!mIsPageRow) {
                return;
            }

            // We will execute this request only when the fast lane is hidden/disabled.
            if (!mShowingHeaders) {
                if (show) {
                    showTitle(TitleView.FULL_VIEW_VISIBLE);
                } else {
                    showTitle(false);
                }
            }
        }
    }

    /**
     * Interface that defines the interaction between {@link BrowseSupportFragment} and it's main
     * content fragment. The key method is {@link MainFragmentAdapter#getFragment()},
     * it will be used to get the fragment to be shown in the content section. Clients can
     * provide any implementation of fragment and customize it's interaction with
     * {@link BrowseSupportFragment} by overriding the necessary methods.
     *
     * <p>
     * Clients are expected to provide
     * an instance of {@link MainFragmentAdapterRegistry} which will be responsible for providing
     * implementations of {@link MainFragmentAdapter} for given content types. Currently
     * we support different types of content - {@link ListRow}, {@link PageRow} or any subtype
     * of {@link Row}. We provide an out of the box adapter implementation for any rows other than
     * {@link PageRow} - {@link android.support.v17.leanback.app.RowsSupportFragment.MainFragmentAdapter}.
     *
     * <p>
     * {@link PageRow} is intended to give full flexibility to developers in terms of Fragment
     * design. Users will have to provide an implementation of {@link MainFragmentAdapter}
     * and provide that through {@link MainFragmentAdapterRegistry}.
     * {@link MainFragmentAdapter} implementation can supply any fragment and override
     * just those interactions that makes sense.
     */
    public static class MainFragmentAdapter<T extends Fragment> {
        private boolean mScalingEnabled;
        private final T mFragment;
        private FragmentHostImpl mFragmentHost;

        public MainFragmentAdapter(T fragment) {
            this.mFragment = fragment;
        }

        public final T getFragment() {
            return mFragment;
        }

        /**
         * Returns whether its scrolling.
         */
        public boolean isScrolling() {
            return false;
        }

        /**
         * Set the visibility of titles/hovercard of browse rows.
         */
        public void setExpand(boolean expand) {
        }

        /**
         * For rows that willing to participate entrance transition,  this function
         * hide views if afterTransition is true,  show views if afterTransition is false.
         */
        public void setEntranceTransitionState(boolean state) {
        }

        /**
         * Sets the window alignment and also the pivots for scale operation.
         */
        public void setAlignment(int windowAlignOffsetFromTop) {
        }

        /**
         * Callback indicating transition prepare start.
         */
        public boolean onTransitionPrepare() {
            return false;
        }

        /**
         * Callback indicating transition start.
         */
        public void onTransitionStart() {
        }

        /**
         * Callback indicating transition end.
         */
        public void onTransitionEnd() {
        }

        /**
         * Returns whether row scaling is enabled.
         */
        public boolean isScalingEnabled() {
            return mScalingEnabled;
        }

        /**
         * Sets the row scaling property.
         */
        public void setScalingEnabled(boolean scalingEnabled) {
            this.mScalingEnabled = scalingEnabled;
        }

        /**
         * Returns the current host interface so that main fragment can interact with
         * {@link BrowseSupportFragment}.
         */
        public final FragmentHost getFragmentHost() {
            return mFragmentHost;
        }

        void setFragmentHost(FragmentHostImpl fragmentHost) {
            this.mFragmentHost = fragmentHost;
        }
    }

    /**
     * This is used to pass information to {@link RowsSupportFragment}.
     * {@link android.support.v17.leanback.app.RowsSupportFragment.MainFragmentAdapter}
     * would return an instance to connect the callbacks from {@link BrowseSupportFragment} to
     * {@link RowsSupportFragment}.
     */
    public static class MainFragmentRowsAdapter<T extends Fragment> {
        private final T mFragment;

        public MainFragmentRowsAdapter(T fragment) {
            if (fragment == null) {
                throw new IllegalArgumentException("Fragment can't be null");
            }
            this.mFragment = fragment;
        }

        public final T getFragment() {
            return mFragment;
        }
        /**
         * Set the visibility titles/hover of browse rows.
         */
        public void setAdapter(ObjectAdapter adapter) {
        }

        /**
         * Sets an item clicked listener on the fragment.
         */
        public void setOnItemViewClickedListener(OnItemViewClickedListener listener) {
        }

        /**
         * Sets an item selection listener.
         */
        public void setOnItemViewSelectedListener(OnItemViewSelectedListener listener) {
        }

        /**
         * Selects a Row and perform an optional task on the Row.
         */
        public void setSelectedPosition(int rowPosition,
                                        boolean smooth,
                                        final Presenter.ViewHolderTask rowHolderTask) {
        }

        /**
         * Selects a Row.
         */
        public void setSelectedPosition(int rowPosition, boolean smooth) {
        }

        /**
         * Returns the selected position.
         */
        public int getSelectedPosition() {
            return 0;
        }
    }

    private boolean createMainFragment(ObjectAdapter adapter, int position) {
        Object item = null;
        if (adapter == null || adapter.size() == 0) {
            return false;
        } else {
            if (position < 0) {
                position = 0;
            } else if (position >= adapter.size()) {
                throw new IllegalArgumentException(
                        String.format("Invalid position %d requested", position));
            }
            item = adapter.get(position);
        }

        mSelectedPosition = position;
        boolean oldIsPageRow = mIsPageRow;
        mIsPageRow = item instanceof PageRow;
        boolean swap;

        if (mMainFragment == null) {
            swap = true;
        } else {
            if (oldIsPageRow) {
                swap = true;
            } else {
                swap = mIsPageRow;
            }
        }

        if (swap) {
            mMainFragment = mMainFragmentAdapterRegistry.createFragment(item);
            mMainFragmentAdapter = (MainFragmentAdapter) ((Adaptable)mMainFragment)
                    .getAdapter(MainFragmentAdapter.class);
            mMainFragmentAdapter.setFragmentHost(new FragmentHostImpl());
            if (!mIsPageRow) {
                mMainFragmentRowsAdapter = (MainFragmentRowsAdapter) ((Adaptable)mMainFragment)
                        .getAdapter(MainFragmentRowsAdapter.class);
                mIsPageRow = mMainFragmentRowsAdapter == null;
            } else {
                mMainFragmentRowsAdapter = null;
            }
        }
        return swap;
    }

    /**
     * Factory class responsible for creating fragment given the current item. {@link ListRow}
     * should returns {@link RowsSupportFragment} or it's subclass whereas {@link PageRow}
     * can return any fragment class.
     */
    public abstract static class FragmentFactory<T extends Fragment> {
        public abstract T createFragment(Object row);
    }

    /**
     * FragmentFactory implementation for {@link ListRow}.
     */
    public static class ListRowFragmentFactory extends FragmentFactory<RowsSupportFragment> {
        @Override
        public RowsSupportFragment createFragment(Object row) {
            return new RowsSupportFragment();
        }
    }

    /**
     * Registry class maintaining the mapping of {@link Row} subclasses to {@link FragmentFactory}.
     * BrowseRowFragment automatically registers {@link ListRowFragmentFactory} for
     * handling {@link ListRow}. Developers can override that and also if they want to
     * use custom fragment, they can register a custom {@link FragmentFactory}
     * against {@link PageRow}.
     */
    public final static class MainFragmentAdapterRegistry {
        private final Map<Class, FragmentFactory> mItemToFragmentFactoryMapping = new HashMap();
        private final static FragmentFactory sDefaultFragmentFactory = new ListRowFragmentFactory();

        public MainFragmentAdapterRegistry() {
            registerFragment(ListRow.class, sDefaultFragmentFactory);
        }

        public void registerFragment(Class rowClass, FragmentFactory factory) {
            mItemToFragmentFactoryMapping.put(rowClass, factory);
        }

        public Fragment createFragment(Object item) {
            if (item == null) {
                throw new IllegalArgumentException("Item can't be null");
            }

            FragmentFactory fragmentFactory = mItemToFragmentFactoryMapping.get(item.getClass());
            if (fragmentFactory == null && !(item instanceof PageRow)) {
                fragmentFactory = sDefaultFragmentFactory;
            }

            return fragmentFactory.createFragment(item);
        }
    }

    private static final String TAG = "BrowseSupportFragment";

    private static final String LB_HEADERS_BACKSTACK = "lbHeadersBackStack_";

    private static boolean DEBUG = false;

    /** The headers fragment is enabled and shown by default. */
    public static final int HEADERS_ENABLED = 1;

    /** The headers fragment is enabled and hidden by default. */
    public static final int HEADERS_HIDDEN = 2;

    /** The headers fragment is disabled and will never be shown. */
    public static final int HEADERS_DISABLED = 3;

    private MainFragmentAdapterRegistry mMainFragmentAdapterRegistry
            = new MainFragmentAdapterRegistry();
    private MainFragmentAdapter mMainFragmentAdapter;
    private Fragment mMainFragment;
    private HeadersSupportFragment mHeadersSupportFragment;
    private MainFragmentRowsAdapter mMainFragmentRowsAdapter;

    private ObjectAdapter mAdapter;

    private int mHeadersState = HEADERS_ENABLED;
    private int mBrandColor = Color.TRANSPARENT;
    private boolean mBrandColorSet;

    private BrowseFrameLayout mBrowseFrame;
    private ScaleFrameLayout mScaleFrameLayout;
    private boolean mHeadersBackStackEnabled = true;
    private String mWithHeadersBackStackName;
    private boolean mShowingHeaders = true;
    private boolean mCanShowHeaders = true;
    private int mContainerListMarginStart;
    private int mContainerListAlignTop;
    private boolean mMainFragmentScaleEnabled = true;
    private OnItemViewSelectedListener mExternalOnItemViewSelectedListener;
    private OnItemViewClickedListener mOnItemViewClickedListener;
    private int mSelectedPosition = -1;
    private float mScaleFactor;
    private boolean mIsPageRow;

    private PresenterSelector mHeaderPresenterSelector;
    private final SetSelectionRunnable mSetSelectionRunnable = new SetSelectionRunnable();

    // transition related:
    private Object mSceneWithHeaders;
    private Object mSceneWithoutHeaders;
    private Object mSceneAfterEntranceTransition;
    private Object mHeadersTransition;
    private BackStackListener mBackStackChangedListener;
    private BrowseTransitionListener mBrowseTransitionListener;

    private static final String ARG_TITLE = BrowseSupportFragment.class.getCanonicalName() + ".title";
    private static final String ARG_HEADERS_STATE =
        BrowseSupportFragment.class.getCanonicalName() + ".headersState";

    /**
     * Creates arguments for a browse fragment.
     *
     * @param args The Bundle to place arguments into, or null if the method
     *        should return a new Bundle.
     * @param title The title of the BrowseSupportFragment.
     * @param headersState The initial state of the headers of the
     *        BrowseSupportFragment. Must be one of {@link #HEADERS_ENABLED}, {@link
     *        #HEADERS_HIDDEN}, or {@link #HEADERS_DISABLED}.
     * @return A Bundle with the given arguments for creating a BrowseSupportFragment.
     */
    public static Bundle createArgs(Bundle args, String title, int headersState) {
        if (args == null) {
            args = new Bundle();
        }
        args.putString(ARG_TITLE, title);
        args.putInt(ARG_HEADERS_STATE, headersState);
        return args;
    }

    /**
     * Sets the brand color for the browse fragment. The brand color is used as
     * the primary color for UI elements in the browse fragment. For example,
     * the background color of the headers fragment uses the brand color.
     *
     * @param color The color to use as the brand color of the fragment.
     */
    public void setBrandColor(@ColorInt int color) {
        mBrandColor = color;
        mBrandColorSet = true;

        if (mHeadersSupportFragment != null) {
            mHeadersSupportFragment.setBackgroundColor(mBrandColor);
        }
    }

    /**
     * Returns the brand color for the browse fragment.
     * The default is transparent.
     */
    @ColorInt
    public int getBrandColor() {
        return mBrandColor;
    }

    /**
     * Sets the adapter containing the rows for the fragment.
     *
     * <p>The items referenced by the adapter must be be derived from
     * {@link Row}. These rows will be used by the rows fragment and the headers
     * fragment (if not disabled) to render the browse rows.
     *
     * @param adapter An ObjectAdapter for the browse rows. All items must
     *        derive from {@link Row}.
     */
    public void setAdapter(ObjectAdapter adapter) {
        mAdapter = adapter;
        if (getView() == null) {
            return;
        }
        replaceMainFragment(mSelectedPosition);

        if (adapter != null) {
            if (mMainFragmentRowsAdapter != null) {
                mMainFragmentRowsAdapter.setAdapter(adapter);
            }
            mHeadersSupportFragment.setAdapter(adapter);
        }
    }

    public final MainFragmentAdapterRegistry getMainFragmentRegistry() {
        return mMainFragmentAdapterRegistry;
    }

    /**
     * Returns the adapter containing the rows for the fragment.
     */
    public ObjectAdapter getAdapter() {
        return mAdapter;
    }

    /**
     * Sets an item selection listener.
     */
    public void setOnItemViewSelectedListener(OnItemViewSelectedListener listener) {
        mExternalOnItemViewSelectedListener = listener;
    }

    /**
     * Returns an item selection listener.
     */
    public OnItemViewSelectedListener getOnItemViewSelectedListener() {
        return mExternalOnItemViewSelectedListener;
    }

    /**
     * Get RowsSupportFragment if it's bound to BrowseSupportFragment or null if either BrowseSupportFragment has
     * not been created yet or a different fragment is bound to it.
     *
     * @return RowsSupportFragment if it's bound to BrowseSupportFragment or null otherwise.
     */
    public RowsSupportFragment getRowsSupportFragment() {
        if (mMainFragment instanceof RowsSupportFragment) {
            return (RowsSupportFragment) mMainFragment;
        }

        return null;
    }

    /**
     * Get currently bound HeadersSupportFragment or null if HeadersSupportFragment has not been created yet.
     * @return Currently bound HeadersSupportFragment or null if HeadersSupportFragment has not been created yet.
     */
    public HeadersSupportFragment getHeadersSupportFragment() {
        return mHeadersSupportFragment;
    }

    /**
     * Sets an item clicked listener on the fragment.
     * OnItemViewClickedListener will override {@link View.OnClickListener} that
     * item presenter sets during {@link Presenter#onCreateViewHolder(ViewGroup)}.
     * So in general, developer should choose one of the listeners but not both.
     */
    public void setOnItemViewClickedListener(OnItemViewClickedListener listener) {
        mOnItemViewClickedListener = listener;
        if (mMainFragmentRowsAdapter != null) {
            mMainFragmentRowsAdapter.setOnItemViewClickedListener(listener);
        }
    }

    /**
     * Returns the item Clicked listener.
     */
    public OnItemViewClickedListener getOnItemViewClickedListener() {
        return mOnItemViewClickedListener;
    }

    /**
     * Starts a headers transition.
     *
     * <p>This method will begin a transition to either show or hide the
     * headers, depending on the value of withHeaders. If headers are disabled
     * for this browse fragment, this method will throw an exception.
     *
     * @param withHeaders True if the headers should transition to being shown,
     *        false if the transition should result in headers being hidden.
     */
    public void startHeadersTransition(boolean withHeaders) {
        if (!mCanShowHeaders) {
            throw new IllegalStateException("Cannot start headers transition");
        }
        if (isInHeadersTransition() || mShowingHeaders == withHeaders) {
            return;
        }
        startHeadersTransitionInternal(withHeaders);
    }

    /**
     * Returns true if the headers transition is currently running.
     */
    public boolean isInHeadersTransition() {
        return mHeadersTransition != null;
    }

    /**
     * Returns true if headers are shown.
     */
    public boolean isShowingHeaders() {
        return mShowingHeaders;
    }

    /**
     * Sets a listener for browse fragment transitions.
     *
     * @param listener The listener to call when a browse headers transition
     *        begins or ends.
     */
    public void setBrowseTransitionListener(BrowseTransitionListener listener) {
        mBrowseTransitionListener = listener;
    }

    /**
     * @deprecated use {@link BrowseSupportFragment#enableMainFragmentScaling(boolean)} instead.
     *
     * @param enable true to enable row scaling
     */
    @Deprecated
    public void enableRowScaling(boolean enable) {
        enableMainFragmentScaling(enable);
    }

    /**
     * Enables scaling of main fragment when headers are present. For the page/row fragment,
     * scaling is enabled only when both this method and
     * {@link MainFragmentAdapter#isScalingEnabled()} are enabled.
     *
     * @param enable true to enable row scaling
     */
    public void enableMainFragmentScaling(boolean enable) {
        mMainFragmentScaleEnabled = enable;
    }

    private void startHeadersTransitionInternal(final boolean withHeaders) {
        if (getFragmentManager().isDestroyed()) {
            return;
        }
        if (!isHeadersDataReady()) {
            return;
        }
        mShowingHeaders = withHeaders;
        mMainFragmentAdapter.onTransitionPrepare();
        mMainFragmentAdapter.onTransitionStart();
        onExpandTransitionStart(!withHeaders, new Runnable() {
            @Override
            public void run() {
                mHeadersSupportFragment.onTransitionPrepare();
                mHeadersSupportFragment.onTransitionStart();
                createHeadersTransition();
                if (mBrowseTransitionListener != null) {
                    mBrowseTransitionListener.onHeadersTransitionStart(withHeaders);
                }
                TransitionHelper.runTransition(
                        withHeaders ? mSceneWithHeaders : mSceneWithoutHeaders, mHeadersTransition);
                if (mHeadersBackStackEnabled) {
                    if (!withHeaders) {
                        getFragmentManager().beginTransaction()
                                .addToBackStack(mWithHeadersBackStackName).commit();
                    } else {
                        int index = mBackStackChangedListener.mIndexOfHeadersBackStack;
                        if (index >= 0) {
                            BackStackEntry entry = getFragmentManager().getBackStackEntryAt(index);
                            getFragmentManager().popBackStackImmediate(entry.getId(),
                                    FragmentManager.POP_BACK_STACK_INCLUSIVE);
                        }
                    }
                }
            }
        });
    }

    boolean isVerticalScrolling() {
        // don't run transition
        return mHeadersSupportFragment.isScrolling() || mMainFragmentAdapter.isScrolling();
    }


    private final BrowseFrameLayout.OnFocusSearchListener mOnFocusSearchListener =
            new BrowseFrameLayout.OnFocusSearchListener() {
        @Override
        public View onFocusSearch(View focused, int direction) {
            // if headers is running transition,  focus stays
            if (mCanShowHeaders && isInHeadersTransition()) {
                return focused;
            }
            if (DEBUG) Log.v(TAG, "onFocusSearch focused " + focused + " + direction " + direction);

            if (getTitleView() != null && focused != getTitleView() &&
                    direction == View.FOCUS_UP) {
                return getTitleView();
            }
            if (getTitleView() != null && getTitleView().hasFocus() &&
                    direction == View.FOCUS_DOWN) {
                return mCanShowHeaders && mShowingHeaders ?
                        mHeadersSupportFragment.getVerticalGridView() : mMainFragment.getView();
            }

            boolean isRtl = ViewCompat.getLayoutDirection(focused) == View.LAYOUT_DIRECTION_RTL;
            int towardStart = isRtl ? View.FOCUS_RIGHT : View.FOCUS_LEFT;
            int towardEnd = isRtl ? View.FOCUS_LEFT : View.FOCUS_RIGHT;
            if (mCanShowHeaders && direction == towardStart) {
                if (isVerticalScrolling() || mShowingHeaders || !isHeadersDataReady()) {
                    return focused;
                }
                return mHeadersSupportFragment.getVerticalGridView();
            } else if (direction == towardEnd) {
                if (isVerticalScrolling()) {
                    return focused;
                }
                return mMainFragment.getView();
            } else {
                return null;
            }
        }
    };

    private final boolean isHeadersDataReady() {
        return mAdapter != null && mAdapter.size() != 0;
    }

    private final BrowseFrameLayout.OnChildFocusListener mOnChildFocusListener =
            new BrowseFrameLayout.OnChildFocusListener() {

        @Override
        public boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
            if (getChildFragmentManager().isDestroyed()) {
                return true;
            }
            // Make sure not changing focus when requestFocus() is called.
            if (mCanShowHeaders && mShowingHeaders) {
                if (mHeadersSupportFragment != null && mHeadersSupportFragment.getView() != null &&
                        mHeadersSupportFragment.getView().requestFocus(direction, previouslyFocusedRect)) {
                    return true;
                }
            }
            if (mMainFragment != null && mMainFragment.getView() != null &&
                    mMainFragment.getView().requestFocus(direction, previouslyFocusedRect)) {
                return true;
            }
            if (getTitleView() != null &&
                    getTitleView().requestFocus(direction, previouslyFocusedRect)) {
                return true;
            }
            return false;
        }

        @Override
        public void onRequestChildFocus(View child, View focused) {
            if (getChildFragmentManager().isDestroyed()) {
                return;
            }
            if (!mCanShowHeaders || isInHeadersTransition()) return;
            int childId = child.getId();
            if (childId == R.id.browse_container_dock && mShowingHeaders) {
                startHeadersTransitionInternal(false);
            } else if (childId == R.id.browse_headers_dock && !mShowingHeaders) {
                startHeadersTransitionInternal(true);
            }
        }
    };

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(CURRENT_SELECTED_POSITION, mSelectedPosition);
        outState.putBoolean(IS_PAGE_ROW, mIsPageRow);

        if (mBackStackChangedListener != null) {
            mBackStackChangedListener.save(outState);
        } else {
            outState.putBoolean(HEADER_SHOW, mShowingHeaders);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TypedArray ta = getActivity().obtainStyledAttributes(R.styleable.LeanbackTheme);
        mContainerListMarginStart = (int) ta.getDimension(
                R.styleable.LeanbackTheme_browseRowsMarginStart, getActivity().getResources()
                .getDimensionPixelSize(R.dimen.lb_browse_rows_margin_start));
        mContainerListAlignTop = (int) ta.getDimension(
                R.styleable.LeanbackTheme_browseRowsMarginTop, getActivity().getResources()
                .getDimensionPixelSize(R.dimen.lb_browse_rows_margin_top));
        ta.recycle();

        readArguments(getArguments());

        if (mCanShowHeaders) {
            if (mHeadersBackStackEnabled) {
                mWithHeadersBackStackName = LB_HEADERS_BACKSTACK + this;
                mBackStackChangedListener = new BackStackListener();
                getFragmentManager().addOnBackStackChangedListener(mBackStackChangedListener);
                mBackStackChangedListener.load(savedInstanceState);
            } else {
                if (savedInstanceState != null) {
                    mShowingHeaders = savedInstanceState.getBoolean(HEADER_SHOW);
                }
            }
        }

        mScaleFactor = getResources().getFraction(R.fraction.lb_browse_rows_scale, 1, 1);
    }

    @Override
    public void onDestroyView() {
        mMainFragmentRowsAdapter = null;
        mMainFragmentAdapter = null;
        mMainFragment = null;
        mHeadersSupportFragment = null;
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        if (mBackStackChangedListener != null) {
            getFragmentManager().removeOnBackStackChangedListener(mBackStackChangedListener);
        }
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        if (getChildFragmentManager().findFragmentById(R.id.scale_frame) == null) {
            mHeadersSupportFragment = new HeadersSupportFragment();

            createMainFragment(mAdapter, mSelectedPosition);
            FragmentTransaction ft = getChildFragmentManager().beginTransaction()
                    .replace(R.id.browse_headers_dock, mHeadersSupportFragment);

            if (mMainFragment != null) {
                ft.replace(R.id.scale_frame, mMainFragment);
            } else {
                // Empty adapter used to guard against lazy adapter loading. When this
                // fragment is instantiated, mAdapter might not have the data or might not
                // have been set. In either of those cases mFragmentAdapter will be null.
                // This way we can maintain the invariant that mMainFragmentAdapter is never
                // null and it avoids doing null checks all over the code.
                mMainFragmentAdapter = new MainFragmentAdapter(null);
                mMainFragmentAdapter.setFragmentHost(new FragmentHostImpl());
            }

            ft.commit();
        } else {
            mHeadersSupportFragment = (HeadersSupportFragment) getChildFragmentManager()
                    .findFragmentById(R.id.browse_headers_dock);
            mMainFragment = getChildFragmentManager().findFragmentById(R.id.scale_frame);
            mMainFragmentAdapter = (MainFragmentAdapter) ((Adaptable)mMainFragment)
                    .getAdapter(MainFragmentAdapter.class);
            mMainFragmentAdapter.setFragmentHost(new FragmentHostImpl());

            mIsPageRow = savedInstanceState != null ?
                    savedInstanceState.getBoolean(IS_PAGE_ROW, false) : false;

            mSelectedPosition = savedInstanceState != null ?
                    savedInstanceState.getInt(CURRENT_SELECTED_POSITION, 0) : 0;

            if (!mIsPageRow) {
                mMainFragmentRowsAdapter = (MainFragmentRowsAdapter) ((Adaptable) mMainFragment)
                        .getAdapter(MainFragmentRowsAdapter.class);
            } else {
                mMainFragmentRowsAdapter = null;
            }
        }

        mHeadersSupportFragment.setHeadersGone(!mCanShowHeaders);
        if (mHeaderPresenterSelector != null) {
            mHeadersSupportFragment.setPresenterSelector(mHeaderPresenterSelector);
        }
        mHeadersSupportFragment.setAdapter(mAdapter);
        mHeadersSupportFragment.setOnHeaderViewSelectedListener(mHeaderViewSelectedListener);
        mHeadersSupportFragment.setOnHeaderClickedListener(mHeaderClickedListener);

        View root = inflater.inflate(R.layout.lb_browse_fragment, container, false);

        getProgressBarManager().setRootView((ViewGroup)root);

        setTitleView((TitleView) root.findViewById(R.id.browse_title_group));

        mBrowseFrame = (BrowseFrameLayout) root.findViewById(R.id.browse_frame);
        mBrowseFrame.setOnChildFocusListener(mOnChildFocusListener);
        mBrowseFrame.setOnFocusSearchListener(mOnFocusSearchListener);

        mScaleFrameLayout = (ScaleFrameLayout) root.findViewById(R.id.scale_frame);
        mScaleFrameLayout.setPivotX(0);
        mScaleFrameLayout.setPivotY(mContainerListAlignTop);

        setupMainFragment();

        if (mBrandColorSet) {
            mHeadersSupportFragment.setBackgroundColor(mBrandColor);
        }

        mSceneWithHeaders = TransitionHelper.createScene(mBrowseFrame, new Runnable() {
            @Override
            public void run() {
                showHeaders(true);
            }
        });
        mSceneWithoutHeaders =  TransitionHelper.createScene(mBrowseFrame, new Runnable() {
            @Override
            public void run() {
                showHeaders(false);
            }
        });
        mSceneAfterEntranceTransition = TransitionHelper.createScene(mBrowseFrame, new Runnable() {
            @Override
            public void run() {
                setEntranceTransitionEndState();
            }
        });

        return root;
    }

    private void setupMainFragment() {
        if (mMainFragmentRowsAdapter != null) {
            mMainFragmentRowsAdapter.setAdapter(mAdapter);
            mMainFragmentRowsAdapter.setOnItemViewSelectedListener(
                    new MainFragmentItemViewSelectedListener(mMainFragmentRowsAdapter));
            mMainFragmentRowsAdapter.setOnItemViewClickedListener(mOnItemViewClickedListener);
        }
    }

    @Override
    boolean isReadyForPrepareEntranceTransition() {
        return mMainFragment != null && mMainFragment.getView() != null;
    }

    @Override
    boolean isReadyForStartEntranceTransition() {
        return mMainFragment != null && mMainFragment.getView() != null;
    }

    void processingPendingEntranceTransition() {
        performPendingStates();
    }

    private void createHeadersTransition() {
        mHeadersTransition = TransitionHelper.loadTransition(getActivity(),
                mShowingHeaders ?
                R.transition.lb_browse_headers_in : R.transition.lb_browse_headers_out);

        TransitionHelper.addTransitionListener(mHeadersTransition, new TransitionListener() {
            @Override
            public void onTransitionStart(Object transition) {
            }
            @Override
            public void onTransitionEnd(Object transition) {
                mHeadersTransition = null;
                if (mMainFragmentAdapter != null) {
                    mMainFragmentAdapter.onTransitionEnd();
                    if (!mShowingHeaders && mMainFragment != null) {
                        View mainFragmentView = mMainFragment.getView();
                        if (mainFragmentView != null && !mainFragmentView.hasFocus()) {
                            mainFragmentView.requestFocus();
                        }
                    }
                }
                if (mHeadersSupportFragment != null) {
                    mHeadersSupportFragment.onTransitionEnd();
                    if (mShowingHeaders) {
                        VerticalGridView headerGridView = mHeadersSupportFragment.getVerticalGridView();
                        if (headerGridView != null && !headerGridView.hasFocus()) {
                            headerGridView.requestFocus();
                        }
                    }
                }

                // Animate TitleView once header animation is complete.
                if (!mShowingHeaders) {
                    if (mMainFragmentAdapter != null) {
                        if (((FragmentHostImpl)mMainFragmentAdapter.getFragmentHost())
                                .mShowTitleView) {
                            showTitle(TitleView.FULL_VIEW_VISIBLE);
                        } else {
                            showTitle(false);
                        }
                    }
                } else {
                    if (mSelectedPosition == 0) {
                        showTitle(TitleView.FULL_VIEW_VISIBLE);
                    } else {
                        if (mIsPageRow && mMainFragmentAdapter != null) {
                            if (((FragmentHostImpl)mMainFragmentAdapter.getFragmentHost())
                                    .mShowTitleView) {
                                showTitle(TitleView.BRANDING_VIEW_VISIBLE);
                            } else {
                                showTitle(false);
                            }
                        } else {
                            showTitle(false);
                        }
                    }
                }

                if (mBrowseTransitionListener != null) {
                    mBrowseTransitionListener.onHeadersTransitionStop(mShowingHeaders);
                }
            }
        });
    }

    /**
     * Sets the {@link PresenterSelector} used to render the row headers.
     *
     * @param headerPresenterSelector The PresenterSelector that will determine
     *        the Presenter for each row header.
     */
    public void setHeaderPresenterSelector(PresenterSelector headerPresenterSelector) {
        mHeaderPresenterSelector = headerPresenterSelector;
        if (mHeadersSupportFragment != null) {
            mHeadersSupportFragment.setPresenterSelector(mHeaderPresenterSelector);
        }
    }

    private void setHeadersOnScreen(boolean onScreen) {
        MarginLayoutParams lp;
        View containerList;
        containerList = mHeadersSupportFragment.getView();
        lp = (MarginLayoutParams) containerList.getLayoutParams();
        lp.setMarginStart(onScreen ? 0 : -mContainerListMarginStart);
        containerList.setLayoutParams(lp);
    }

    private void showHeaders(boolean show) {
        if (DEBUG) Log.v(TAG, "showHeaders " + show);
        mHeadersSupportFragment.setHeadersEnabled(show);
        setHeadersOnScreen(show);
        expandMainFragment(!show);
    }

    private void expandMainFragment(boolean expand) {
        MarginLayoutParams params = (MarginLayoutParams) mScaleFrameLayout.getLayoutParams();
        params.leftMargin = !expand ? mContainerListMarginStart : 0;
        mScaleFrameLayout.setLayoutParams(params);
        mMainFragmentAdapter.setExpand(expand);

        setMainFragmentAlignment();
        final float scaleFactor = !expand
                && mMainFragmentScaleEnabled
                && mMainFragmentAdapter.isScalingEnabled() ? mScaleFactor : 1;
        mScaleFrameLayout.setLayoutScaleY(scaleFactor);
        mScaleFrameLayout.setChildScale(scaleFactor);
    }

    private HeadersSupportFragment.OnHeaderClickedListener mHeaderClickedListener =
        new HeadersSupportFragment.OnHeaderClickedListener() {
            @Override
            public void onHeaderClicked(RowHeaderPresenter.ViewHolder viewHolder, Row row) {
                if (!mCanShowHeaders || !mShowingHeaders || isInHeadersTransition()) {
                    return;
                }
                startHeadersTransitionInternal(false);
                mMainFragment.getView().requestFocus();
            }
        };

    class MainFragmentItemViewSelectedListener implements OnItemViewSelectedListener {
        MainFragmentRowsAdapter mMainFragmentRowsAdapter;

        public MainFragmentItemViewSelectedListener(MainFragmentRowsAdapter fragmentRowsAdapter) {
            mMainFragmentRowsAdapter = fragmentRowsAdapter;
        }

        @Override
        public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item,
                RowPresenter.ViewHolder rowViewHolder, Row row) {
            int position = mMainFragmentRowsAdapter.getSelectedPosition();
            if (DEBUG) Log.v(TAG, "row selected position " + position);
            onRowSelected(position);
            if (mExternalOnItemViewSelectedListener != null) {
                mExternalOnItemViewSelectedListener.onItemSelected(itemViewHolder, item,
                        rowViewHolder, row);
            }
        }
    };

    private HeadersSupportFragment.OnHeaderViewSelectedListener mHeaderViewSelectedListener =
            new HeadersSupportFragment.OnHeaderViewSelectedListener() {
        @Override
        public void onHeaderSelected(RowHeaderPresenter.ViewHolder viewHolder, Row row) {
            int position = mHeadersSupportFragment.getSelectedPosition();
            if (DEBUG) Log.v(TAG, "header selected position " + position);
            onRowSelected(position);
        }
    };

    private void onRowSelected(int position) {
        if (position != mSelectedPosition) {
            mSetSelectionRunnable.post(
                    position, SetSelectionRunnable.TYPE_INTERNAL_SYNC, true);
        }
    }

    private void setSelection(int position, boolean smooth) {
        if (position == NO_POSITION) {
            return;
        }

        mHeadersSupportFragment.setSelectedPosition(position, smooth);
        replaceMainFragment(position);

        if (mMainFragmentRowsAdapter != null) {
            mMainFragmentRowsAdapter.setSelectedPosition(position, smooth);
        }
        mSelectedPosition = position;

        if (getAdapter() == null || getAdapter().size() == 0 || position == 0) {
            showTitle(TitleView.FULL_VIEW_VISIBLE);
        } else {
            if (mIsPageRow) {
                showTitle(TitleView.BRANDING_VIEW_VISIBLE);
            }
            else {
                showTitle(false);
            }
        }
    }

    private void replaceMainFragment(int position) {
        if (createMainFragment(mAdapter, position)) {
            swapBrowseContent(mMainFragment);
            expandMainFragment(!(mCanShowHeaders && mShowingHeaders));
            setupMainFragment();
            processingPendingEntranceTransition();
        }
    }

    private void swapBrowseContent(Fragment fragment) {
        getChildFragmentManager().beginTransaction().replace(R.id.scale_frame, fragment).commit();
    }

    /**
     * Sets the selected row position with smooth animation.
     */
    public void setSelectedPosition(int position) {
        setSelectedPosition(position, true);
    }

    /**
     * Gets position of currently selected row.
     * @return Position of currently selected row.
     */
    public int getSelectedPosition() {
        return mSelectedPosition;
    }

    /**
     * Sets the selected row position.
     */
    public void setSelectedPosition(int position, boolean smooth) {
        mSetSelectionRunnable.post(
                position, SetSelectionRunnable.TYPE_USER_REQUEST, smooth);
    }

    /**
     * Selects a Row and perform an optional task on the Row. For example
     * <code>setSelectedPosition(10, true, new ListRowPresenterSelectItemViewHolderTask(5))</code>
     * scrolls to 11th row and selects 6th item on that row.  The method will be ignored if
     * RowsSupportFragment has not been created (i.e. before {@link #onCreateView(LayoutInflater,
     * ViewGroup, Bundle)}).
     *
     * @param rowPosition Which row to select.
     * @param smooth True to scroll to the row, false for no animation.
     * @param rowHolderTask Optional task to perform on the Row.  When the task is not null, headers
     * fragment will be collapsed.
     */
    public void setSelectedPosition(int rowPosition, boolean smooth,
            final Presenter.ViewHolderTask rowHolderTask) {
        if (mMainFragmentAdapterRegistry == null) {
            return;
        }
        if (rowHolderTask != null) {
            startHeadersTransition(false);
        }
        if (mMainFragmentRowsAdapter != null) {
            mMainFragmentRowsAdapter.setSelectedPosition(rowPosition, smooth, rowHolderTask);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mHeadersSupportFragment.setAlignment(mContainerListAlignTop);
        setMainFragmentAlignment();

        if (mCanShowHeaders && mShowingHeaders && mHeadersSupportFragment.getView() != null) {
            mHeadersSupportFragment.getView().requestFocus();
        } else if ((!mCanShowHeaders || !mShowingHeaders)
                && mMainFragment.getView() != null) {
            mMainFragment.getView().requestFocus();
        }

        if (mCanShowHeaders) {
            showHeaders(mShowingHeaders);
        }

        if (isEntranceTransitionEnabled()) {
            setEntranceTransitionStartState();
        }
    }

    private void onExpandTransitionStart(boolean expand, final Runnable callback) {
        if (expand) {
            callback.run();
            return;
        }
        // Run a "pre" layout when we go non-expand, in order to get the initial
        // positions of added rows.
        new ExpandPreLayout(callback, mMainFragmentAdapter, getView()).execute();
    }

    private void setMainFragmentAlignment() {
        int alignOffset = mContainerListAlignTop;
        if (mMainFragmentScaleEnabled
                && mMainFragmentAdapter.isScalingEnabled()
                && mShowingHeaders) {
            alignOffset = (int) (alignOffset / mScaleFactor + 0.5f);
        }
        mMainFragmentAdapter.setAlignment(alignOffset);
    }

    /**
     * Enables/disables headers transition on back key support. This is enabled by
     * default. The BrowseSupportFragment will add a back stack entry when headers are
     * showing. Running a headers transition when the back key is pressed only
     * works when the headers state is {@link #HEADERS_ENABLED} or
     * {@link #HEADERS_HIDDEN}.
     * <p>
     * NOTE: If an Activity has its own onBackPressed() handling, you must
     * disable this feature. You may use {@link #startHeadersTransition(boolean)}
     * and {@link BrowseTransitionListener} in your own back stack handling.
     */
    public final void setHeadersTransitionOnBackEnabled(boolean headersBackStackEnabled) {
        mHeadersBackStackEnabled = headersBackStackEnabled;
    }

    /**
     * Returns true if headers transition on back key support is enabled.
     */
    public final boolean isHeadersTransitionOnBackEnabled() {
        return mHeadersBackStackEnabled;
    }

    private void readArguments(Bundle args) {
        if (args == null) {
            return;
        }
        if (args.containsKey(ARG_TITLE)) {
            setTitle(args.getString(ARG_TITLE));
        }
        if (args.containsKey(ARG_HEADERS_STATE)) {
            setHeadersState(args.getInt(ARG_HEADERS_STATE));
        }
    }

    /**
     * Sets the state for the headers column in the browse fragment. Must be one
     * of {@link #HEADERS_ENABLED}, {@link #HEADERS_HIDDEN}, or
     * {@link #HEADERS_DISABLED}.
     *
     * @param headersState The state of the headers for the browse fragment.
     */
    public void setHeadersState(int headersState) {
        if (headersState < HEADERS_ENABLED || headersState > HEADERS_DISABLED) {
            throw new IllegalArgumentException("Invalid headers state: " + headersState);
        }
        if (DEBUG) Log.v(TAG, "setHeadersState " + headersState);

        if (headersState != mHeadersState) {
            mHeadersState = headersState;
            switch (headersState) {
                case HEADERS_ENABLED:
                    mCanShowHeaders = true;
                    mShowingHeaders = true;
                    break;
                case HEADERS_HIDDEN:
                    mCanShowHeaders = true;
                    mShowingHeaders = false;
                    break;
                case HEADERS_DISABLED:
                    mCanShowHeaders = false;
                    mShowingHeaders = false;
                    break;
                default:
                    Log.w(TAG, "Unknown headers state: " + headersState);
                    break;
            }
            if (mHeadersSupportFragment != null) {
                mHeadersSupportFragment.setHeadersGone(!mCanShowHeaders);
            }
        }
    }

    /**
     * Returns the state of the headers column in the browse fragment.
     */
    public int getHeadersState() {
        return mHeadersState;
    }

    @Override
    protected Object createEntranceTransition() {
        return TransitionHelper.loadTransition(getActivity(),
                R.transition.lb_browse_entrance_transition);
    }

    @Override
    protected void runEntranceTransition(Object entranceTransition) {
        TransitionHelper.runTransition(mSceneAfterEntranceTransition, entranceTransition);
    }

    @Override
    protected void onEntranceTransitionPrepare() {
        mHeadersSupportFragment.onTransitionPrepare();
        // setEntranceTransitionStartState() might be called when mMainFragment is null,
        // make sure it is called.
        mMainFragmentAdapter.setEntranceTransitionState(false);
        mMainFragmentAdapter.onTransitionPrepare();
    }

    @Override
    protected void onEntranceTransitionStart() {
        mHeadersSupportFragment.onTransitionStart();
        mMainFragmentAdapter.onTransitionStart();
    }

    @Override
    protected void onEntranceTransitionEnd() {
        if (mMainFragmentAdapter != null) {
            mMainFragmentAdapter.onTransitionEnd();
        }

        if (mHeadersSupportFragment != null) {
            mHeadersSupportFragment.onTransitionEnd();
        }
    }

    void setSearchOrbViewOnScreen(boolean onScreen) {
        View searchOrbView = getTitleView().getSearchAffordanceView();
        MarginLayoutParams lp = (MarginLayoutParams) searchOrbView.getLayoutParams();
        lp.setMarginStart(onScreen ? 0 : -mContainerListMarginStart);
        searchOrbView.setLayoutParams(lp);
    }

    void setEntranceTransitionStartState() {
        setHeadersOnScreen(false);
        setSearchOrbViewOnScreen(false);
        mMainFragmentAdapter.setEntranceTransitionState(false);
    }

    void setEntranceTransitionEndState() {
        setHeadersOnScreen(mShowingHeaders);
        setSearchOrbViewOnScreen(true);
        mMainFragmentAdapter.setEntranceTransitionState(true);
    }

    private class ExpandPreLayout implements ViewTreeObserver.OnPreDrawListener {

        private final View mView;
        private final Runnable mCallback;
        private int mState;
        private MainFragmentAdapter mainFragmentAdapter;

        final static int STATE_INIT = 0;
        final static int STATE_FIRST_DRAW = 1;
        final static int STATE_SECOND_DRAW = 2;

        ExpandPreLayout(Runnable callback, MainFragmentAdapter adapter, View view) {
            mView = view;
            mCallback = callback;
            mainFragmentAdapter = adapter;
        }

        void execute() {
            mView.getViewTreeObserver().addOnPreDrawListener(this);
            mainFragmentAdapter.setExpand(false);
            mState = STATE_INIT;
        }

        @Override
        public boolean onPreDraw() {
            if (getView() == null || getActivity() == null) {
                mView.getViewTreeObserver().removeOnPreDrawListener(this);
                return true;
            }
            if (mState == STATE_INIT) {
                mainFragmentAdapter.setExpand(true);
                mState = STATE_FIRST_DRAW;
            } else if (mState == STATE_FIRST_DRAW) {
                mCallback.run();
                mView.getViewTreeObserver().removeOnPreDrawListener(this);
                mState = STATE_SECOND_DRAW;
            }
            return false;
        }
    }
}
