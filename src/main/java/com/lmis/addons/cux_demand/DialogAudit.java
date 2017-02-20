package com.lmis.addons.cux_demand;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.lmis.R;
import com.lmis.orm.LmisDataRow;

/**
 * Created by chengdh on 2017/2/19.
 */

public class DialogAudit extends DialogFragment {

    EditText mEdtAuditNote;
    /* The activity that creates an instance of this dialog fragment must
         * implement this interface in order to receive event callbacks.
         * Each method passes the DialogFragment in case the host needs to query it. */
    public interface NoticeDialogListener {
        public void onDialogPositiveClick(DialogAudit dialog);

        public void onDialogNegativeClick(DialogAudit dialog);
    }

    // Use this instance of the interface to deliver action events
    NoticeDialogListener mListener;
    LmisDataRow mBill;

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
        View view = inflater.inflate(R.layout.dialog_audit, null);


        mEdtAuditNote = (EditText) view.findViewById(R.id.edt_audit);
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view)
                // Add action buttons
                .setPositiveButton(R.string.confirm_audit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDialogPositiveClick(DialogAudit.this);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        DialogAudit.this.getDialog().cancel();
                        mListener.onDialogNegativeClick(DialogAudit.this);
                    }
                });
        return builder.create();
    }
    public String getAuditNote(){
        return mEdtAuditNote.getText().toString();

    }
}
