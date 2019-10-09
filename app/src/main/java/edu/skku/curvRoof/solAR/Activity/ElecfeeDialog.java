package edu.skku.curvRoof.solAR.Activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import edu.skku.curvRoof.solAR.R;

public class ElecfeeDialog extends AppCompatDialogFragment {
    private EditText editFee;
    private ElecfeeDialogListner listner;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_elecfee, null);

        builder.setView(view)
                .setTitle("평균 전기세 입력")
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {

                    }
                })
                .setPositiveButton("확인",  new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        String elecfee = editFee.getText().toString();
                        listner.applyTexts((elecfee));
                    }
                });
        editFee = (EditText) view.findViewById(R.id.elecfee);

        return builder.create();
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);

        try {
            listner = (ElecfeeDialogListner) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement ElecfeeDialogListener");
        }
    }

    public interface  ElecfeeDialogListner{
        void applyTexts(String elecfee);
    }
}
