package com.example.mydocsapp;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NavUtils;

import com.example.mydocsapp.databinding.ActivityTemplateBinding;

import java.util.ArrayList;
import java.util.List;

public class TemplateActivity extends AppCompatActivity {

    private static final int DIALOG_EDITTEXT = 1;
    private static final int DIALOG_NUMBER_TEXT = 2;
    private static final int DIALOG_CHECKBOX = 3;
    ActivityTemplateBinding binding;
    List<View> showedLayouts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTemplateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        showedLayouts = new ArrayList<>();
        setOnClickListeners();
    }

    @Override
    public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(this);
        super.onBackPressed();
    }
    private void setOnClickListeners() {
        binding.menubarBack.setOnClickListener(v->{onBackPressed();});
        binding.bottomAddEdittextPanel.setOnClickListener(v->{getTitleDialog(DIALOG_EDITTEXT);});
        binding.bottomAddNumberTextPanel.setOnClickListener(v->{getTitleDialog(DIALOG_NUMBER_TEXT);});
        binding.bottomAddCheckboxPanel.setOnClickListener(v->{getTitleDialog(DIALOG_CHECKBOX);});
        binding.bottomAddPhotoPanel.setOnClickListener(v->{makePhoto();});
    }


    private void makeEditText(String dialogTitleText, boolean isNumeric) {
        ConstraintLayout layout = (ConstraintLayout) getLayoutInflater().inflate(R.layout.template_edittext_layout, null);
        TextView titleTxt = layout.findViewById(R.id.title_txt);
        titleTxt.setText(dialogTitleText);
        if(isNumeric){
            EditText editText = layout.findViewById(R.id.content_txt);
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        }
        ImageButton deleteBtn = layout.findViewById(R.id.delete_btn);
        deleteBtn.setOnClickListener(v->{
            showedLayouts.remove(layout);
            binding.mainContentPanel.removeView(layout);
        });
        binding.mainContentPanel.addView(layout);
    }

    private void makePhoto() {
    }

    private void getTitleDialog(int mode) {
        final String title;
        Dialog makeRenameDialog = new Dialog(TemplateActivity.this);
        makeRenameDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        makeRenameDialog.setContentView(R.layout.make_rename_layout);
        makeRenameDialog.setCancelable(true);
        makeRenameDialog.setCanceledOnTouchOutside(false);
        makeRenameDialog.getWindow().getAttributes().windowAnimations = R.style.MakeRenameDialogAnimation;

        EditText editText = makeRenameDialog.findViewById(R.id.folderNameTxt);
        editText.setSelection(editText.getText().length());
        editText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
        editText.setHint(R.string.create_a_folder);

        ((Button) makeRenameDialog.findViewById(R.id.make_rename_item_btn)).setText(R.string.create_a_folder);

        makeRenameDialog.findViewById(R.id.make_rename_item_btn).setOnClickListener(view -> {
            if (editText.getText().toString().replace(" ","").equals(""))
                return;
            else{
                switch (mode){
                    case DIALOG_EDITTEXT:
                        makeEditText(editText.getText().toString(), false);
                        break;
                    case DIALOG_NUMBER_TEXT:
                        makeEditText(editText.getText().toString(), true);
                        break;
                    case DIALOG_CHECKBOX:
                        makeCheckBox(editText.getText().toString());
                        break;
                }
                makeRenameDialog.dismiss();
            }
        });
        makeRenameDialog.findViewById(R.id.cancel_btn).setOnClickListener(view -> {
            makeRenameDialog.dismiss();
        });
        makeRenameDialog.show();
    }
    private void makeCheckBox(String dialogTitleText) {
        ConstraintLayout layout = (ConstraintLayout) getLayoutInflater().inflate(R.layout.template_checkbox_layout, null);
        CheckBox titleTxt = layout.findViewById(R.id.title_txt);
        titleTxt.setText(dialogTitleText);
        ImageButton deleteBtn = layout.findViewById(R.id.delete_btn);
        deleteBtn.setOnClickListener(v->{
            showedLayouts.remove(layout);
            binding.mainContentPanel.removeView(layout);
        });
        binding.mainContentPanel.addView(layout);
    }
}