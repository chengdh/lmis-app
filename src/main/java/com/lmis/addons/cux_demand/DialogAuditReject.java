package com.lmis.addons.cux_demand;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;

import com.lmis.R;
import com.lmis.orm.LmisDataRow;

/**
 * Created by chengdh on 2017/2/19.
 */

public class DialogAuditReject extends DialogFragment {

    /* The activity that creates an instance of this dialog fragment must
         * implement this interface in order to receive event callbacks.
         * Each method passes the DialogFragment in case the host needs to query it. */
    public interface NoticeDialogListener {
        public void onRejectDialogPositiveClick(DialogAuditReject dialog);

        public void onRejectDialogNegativeClick(DialogAuditReject dialog);
    }

    // Use this instance of the interface to deliver action events
    NoticeDialogListener mListener;

    public NoticeDialogListener getmListener() {
        return mListener;
    }

    public void setmListener(NoticeDialogListener mListener) {
        this.mListener = mListener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.dialog_audit_reject, null))
                // Add action buttons
                .setPositiveButton(R.string.confirm_audit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onRejectDialogPositiveClick(DialogAuditReject.this);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        DialogAuditReject.this.getDialog().cancel();
                        mListener.onRejectDialogNegativeClick(DialogAuditReject.this);
                    }
                });
        return builder.create();
    }
}
