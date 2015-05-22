package com.dxd.barcodescanner;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class MessageDialogFragment extends DialogFragment {
    private String mTitle;
    private String mMessage;
    private MessageDialogListener mListener;

    public static MessageDialogFragment newInstance(String title, String message, MessageDialogListener listener) {
        MessageDialogFragment fragment = new MessageDialogFragment();
        fragment.mTitle = title;
        fragment.mMessage = message;
        fragment.mListener = listener;
        return fragment;
    }

    public void onCreate(Bundle state) {
        super.onCreate(state);
        setRetainInstance(true);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(mMessage)
                .setTitle(mTitle);

        builder.setNegativeButton("Scan Again", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (mListener != null) {
                    mListener.onDialogPositiveClick(MessageDialogFragment.this);
                }
            }
        })
                .setPositiveButton("Search Product", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(getActivity(), ProductSearch.class);
                        intent.putExtra("barcode", mMessage);
                        startActivity(intent);

                    }
                });

        return builder.create();
    }

    public interface MessageDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog);
    }
}
