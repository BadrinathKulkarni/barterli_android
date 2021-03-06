/*******************************************************************************
 * Copyright 2014,  barter.li
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
 ******************************************************************************/

package li.barter.fragments.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;

import li.barter.utils.Logger;

/**
 * Single Choice Alert Dialog.
 * 
 * @author Vinay S Shenoy
 */
public class SingleChoiceDialogFragment extends DialogFragment {

    private static final String TAG = "SingleChoiceDialogFragment";

    /** Res Id for the Dialog Title. */
    private int                 mTitleId;

    /** Click Listener for the Dialog buttons. */
    private OnClickListener     mClickListener;

    /** On Dismiss Listener for the Dialog */
    private OnDismissListener   mOnDismissListener;

    /** Resource Id for the icon to be be used in the alert dialog. */
    private int                 mIconId;

    /** Boolean flag to identify if the dialog is cancelable. */
    private boolean             isCancellable;

    /** {@linkplain AlertDialog}'s THEME constants for the dialog theme. */
    private int                 mTheme;

    /**
     * Resource Id for an array to be added to the dialog items
     */
    private int                 mItemsId;

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            mTitleId = savedInstanceState.getInt(DialogKeys.TITLE_ID);
            isCancellable = savedInstanceState
                            .getBoolean(DialogKeys.CANCELLABLE);
            mIconId = savedInstanceState.getInt(DialogKeys.ICON_ID);
            mTheme = savedInstanceState.getInt(DialogKeys.THEME);
            mItemsId = savedInstanceState.getInt(DialogKeys.ITEMS_ID);
        }

        final Builder builder = new Builder(getActivity(), mTheme);

        if (mItemsId == 0) {
            throw new IllegalArgumentException("No items available to add");
        }

        builder.setItems(mItemsId, mClickListener);

        if (mIconId != 0) {
            builder.setIcon(mIconId);
        }
        if (mTitleId != 0) {
            builder.setTitle(mTitleId);
        }

        builder.setCancelable(isCancellable);
        setCancelable(isCancellable);
        return builder.create();
    }

    @Override
    public void onAttach(final Activity activity) {

        super.onAttach(activity);

        if (activity instanceof OnClickListener) {
            mClickListener = (OnClickListener) activity;
        }

        else {
            throw new IllegalStateException("Activity must implement DialogInterface.OnClickListener");
        }

        if (activity instanceof OnDismissListener) {
            mOnDismissListener = (OnDismissListener) activity;
        }
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mOnDismissListener != null) {
            mOnDismissListener.onDismiss(dialog);
        }
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {

        outState.putInt(DialogKeys.TITLE_ID, mTitleId);
        outState.putBoolean(DialogKeys.CANCELLABLE, isCancellable);
        outState.putInt(DialogKeys.ICON_ID, mIconId);
        outState.putInt(DialogKeys.THEME, mTheme);
        outState.putInt(DialogKeys.ITEMS_ID, mItemsId);
        super.onSaveInstanceState(outState);
    }

    public void show(final int theme, final int itemsId, final int iconId,
                    final int titleId, final FragmentManager manager,
                    final boolean cancellable, final String fragmentTag) {

        mTheme = theme;
        mItemsId = itemsId;
        mIconId = iconId;
        mTitleId = titleId;
        isCancellable = cancellable;

        try {
            super.show(manager, fragmentTag);
        } catch (final IllegalStateException e) {
            Logger.e(TAG, e, "Exception");
        }

    }

}
