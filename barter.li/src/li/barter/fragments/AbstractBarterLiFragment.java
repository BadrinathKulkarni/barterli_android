/**
 * Copyright 2014, barter.li
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package li.barter.fragments;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import li.barter.R;
import li.barter.activities.AbstractBarterLiActivity;
import li.barter.http.HttpConstants;
import li.barter.http.IBlRequestContract;
import li.barter.http.IVolleyHelper;
import li.barter.http.ResponseInfo;
import li.barter.http.VolleyCallbacks;
import li.barter.http.VolleyCallbacks.IHttpCallbacks;
import li.barter.utils.AppConstants.Keys;
import li.barter.utils.AppConstants.UserInfo;
import li.barter.widgets.TypefaceCache;

/**
 * Base fragment class to encapsulate common functionality. Call the init()
 * method in the onCreateView() of your fragments
 * 
 * @author Vinay S Shenoy
 */
public abstract class AbstractBarterLiFragment extends Fragment implements
                IHttpCallbacks {

    private static final String TAG = "BaseBarterLiFragment";

    /**
     * Flag that indicates that this fragment is attached to an Activity
     */
    private boolean             mIsAttached;

    /**
     * Stores the id for the container view
     */
    protected int               mContainerViewId;

    /**
     * {@link VolleyCallbacks} for encapsulating the Volley response flow
     */
    protected VolleyCallbacks   mVolleyCallbacks;

    private ImageLoader         mImageLoader;
    private AtomicInteger       mRequestCounter;

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        mIsAttached = true;
        final RequestQueue requestQueue = ((IVolleyHelper) activity
                        .getApplication()).getRequestQueue();
        mImageLoader = ((IVolleyHelper) activity.getApplication())
                        .getImageLoader();
        mVolleyCallbacks = new VolleyCallbacks(requestQueue, this);
        mRequestCounter = new AtomicInteger(0);
    }

    /**
     * Call this method in the onCreateView() of any subclasses
     * 
     * @param container The container passed into onCreateView()
     */
    protected void init(final ViewGroup container) {
        mContainerViewId = container.getId();
    }

    protected void setActionBarDrawerToggleEnabled(final boolean enabled) {
        final AbstractBarterLiActivity activity = (AbstractBarterLiActivity) getActivity();
        if (activity.hasNavigationDrawer()) {
            activity.setActionBarDrawerToggleEnabled(enabled);
        }
    }

    /**
     * Helper method to load fragments into layout
     * 
     * @param containerResId The container resource Id in the content view into
     *            which to load the fragment
     * @param fragment The fragment to load
     * @param tag The fragment tag
     * @param addToBackStack Whether the transaction should be addded to the
     *            backstack
     * @param backStackTag The tag used for the backstack tag
     */
    public void loadFragment(final int containerResId,
                    final AbstractBarterLiFragment fragment, final String tag,
                    final boolean addToBackStack, final String backStackTag) {

        if (mIsAttached) {
            ((AbstractBarterLiActivity) getActivity())
                            .loadFragment(containerResId, fragment, tag, addToBackStack, backStackTag);
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mContainerViewId = 0;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mIsAttached = false;
        mVolleyCallbacks = null;
        mImageLoader = null;
        mRequestCounter = null;
    }

    /**
     * Is the device connected to a network or not.
     * 
     * @return <code>true</code> if connected, <code>false</code> otherwise
     */
    public boolean isConnectedToInternet() {
        return ((AbstractBarterLiActivity) getActivity())
                        .isConnectedToInternet();
    }

    public void setActionBarDisplayOptions(final int displayOptions) {
        if (mIsAttached) {

            ((AbstractBarterLiActivity) getActivity())
                            .setActionBarDisplayOptions(displayOptions);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mVolleyCallbacks.cancelAll(getVolleyTag());
        getActivity().setProgressBarIndeterminateVisibility(false);
    }

    /**
     * Reference to the {@link ImageLoader}
     * 
     * @return The {@link ImageLoader} for loading images from ntwork
     */
    protected ImageLoader getImageLoader() {
        return mImageLoader;
    }

    /**
     * Add a request on the network queue
     * 
     * @param request The {@link Request} to add
     * @param showErrorOnNoNetwork Whether an error toast should be displayed on
     *            no internet connection
     * @param errorMsgResId String resource Id for error message to show if no
     *            internet connection, 0 for a default error message
     */
    protected void addRequestToQueue(final Request<?> request,
                    final boolean showErrorOnNoNetwork, final int errorMsgResId) {

        if (mIsAttached) {
            request.setTag(getVolleyTag());
            if (isConnectedToInternet()) {
                addHeadersToRequest(request);
                mRequestCounter.incrementAndGet();
                getActivity().setProgressBarIndeterminateVisibility(true);
                mVolleyCallbacks.queue(request);
            } else if (showErrorOnNoNetwork) {
                showToast(errorMsgResId != 0 ? errorMsgResId
                                : R.string.no_network_connection, false);
            }
        }
    }

    /**
     * Add Request Headers to the headers
     * 
     * @param request The request to add the headers to
     */
    private void addHeadersToRequest(Request<?> request) {

        final Map<String, String> headers = new HashMap<String, String>(1);
        headers.put(HttpConstants.HEADER_AUTHORIZATION, UserInfo.INSTANCE
                        .getAuthHeader());
        request.setHeaders(headers);
    }

    /**
     * A Tag to add to all Volley requests. This must be unique for all
     * Fragments types
     * 
     * @return An Object that's the tag for this fragment
     */
    protected abstract Object getVolleyTag();

    /**
     * Display a {@link Toast} message
     * 
     * @param toastMessage The message to display
     * @param isLong Whether it is a long toast
     */
    public void showToast(final String toastMessage, final boolean isLong) {
        if (mIsAttached) {
            ((AbstractBarterLiActivity) getActivity())
                            .showToast(toastMessage, isLong);
        }
    }

    /**
     * Display a {@link Toast} message
     * 
     * @param toastMessageResId The message string resource Id to display
     * @param isLong Whether it is a long toast
     */
    public void showToast(final int toastMessageResId, final boolean isLong) {
        if (mIsAttached) {
            ((AbstractBarterLiActivity) getActivity())
                            .showToast(toastMessageResId, isLong);
        }
    }

    /**
     * Whether this Fragment is currently attached to an Activity
     * 
     * @return <code>true</code> if attached, <code>false</code> otherwise
     */
    public boolean isAttached() {
        return mIsAttached;
    }

    /**
     * Sets the Action bar title, using the desired {@link Typeface} loaded from
     * {@link TypefaceCache}
     * 
     * @param title The title to set for the Action Bar
     */
    public final void setActionBarTitle(final String title) {

        if (mIsAttached) {
            ((AbstractBarterLiActivity) getActivity()).setActionBarTitle(title);
        }
    }

    /**
     * Sets the Action bar title, using the desired {@link Typeface} loaded from
     * {@link TypefaceCache}
     * 
     * @param titleResId The title string resource Id to set for the Action Bar
     */
    public final void setActionBarTitle(final int titleResId) {
        setActionBarTitle(getString(titleResId));
    }

    /**
     * Is the user logged in
     */
    protected boolean isLoggedIn() {
        return !TextUtils.isEmpty(UserInfo.INSTANCE.getAuthToken());
    }

    /**
     * Pops the fragment from the backstack, checking to see if the bundle args
     * have {@linkplain Keys#BACKSTACK_TAG} which gives the name of the
     * backstack tag to pop to. This is mainly for providing Up navigation
     */
    public void onUpNavigate() {
        final Bundle args = getArguments();

        if ((args != null) && args.containsKey(Keys.BACKSTACK_TAG)) {
            getFragmentManager()
                            .popBackStack(args.getString(Keys.BACKSTACK_TAG), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        } else {
            getFragmentManager().popBackStack();
        }
    }

    /**
     * Handles the behaviour for onBackPressed() Default behavious is to pop the
     * frament manager's backstack. Child fragments must override this if they
     * wish to provide custom behaviour
     */
    public void onBackPressed() {

        getFragmentManager().popBackStack();
    }

    @Override
    public void onPreExecute(IBlRequestContract request) {
        mRequestCounter.incrementAndGet();
        if (mIsAttached) {
            getActivity().setProgressBarIndeterminateVisibility(true);
        }
    }

    @Override
    public void onPostExecute(IBlRequestContract request) {
        if (mRequestCounter.decrementAndGet() == 0 && mIsAttached) {
            getActivity().setProgressBarIndeterminateVisibility(false);
        }
    }

    @Override
    public abstract void onSuccess(int requestId, IBlRequestContract request,
                    ResponseInfo response);

    @Override
    public abstract void onBadRequestError(int requestId,
                    IBlRequestContract request, int errorCode,
                    String errorMessage, Bundle errorResponseBundle);

    @Override
    public void onAuthError(int requestId, IBlRequestContract request) {
        //TODO Show Login Fragment and ask user to login again
    }

    @Override
    public void onOtherError(int requestId, IBlRequestContract request,
                    int errorCode) {
        //TODO Show generic network error message
    }

}
