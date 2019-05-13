package com.example.leedaehyung.smartbottle;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

/**
 * Created by Lee DaeHyung on 2019-05-06.
 */

public class PurposeDialog extends DialogFragment {
    private EditText purposeWater;
    private NameInputListener listener;

    public static PurposeDialog newInstance(NameInputListener listener){
        PurposeDialog dialog = new PurposeDialog();
        dialog.listener = listener;
        return dialog;
    }
    public interface NameInputListener {
        void onNameInputComplete(String purpose);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder= new AlertDialog.Builder(getActivity());
        LayoutInflater inflater= getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.content_dialogsetpurpose,null);
        purposeWater = view.findViewById(R.id.weightSet);
        builder.setView(view).setTitle("목표량설정")
                .setNegativeButton("취소", null)
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onNameInputComplete(purposeWater.getText().toString());
                    }
                });

        return builder.create();
    }
}
