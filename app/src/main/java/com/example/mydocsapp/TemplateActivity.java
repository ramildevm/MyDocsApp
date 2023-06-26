package com.example.mydocsapp;

import static com.example.mydocsapp.services.AppService.NULL_UUID;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import com.example.mydocsapp.apputils.ImageService;
import com.example.mydocsapp.apputils.MyEncrypter;
import com.example.mydocsapp.apputils.PDFMaker;
import com.example.mydocsapp.databinding.ActivityTemplateBinding;
import com.example.mydocsapp.models.Item;
import com.example.mydocsapp.models.Template;
import com.example.mydocsapp.models.TemplateDocument;
import com.example.mydocsapp.models.TemplateDocumentData;
import com.example.mydocsapp.models.TemplateObject;
import com.example.mydocsapp.services.AppService;
import com.example.mydocsapp.services.DBHelper;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class TemplateActivity extends AppCompatActivity {
    private static final int DIALOG_EDITTEXT = 1;
    private static final int DIALOG_NUMBER_TEXT = 2;
    private static final int DIALOG_CHECKBOX = 3;
    private static final int DIALOG_TEMPLATE_NAME = 4;
    private static final int DIALOG_IMAGE = 5;
    private static final int DB_IMAGE = 1;
    ActivityTemplateBinding binding;
    List<TemplateObject> templateObjects;
    DBHelper db;
    Template template = null;
    TemplateDocument templateDoc = null;
    boolean isCreateDocumentMode = false;
    boolean isCreateTemplateMode = false;
    boolean isReviewTemplateMode = false;
    boolean isReviewUserTemplateMode = false;
    boolean isUpdateDocumentMode = false;
    private boolean isShowing = false;
    private int userId;
    Map<TemplateObject, View> templateViews = new HashMap<>();
    Map<View, Bitmap> templateViewBitmaps = new HashMap<>();
    private boolean isReview;
    private ActivityResultLauncher<Intent> registerForARImage;
    private View currentLoadPhotoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTemplateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        templateObjects = new ArrayList<>();
        userId = AppService.getUserId(this);
        db = new DBHelper(this, userId);
        template = getIntent().getParcelableExtra("template");
        templateDoc = getIntent().getParcelableExtra("document");
        isReview = getIntent().getIntExtra("isReview", 0) == 1;
        setCheckParameters();
        setWindowData();
        registerForARImage = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                CropImage.ActivityResult cropImageResult = CropImage.getActivityResult(result.getData());
                if (result.getData() != null) {
                    final Uri uri = cropImageResult.getUri();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                        Bitmap decoded = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));
                        loadPhotoFromBitmap(decoded);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            currentLoadPhotoView = null;
        });
        if(isCreateDocumentMode)
            setTopPatternPanelData();
        setOnClickListeners();
    }
    private void loadPhotoFromBitmap(Bitmap bitmap) {
        ImageView imageView = (ImageView) currentLoadPhotoView;
        templateViewBitmaps.put(imageView,bitmap);
        bitmap = ImageService.scaleDown(bitmap, ImageService.dpToPx(this, imageView.getWidth()), true);
        imageView.setBackgroundColor(Color.TRANSPARENT);
        imageView.setImageBitmap(bitmap);
    }
    private void setCheckParameters() {
        if (templateDoc != null & template != null)
            isUpdateDocumentMode = true;
        else if (templateDoc == null & template == null)
            isCreateTemplateMode = true;
        else if (templateDoc == null & template != null) {
            if (isReview) {
                if (template.UserId != userId)
                    isReviewTemplateMode = true;
                else
                    isReviewUserTemplateMode = true;
            } else
                isCreateDocumentMode = true;
        }
    }
    private void setWindowData() {
        binding.rightMenuSaveBtn.setVisibility(View.VISIBLE);
        binding.rightMenuDeleteBtn.setVisibility(View.VISIBLE);
        binding.rightMenuSaveAsBtn.setVisibility(View.VISIBLE);
        binding.rightMenuSaveBtn.setText(R.string.save);
        templateViews.clear();
        templateObjects.clear();
        if (isCreateDocumentMode) {
            binding.arrowImageView.setVisibility(View.VISIBLE);
            binding.nameTxt.setOnClickListener(v -> {
                Boolean btnFlag = v.getTag().equals("off");
                ImageView arrowImageView = findViewById(R.id.arrow_image_view);
                ObjectAnimator animator = btnFlag ? ObjectAnimator.ofFloat(arrowImageView, View.ROTATION_X, 0f, 180f) : ObjectAnimator.ofFloat(arrowImageView, View.ROTATION_X, 180f, 0f);
                animator.setDuration(600);
                animator.start();
                MotionLayout ml = findViewById(R.id.main_panel);
                ml.setTransition(R.id.transTop);
                if (btnFlag) {
                    ml.transitionToEnd();
                    v.setTag("on");
                } else {
                    ml.transitionToStart();
                    v.setTag("off");
                }
            });
        }
        if (!isCreateTemplateMode) {
            binding.bottomPanel.setVisibility(View.GONE);
            binding.hideBottomPanelBtn.setVisibility(View.GONE);
            binding.hideBottomPanelBtn.setVisibility(View.GONE);
            templateObjects.addAll(db.getTemplateObjectsByTemplateId(template.Id));
            setContentPanelData();
        }
        else{
            binding.rightMenuDeleteBtn.setVisibility(View.GONE);
            binding.rightMenuSaveAsBtn.setVisibility(View.GONE);
        }
        if (isReviewUserTemplateMode) {
            binding.confirmButton.setVisibility(View.INVISIBLE);
            binding.rightMenuSaveBtn.setText(R.string.create_document);
            binding.rightMenuSaveAsBtn.setVisibility(View.GONE);
        }
        else
            binding.confirmButton.setVisibility(View.VISIBLE);
        if (isReviewTemplateMode) {
            binding.rightMenuSaveBtn.setText(R.string.download);
            binding.rightMenuDeleteBtn.setVisibility(View.GONE);
            binding.rightMenuSaveAsBtn.setVisibility(View.GONE);
            binding.confirmButton.setBackgroundResource(R.drawable.ic_round_download);
        }
    }
    private void setTopPatternPanelData() {
        LinearLayout patternContainer = findViewById(R.id.pattern_container_layout);
        ArrayList<Template> templates = (ArrayList<Template>) db.getTemplateByUserId(AppService.getUserId(this));
        ArrayList<Template> downloadedTemplates = (ArrayList<Template>) db.getTemplateDownload(AppService.getUserId(this));
        if (templates.size() > 0)
            patternContainer.addView(makePatternTopTextView(getString(R.string.my_templates) + ":"));
        for (Template template :
                templates) {
            patternContainer.addView(makePatternTopButton(template));
        }
        if(downloadedTemplates.size() > 0)
            patternContainer.addView(makePatternTopTextView(getString(R.string.downloaded)));
        for (Template template :
                downloadedTemplates) {
            patternContainer.addView(makePatternTopButton(template));
        }
    }
    private View makePatternTopButton(Template template) {
        Button button = (Button) getLayoutInflater().inflate(R.layout.pattern_button_layout, null);
        button.setText(template.Name);
        if (template.Status.equals("Downloaded"))
            button.setCompoundDrawables(getDrawable(R.drawable.ic_downloaded_template), null, null, null);
        button.setTag(template);
        button.setOnClickListener(v -> {
            Template temp = (Template) v.getTag();
            this.template = temp;
            templateObjects.clear();
            templateViews.clear();
            templateObjects.addAll(db.getTemplateObjectsByTemplateId(template.Id));
            setContentPanelData();
        });
        return button;
    }
    private View makePatternTopTextView(String text) {
        TextView textView = new TextView(this);
        textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(getResources().getColor(R.color.yellow));
        textView.setText(text);
        textView.setTextSize(18);
        textView.setTypeface(null, Typeface.BOLD);
        return textView;
    }
    private void setContentPanelData() {
        binding.mainContentPanel.removeAllViews();
        for (TemplateObject templateObj :
                templateObjects) {
            if (templateObj.Type.equals("EditText"))
                makeEditText(templateObj);
            else if (templateObj.Type.equals("NumberText"))
                makeEditText(templateObj);
            else if (templateObj.Type.equals("CheckBox"))
                makeCheckBox(templateObj);
            else if (templateObj.Type.equals("Photo"))
                makePhoto(templateObj);
        }
    }
    private void setOnClickListeners() {
        binding.menubarBack.setOnClickListener(v ->
                onBackPressed()
        );
        binding.bottomAddEdittextPanel.setOnClickListener(v -> getTitleDialog(DIALOG_EDITTEXT));
        binding.bottomAddNumberTextPanel.setOnClickListener(v -> getTitleDialog(DIALOG_NUMBER_TEXT));
        binding.bottomAddCheckboxPanel.setOnClickListener(v -> getTitleDialog(DIALOG_CHECKBOX));
        binding.bottomAddPhotoPanel.setOnClickListener(v -> getTitleDialog(DIALOG_IMAGE));
        binding.hideBottomPanelBtn.setOnClickListener(v -> showHideBottomPanel());
        if (isCreateTemplateMode) {
            Consumer<View> d = (v) -> {
                if (templateObjects.size() > 0)
                    getTitleDialog(DIALOG_TEMPLATE_NAME);
                else
                    Toast.makeText(this, "Add at least one element", Toast.LENGTH_SHORT).show();
            };
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                binding.rightMenuSaveBtn.setOnClickListener(d::accept);
                binding.confirmButton.setOnClickListener(d::accept);
            }
        }
        if (isReviewTemplateMode) {
            binding.confirmButton.setOnClickListener(v -> downloadTemplateBtnClick());
        }
        if (isCreateDocumentMode) {
            binding.rightMenuSaveBtn.setOnClickListener(v->{saveDocumentBtnClick();onBackPressed();});
            binding.confirmButton.setOnClickListener(v -> {saveDocumentBtnClick();onBackPressed();});
        }
        if (isUpdateDocumentMode) {
            binding.rightMenuSaveBtn.setOnClickListener(v->updateDocumentBtnClick());
            binding.confirmButton.setOnClickListener(v -> updateDocumentBtnClick());
            binding.rightMenuDeleteBtn.setOnClickListener(v -> deleteDocumentBtnClick());
        }
        if(isReviewUserTemplateMode) {
            binding.rightMenuSaveBtn.setOnClickListener(v -> loadCreateDocument());
            binding.rightMenuDeleteBtn.setOnClickListener(v -> deleteTemplateBtnClick());
        }
        binding.passportTopBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainDocumentPatternActivity.class);
            intent.putExtra("type","Passport");
            startActivity(intent);
            overridePendingTransition(R.anim.alpha_in, R.anim.alpha_out);});
        binding.SNILSTopBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainDocumentPatternActivity.class);
            intent.putExtra("type","SNILS");
            startActivity(intent);
            overridePendingTransition(R.anim.alpha_in, R.anim.alpha_out);});
        binding.INNTopBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainDocumentPatternActivity.class);
            intent.putExtra("type","INN");
            startActivity(intent);
            overridePendingTransition(R.anim.alpha_in, R.anim.alpha_out);});
        binding.policyTopBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainDocumentPatternActivity.class);
            intent.putExtra("type","Polis");
            startActivity(intent);
            overridePendingTransition(R.anim.alpha_in, R.anim.alpha_out);});
        binding.menubarOptions.setOnClickListener(v->menuOptionsBtnClick(v));
        binding.rightMenuSaveAsBtn.setOnClickListener(v->saveAsBtnClick(v));
    }
    private void deleteDocumentBtnClick() {
        db.deleteItem(templateDoc.Id);
        onBackPressed();
    }
    private void loadCreateDocument() {
        isCreateTemplateMode = isReviewTemplateMode = isReviewUserTemplateMode = isUpdateDocumentMode = false;
        isCreateDocumentMode = true;
        setWindowData();
        setTopPatternPanelData();
        setOnClickListeners();
        binding.confirmButton.setVisibility(View.VISIBLE);
    }
    private void deleteTemplateBtnClick() {
        db.deleteTemplate(template.Id);
        onBackPressed();
    }
    private void saveAsBtnClick(View v) {
        if(isCreateDocumentMode)
            saveDocumentBtnClick();
        if(isUpdateDocumentMode)
            updateDocumentBtnClick();
        if(PDFMaker.createTemplatePDF(templateObjects, templateViews,AppService.getUserId(this)))
            Toast.makeText(this,R.string.create_document,Toast.LENGTH_SHORT).show();
    }
    private void menuOptionsBtnClick(View v) {
        if (v.getTag().equals("off")) {
            MotionLayout ml = findViewById(R.id.main_panel);
            ml.setTransition(R.id.transRightMenu);
            ml.transitionToEnd();
            v.setTag("on");
        } else if (v.getTag().equals("on")) {
            MotionLayout ml = findViewById(R.id.main_panel);
            ml.setTransition(R.id.transRightMenu);
            ml.transitionToStart();
            v.setTag("off");
        }
    }
    private void updateDocumentBtnClick() {
        for (TemplateObject templateObject : templateObjects) {
            View view = templateViews.get(templateObject);
            TemplateDocumentData tempData = db.getTemplateDocumentData(templateObject.Id, templateDoc.Id);
            if (tempData != null) {
                if (templateObject.Type.equals("EditText") || templateObject.Type.equals("NumberText")) {
                    EditText editText = (EditText) view;
                    tempData.Value = editText.getText().toString();
                    db.updateTemplateDocumentData(tempData.Id, tempData,false);
                } else if (templateObject.Type.equals("CheckBox")) {
                    CheckBox checkBox = (CheckBox) view;
                    tempData.Value = checkBox.isChecked() ? "true" : "false";
                    db.updateTemplateDocumentData(tempData.Id, tempData,false);
                }else if (templateObject.Type.equals("Photo")) {
                    Item item = db.getItemById(templateDoc.Id);
                    tempData.Value = savePhoto(view, tempData, item);
                    db.updateTemplateDocumentData(tempData.Id, tempData,false);
                }
            }
        }
    }
    private void saveDocumentBtnClick() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.US);
        String time = df.format(new Date());
        Item item = new Item(NULL_UUID, template.Name + db.selectLastItemId(), "Template", null, 0, 0, 0, time, NULL_UUID, AppService.getUserId(this),"");

        UUID id;
        item.Id = db.insertItem(item, true);
        db.insertTemplateDocument(new TemplateDocument(item.Id, template.Id,null));
        id = db.selectLastTemplateDocumentId();
        for (TemplateObject templateObject : templateObjects) {
            View view = templateViews.get(templateObject);
            if(view==null)
                continue;
            if (templateObject.Type.equals("EditText") || templateObject.Type.equals("NumberText")) {
                EditText editText = (EditText) view;
                db.insertTemplateDocumentData(new TemplateDocumentData(NULL_UUID, editText.getText().toString(), templateObject.Id, id, null), true);
            } else if (templateObject.Type.equals("CheckBox")) {
                CheckBox checkBox = (CheckBox) view;
                String value = checkBox.isChecked() ? "true" : "false";
                db.insertTemplateDocumentData(new TemplateDocumentData(NULL_UUID, value, templateObject.Id, id, null), true);
            }else if (templateObject.Type.equals("Photo")) {
                TemplateDocumentData tempData = new TemplateDocumentData(NULL_UUID, null, templateObject.Id, id, null);
                db.insertTemplateDocumentData(tempData, true);
                tempData.Id = db.selectLastTemplateDocumentDataId();
                tempData.Value = savePhoto(view, tempData, item);
                db.updateTemplateDocumentData(tempData.Id, tempData,false);
            }
        }
    }

    private String savePhoto(View view,TemplateDocumentData templateDocumentData, Item item) {
        if(templateViewBitmaps.get(view)==null) {
            if (templateDocumentData.Value == null)
                return null;
            else
                return templateDocumentData.Value;
        }
        File rootDir = getApplicationContext().getFilesDir();
        String imgPath = rootDir.getAbsolutePath() + "/" + MainContentActivity.APPLICATION_NAME+"/"+ AppService.getUserId(this) + "/Item" + item.Id + "/Template/";
        File dir = new File(imgPath);
        if (!dir.exists())
            dir.mkdirs();
        String imgName = item.Title + templateDocumentData.Id;
        File imgFile = new File(dir, imgName);
        if (templateDocumentData.Value != null) {
            File filePath = new File(templateDocumentData.Value);
            filePath.delete();
            imgFile = new File(templateDocumentData.Value);
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        templateViewBitmaps.get(view).compress(Bitmap.CompressFormat.JPEG, 100, stream);
        InputStream is = new ByteArrayInputStream(stream.toByteArray());
        try {
            MyEncrypter.encryptToFile(AppService.getMy_key(), AppService.getMy_spec_key(), is, new FileOutputStream(imgFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imgFile.getAbsolutePath();
    }

    private void downloadTemplateBtnClick() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.US);
        String time = df.format(new Date());
        Template temp = new Template(NULL_UUID, template.Name, "Downloaded", time,null ,userId);

        UUID id = db.insertTemplate(temp, true);
        for (TemplateObject templateObject :
                templateObjects) {
            templateObject.TemplateId = id;
            db.insertTemplateObject(templateObject, true);
        }
        startActivity(new Intent(this, MainTemplateActivity.class));
        overridePendingTransition(R.anim.alpha_in, R.anim.alpha_out);
    }
    private void saveTemplateBtnClick(String name) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.US);
        String time = df.format(new Date());
        Template template = new Template(NULL_UUID, name, "New", time,null, userId);
        db.insertTemplate(template, true);
        UUID id = db.selectLastTemplateId();
        for (TemplateObject templateObject :
                templateObjects) {
            templateObject.TemplateId = id;
            db.insertTemplateObject(templateObject, true);
        }
        startActivity(new Intent(this, MainTemplateActivity.class));
        overridePendingTransition(R.anim.alpha_in, R.anim.alpha_out);
    }
    @Override
    public void onBackPressed() {
        if (isCreateDocumentMode || isUpdateDocumentMode)
            startActivity(new Intent(this, MainContentActivity.class));
        else
            startActivity(new Intent(this, MainTemplateActivity.class));
        overridePendingTransition(R.anim.alpha_in, R.anim.alpha_out);
    }
    private void showHideBottomPanel() {
        float initialY = binding.bottomPanel.getTranslationY();
        float finalY = binding.mainPanel.getHeight();
        float bottomH = binding.bottomPanel.getHeight();
        float bottomY = binding.bottomPanel.getY();
        float hideY = binding.hideBottomPanelBtn.getY();
        float hideH = binding.hideBottomPanelBtn.getHeight();
        ObjectAnimator flipAnimation;
        int duration = 400;
        if (isShowing)
            flipAnimation = ObjectAnimator.ofFloat(binding.hideBottomPanelBtn, "rotationX", 180, 0).setDuration(duration);
        else
            flipAnimation = ObjectAnimator.ofFloat(binding.hideBottomPanelBtn, "rotationX", 0, 180).setDuration(duration);
        ValueAnimator hideBottomAnimation = isShowing ? ValueAnimator.ofFloat(initialY, 0) : ValueAnimator.ofFloat(0, finalY - bottomY).setDuration(duration);
        hideBottomAnimation.addUpdateListener(valueAnimator -> {
            float animatedValue = (float) valueAnimator.getAnimatedValue();
            binding.bottomPanel.setTranslationY(animatedValue);
        });
        ValueAnimator hideButtonAnimation = isShowing ? ValueAnimator.ofFloat(finalY - hideH, finalY - bottomH - hideH - 20) : ValueAnimator.ofFloat(hideY, finalY - hideH).setDuration(duration);
        hideButtonAnimation.addUpdateListener(valueAnimator -> {
            float animatedValue = (float) valueAnimator.getAnimatedValue();
            binding.hideBottomPanelBtn.setY(animatedValue);
        });
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(flipAnimation, hideBottomAnimation, hideButtonAnimation);
        isShowing = !isShowing;
        animatorSet.start();
    }
    private void makeEditText(final TemplateObject templateObject) {
        if (isCreateTemplateMode)
            templateObjects.add(templateObject);
        ConstraintLayout layout = (ConstraintLayout) getLayoutInflater().inflate(R.layout.template_edittext_layout, null);
        TextView titleTxt = layout.findViewById(R.id.title_txt);
        EditText editTxt = layout.findViewById(R.id.content_txt);
        titleTxt.setText(templateObject.Title);
        if (templateObject.Type.equals("NumberText")) {
            editTxt.setInputType(InputType.TYPE_CLASS_NUMBER);
        }
        ImageButton deleteBtn = layout.findViewById(R.id.delete_btn);
        if (!isCreateTemplateMode)
            deleteBtn.setVisibility(View.GONE);
        else
            deleteBtn.setOnClickListener(v -> {
                templateObjects.remove(templateObject);
                db.deleteTemplateObject(templateObject.Id);
                binding.mainContentPanel.removeView(layout);
            });
        if (isUpdateDocumentMode) {
            TemplateDocumentData docData = db.getTemplateDocumentData(templateObject.Id, templateDoc.Id);
            if (docData != null) {
                editTxt.setText(docData.Value);
            }
        }
        if (isCreateDocumentMode || isUpdateDocumentMode)
            templateViews.put(templateObject, editTxt);
        binding.mainContentPanel.addView(layout);
    }
    private void getTitleDialog(int mode) {
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
        if (mode != DIALOG_TEMPLATE_NAME)
            editText.setHint(R.string.enter_object_name);
        ((Button) makeRenameDialog.findViewById(R.id.make_rename_item_btn)).setText(R.string.create_a_folder);
        makeRenameDialog.findViewById(R.id.make_rename_item_btn).setOnClickListener(view -> {
            if (editText.getText().toString().replace(" ", "").equals(""))
                return;
            else {
                TemplateObject templateObject;
                switch (mode) {
                    case DIALOG_EDITTEXT:
                        templateObject = new TemplateObject(NULL_UUID, templateObjects.size(), "EditText", editText.getText().toString(), NULL_UUID, null);
                        makeEditText(templateObject);
                        break;
                    case DIALOG_NUMBER_TEXT:
                        templateObject = new TemplateObject(NULL_UUID, templateObjects.size(), "NumberText", editText.getText().toString(), NULL_UUID, null);
                        makeEditText(templateObject);
                        break;
                    case DIALOG_CHECKBOX:
                        templateObject = new TemplateObject(NULL_UUID, templateObjects.size(), "CheckBox", editText.getText().toString(), NULL_UUID, null);
                        makeCheckBox(templateObject);
                        break;
                    case DIALOG_IMAGE:
                        templateObject = new TemplateObject(NULL_UUID, templateObjects.size(), "Photo", editText.getText().toString(), NULL_UUID, null);
                        makePhoto(templateObject);
                        break;
                    case DIALOG_TEMPLATE_NAME:
                        saveTemplateBtnClick(editText.getText().toString());
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

    private void makePhoto(TemplateObject templateObject) {
        if (isCreateTemplateMode)
            templateObjects.add(templateObject);
        ConstraintLayout layout = (ConstraintLayout) getLayoutInflater().inflate(R.layout.template_image_layout, null);
        TextView titleTxt = layout.findViewById(R.id.title_txt);
        titleTxt.setText(templateObject.Title);
        ImageView imageView = layout.findViewById(R.id.image_panel);
        Button imageLoadBtn = layout.findViewById(R.id.load_image_btn);
        ImageButton deleteBtn = layout.findViewById(R.id.delete_btn);
        if (!isCreateTemplateMode)
            deleteBtn.setVisibility(View.GONE);
        else {
            deleteBtn.setOnClickListener(v -> {
                templateObjects.remove(templateObject);
                db.deleteTemplateObject(templateObject.Id);
                binding.mainContentPanel.removeView(layout);
            });
            imageLoadBtn.setVisibility(View.GONE);
        }
        if(isReviewUserTemplateMode | isReviewTemplateMode)
            imageLoadBtn.setVisibility(View.GONE);
        if (isUpdateDocumentMode) {
            TemplateDocumentData docData = db.getTemplateDocumentData(templateObject.Id, templateDoc.Id);
            if (docData != null) {
                imageView.setOnClickListener(v->{
                    if (docData.Value == null)
                        return;
                    if(templateViewBitmaps.get(imageView)==null) {
                        Intent intent = new Intent(this, ImageActivity.class);
                        intent.putExtra("text", templateObject.Title);
                        String fileName = docData.Value;
                        intent.putExtra("type", DB_IMAGE);
                        intent.putExtra("imageFile", fileName);
                        startActivity(intent);
                        overridePendingTransition(R.anim.alpha_in, R.anim.alpha_out);
                    }
                });
                if(docData.Value != null) {
                    imageView.setBackgroundColor(Color.TRANSPARENT);
                    File outputFile = new File(docData.Value + "_copy");
                    File encFile = new File(docData.Value);
                    try {
                        MyEncrypter.decryptToFile(AppService.getMy_key(), AppService.getMy_spec_key(), new FileInputStream(encFile), new FileOutputStream(outputFile));
                        imageView.setImageURI(Uri.fromFile(outputFile));
                        outputFile.delete();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        if (isCreateDocumentMode || isUpdateDocumentMode) {
            templateViews.put(templateObject, imageView);
            imageLoadBtn.setOnClickListener(v->{
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = CropImage.activity()
                            .getIntent(this);
                    currentLoadPhotoView = imageView;
                    registerForARImage.launch(intent);
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
                }
            });
        }
        binding.mainContentPanel.addView(layout);
    }

    private void makeCheckBox(TemplateObject templateObject) {
        if (isCreateTemplateMode)
            templateObjects.add(templateObject);
        ConstraintLayout layout = (ConstraintLayout) getLayoutInflater().inflate(R.layout.template_checkbox_layout, null);
        CheckBox checkBox = layout.findViewById(R.id.title_txt);
        checkBox.setText(templateObject.Title);
        ImageButton deleteBtn = layout.findViewById(R.id.delete_btn);
        deleteBtn.setTag(templateObject.Id);
        if (!isCreateTemplateMode)
            deleteBtn.setVisibility(View.GONE);
        else
            deleteBtn.setOnClickListener(v -> {
                templateObjects.remove(templateObject);
                db.deleteTemplateObject(templateObject.Id);
                binding.mainContentPanel.removeView(layout);
            });
        if (isUpdateDocumentMode) {
            TemplateDocumentData docData = db.getTemplateDocumentData(templateObject.Id, templateDoc.Id);
            if (docData != null) {
                if (docData.Value.equals("true"))
                    checkBox.setChecked(true);
                else
                    checkBox.setChecked(false);
            }
        }
        if (isCreateDocumentMode || isUpdateDocumentMode)
            templateViews.put(templateObject, checkBox);
        binding.mainContentPanel.addView(layout);
    }
}