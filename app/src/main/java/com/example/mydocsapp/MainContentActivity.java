package com.example.mydocsapp;

import android.Manifest;
import android.app.Dialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NavUtils;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mydocsapp.api.User;
import com.example.mydocsapp.apputils.MyEncrypter;
import com.example.mydocsapp.apputils.RecyclerItemClickListener;
import com.example.mydocsapp.interfaces.ItemAdapterActivity;
import com.example.mydocsapp.models.Item;
import com.example.mydocsapp.models.Photo;
import com.example.mydocsapp.services.AppService;
import com.example.mydocsapp.services.CurrentItemsService;
import com.example.mydocsapp.services.DBHelper;
import com.example.mydocsapp.services.ItemAdapter;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import javax.crypto.NoSuchPaddingException;

public class MainContentActivity extends AppCompatActivity implements ItemAdapterActivity {
    private static final int DB_IMAGE = 1;
    private static final int SESSION_MODE_CREATE = 1;
    private static final int SESSION_MODE_OPEN = 2;
    RecyclerView recyclerView;
    DBHelper db;
    boolean isSelectMode;
    boolean isSortMode = false;
    private int selectedItemsNum = 0;
    private ActivityResultLauncher<Intent> registerForARFolder;
    private ActivityResultLauncher<Intent> registerForARImage;
    private ActivityResultLauncher<Intent> registerForARImageCollection;
    private RecyclerView recyclerFolderView;
    Dialog makeRenameDialog;
    ItemAdapter adapter;
    CurrentItemsService itemsService;

    private User CurrentUser;
    private ArrayList<Item> CurrentFolderItemsSet;
    private boolean isTitleClicked = false;

    public static final int RECYCLER_ADAPTER_EVENT_CHANGE = 1;
    public static final int RECYCLER_ADAPTER_EVENT_MOVE = 2;
    private static final int RECYCLER_ADAPTER_EVENT_ITEMS_CHANGE = 3;

    public static final String APPLICATION_NAME = "MyDocs";
    private ArrayList<Bitmap> bitmapList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_content);

        registerForARFolder = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                    CurrentFolderItemsSet = getItemsFromDb(itemsService.getCurrentItem().Id);
                    reFillContentPanel(recyclerFolderView, CurrentFolderItemsSet);
                    itemsService.setInitialData();
                    reFillContentPanel(recyclerView, itemsService.getCurrentItemsSet());
        });
        registerForARImage = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    CropImage.ActivityResult cropImageResult = CropImage.getActivityResult(result.getData());
                    if (result.getData() != null) {
                        // Get the URI of the selected file
                        final Uri uri = cropImageResult.getUri();
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                            ByteArrayOutputStream out = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                            Bitmap decoded = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));
                            createImage(decoded);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
        });
        registerForARImageCollection = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result->{
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                // get selected image URIs
                ClipData clipData = result.getData().getClipData();
                if (clipData != null) {
                    if (clipData.getItemCount() > 1) {
                        // multiple images selected
                        for (int i = 0; i < clipData.getItemCount(); i++) {
                            Uri uri = clipData.getItemAt(i).getUri();
                            try {
                                // convert selected image to bitmap
                                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                                // add bitmap to array
                                bitmapList.add(bitmap);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        });

        // начальная инициализация списка
        // создаем базу данных
        db = new DBHelper(this, AppService.getUserId(this));

        setCurrentUserFromDB();
        //setCurrentUserFromAPI();

        isSelectMode = false;

        TextView gtxt = findViewById(R.id.login_txt);
        gtxt.setText((CurrentUser.Login));

        itemsService = new CurrentItemsService(db);
        itemsService.setInitialData();
        itemsService.setSelectedPropertyZero();

        CurrentFolderItemsSet = null;
        isTitleClicked = false;

        setViewsTagOff();
        recyclerView = findViewById(R.id.container);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        // создаем адаптер
        adapter = new ItemAdapter(this, itemsService.getCurrentItemsSet(), isSelectMode);
        //adapter.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY);
        // устанавливаем для списка адаптер
        recyclerView.setAdapter(adapter);

        int spanCount = 2; // 2 columns
        int spacing = 20; // 10px
        boolean includeEdge = true;
        recyclerView.addItemDecoration(new com.example.mydocsapp.apputils.GridSpacingItemDecoration(spanCount, spacing, includeEdge));
        setOnClickListeners();
    }

    private void setCurrentUserFromDB() {
        Cursor cur = db.getUserById(AppService.getUserId(this));
        cur.moveToFirst();
        CurrentUser = new User(AppService.getUserId(this), cur.getString(1), cur.getString(2), cur.getString(3), cur.getString(4), cur.getString(5));
    }
    private void setCurrentUserFromAPI() {
        Cursor cur = db.getUserById(1);
        cur.moveToFirst();
        CurrentUser = new User(0, cur.getString(1), cur.getString(2), cur.getString(3), cur.getString(4), cur.getString(5));
    }

    private void createImage(Bitmap bitmap) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss:SSS", Locale.US);
        String time = df.format(new Date());

        int ItemId = db.selectLastItemId();
        Item item =new Item(0, "Фото" + ItemId, "Изображение", null, 0, 0, 0, time, 0, 0);
        db.insertItem(item);
        ItemId = db.selectLastItemId();

        File rootDir = getApplicationContext().getFilesDir();
        String imgPath = rootDir.getAbsolutePath() + "/" + MainContentActivity.APPLICATION_NAME + "/Item" + ItemId + "/";

        File dir = new File(imgPath);
        if (!dir.exists())
            dir.mkdirs();
        String imgName = "Image" + ItemId + System.currentTimeMillis();
        File imgFile = new File(dir, imgName);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        InputStream is = new ByteArrayInputStream(stream.toByteArray());
        try {
            MyEncrypter.encryptToFile(AppService.getMy_key(), AppService.getMy_spec_key(), is, new FileOutputStream(imgFile));
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String filePath = imgFile.getAbsolutePath();
        db.insertPhoto(new Photo(ItemId,filePath,0));

        item.Image = filePath;
        db.updateItem(ItemId,item);

        Intent intent = new Intent(MainContentActivity.this, ImageActivity.class);
        intent.putExtra("text", item.Title);
        intent.putExtra("type", DB_IMAGE);
        intent.putExtra("item", itemsService.getCurrentItem());
        intent.putExtra("imageFile", filePath);
        this.startActivity(intent);
    }

    private ArrayList<Item> selectedItemsSet = new ArrayList<>();

    private void setOnClickListeners() {
        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(this, recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.Q)
                    @Override
                    public void onItemClick(View view, int position) {
                        if (findViewById(R.id.container).getAlpha() == 0.5f)
                            return;
                        Item item = (Item) view.getTag();
                        if (isSelectMode) {
                            if (item.isSelected == 0) {
                                item.isSelected = 1;
                                selectedItemsNum++;
                                selectedItemsSet.add(item);
                            } else {
                                item.isSelected = 0;
                                selectedItemsNum--;
                                selectedItemsSet.remove(item);
                            }
                            view.setTag(item);
                            itemsService.setItem(position, item);
                            adapter.onItemChanged(item);

                            ((TextView) findViewById(R.id.top_select_picked_txt)).setText(getString(R.string.selected_string) + " " + selectedItemsNum);
                        } else {
                            itemsService.setCurrentItem(item);
                            if (isTitleClicked) {
                                makeRenameDialogMethod(itemsService.getCurrentItemsSet(), "Rename");
                                makeRenameDialog.show();
                            } else {
                                if (item.Type.equals("Папка")) {
                                    Dialog dialog = new Dialog(MainContentActivity.this);
                                    dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                                    dialog.setContentView(R.layout.folder_items_panel_layout);
                                    dialog.getWindow().setGravity(Gravity.CENTER);
                                    dialog.setCancelable(true);

                                    int width = LinearLayout.LayoutParams.MATCH_PARENT;
                                    int height = LinearLayout.LayoutParams.MATCH_PARENT;
                                    dialog.getWindow().setLayout(width, height);

                                    TextView text = dialog.findViewById(R.id.title_folder_txt);
                                    text.setText(item.Title);

                                    CurrentFolderItemsSet = getItemsFromDb(item.Id);

                                    recyclerFolderView = dialog.findViewById(R.id.folder_container);

                                    recyclerFolderView.setLayoutManager(new GridLayoutManager(MainContentActivity.this, 2));
                                    ItemAdapter adapter = new ItemAdapter(MainContentActivity.this, CurrentFolderItemsSet, false);
                                    recyclerFolderView.setAdapter(adapter);
                                    recyclerFolderView.addOnItemTouchListener(
                                            new RecyclerItemClickListener(MainContentActivity.this, recyclerFolderView, new RecyclerItemClickListener.OnItemClickListener() {
                                                @RequiresApi(api = Build.VERSION_CODES.Q)
                                                @Override
                                                public void onItemClick(View view, int position) {
                                                    itemsService.setCurrentItem((Item) view.getTag());
                                                    if (isTitleClicked) {
                                                        makeRenameDialogMethod(CurrentFolderItemsSet, "FRename");
                                                        makeRenameDialog.show();
                                                    } else if (itemsService.getCurrentItem().Type.equals("Паспорт")) {
                                                        Intent intent = new Intent(MainContentActivity.this, MainPassportPatternActivity.class);
                                                        intent.putExtra("item", itemsService.getCurrentItem());
                                                        startActivity(intent);
                                                    } else if (itemsService.getCurrentItem().Type.equals("Карта")) {
                                                        Intent intent = new Intent(MainContentActivity.this, CardPatternActivity.class);
                                                        intent.putExtra("item", itemsService.getCurrentItem());
                                                        startActivity(intent);
                                                    }else if (item.Type.equals("Изображение")) {
                                                        Intent intent = new Intent(MainContentActivity.this, ImageActivity.class);
                                                        intent.putExtra("text", item.Title);
                                                        intent.putExtra("type", DB_IMAGE);
                                                        intent.putExtra("item", itemsService.getCurrentItem());
                                                        intent.putExtra("imageFile", item.Image);
                                                        startActivity(intent);
                                                    }
                                                }

                                                @Override
                                                public void onLongItemClick(View view, int position) {
                                                }
                                            }));
                                    //set buttons
                                    Button flowButton = dialog.findViewById(R.id.flow_folder_button);
                                    flowButton.setOnClickListener(view1 -> {
                                        Intent i = new Intent(MainContentActivity.this, FolderAddItemActivity.class);
                                        itemsService.setCurrentItem(item);
                                        i.putExtra("item", itemsService.getCurrentItem());
                                        registerForARFolder.launch(i);
                                        overridePendingTransition(R.anim.alpha, R.anim.alpha_to_zero);
                                    });

                                    Button hideButton = dialog.findViewById(R.id.hide_folder_btn);
                                    hideButton.setOnClickListener(v -> {
                                        dialog.cancel();
                                    });

                                    dialog.getWindow().setWindowAnimations(R.style.PauseDialogAnimation);
                                    dialog.show();
                                } else if (item.Type.equals("Паспорт")) {
                                    Intent intent = new Intent(MainContentActivity.this, MainPassportPatternActivity.class);
                                    intent.putExtra("item", itemsService.getCurrentItem());
                                    startActivity(intent);
                                } else if (item.Type.equals("Карта")) {
                                    Intent intent = new Intent(MainContentActivity.this, CardPatternActivity.class);
                                    intent.putExtra("item", itemsService.getCurrentItem());
                                    startActivity(intent);
                                }else if (item.Type.equals("Изображение")) {
                                    Intent intent = new Intent(MainContentActivity.this, ImageCollectionActivity.class);
                                    intent.putExtra("userId",CurrentUser.Id);
                                    intent.putExtra("mode",SESSION_MODE_OPEN);
                                    intent.putExtra("item",item);
                                    startActivity(intent);
                                }else if (item.Type.equals("Альбом")) {
                                    Intent intent = new Intent(MainContentActivity.this, ImageCollectionActivity.class);
                                    intent.putExtra("userId",CurrentUser.Id);
                                    intent.putExtra("mode",SESSION_MODE_OPEN);
                                    intent.putExtra("item",item);
                                    startActivity(intent);
                                }
                            }
                        }
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {
                        if (!isSortMode) {
                            Item item = (Item) view.getTag();
                            selectedItemsSet = new ArrayList<>();
                            selectedItemsSet.add(item);
                            if (findViewById(R.id.container).getAlpha() == 0.5f)
                                return;
                            MotionLayout ml = findViewById(R.id.motion_layout);
                            ml.setTransition(R.id.transGoSelect);
                            ml.transitionToEnd();
                            isSelectMode = true;

                            item.isSelected = 1;
                            selectedItemsNum = 1;
                            ((TextView) findViewById(R.id.top_select_picked_txt)).setText(getString(R.string.selected_string) + " " + +selectedItemsNum);

                            itemsService.setItem(position, item);
                            view.setTag(item);

                            reFillContentPanel(RECYCLER_ADAPTER_EVENT_CHANGE, itemsService.getCurrentItemsSet());
                        }
                    }
                })
        );
        Button flowBtn = findViewById(R.id.flow_button);
        flowBtn.setOnClickListener(view -> openAddMenuClick(view));
    }

    private void makeRenameDialogMethod(ArrayList<Item> items, String mode) {
        makeRenameDialog = new Dialog(MainContentActivity.this);
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

        if (mode.equals("Make")) {
            ((Button) makeRenameDialog.findViewById(R.id.make_rename_item_btn)).setText(R.string.create_a_folder);
            editText.setText("");
        } else {
            ((Button) makeRenameDialog.findViewById(R.id.make_rename_item_btn)).setText(R.string.rename_an_item);
            editText.append(itemsService.getCurrentItem().Title);
        }
        makeRenameDialog.findViewById(R.id.make_rename_item_btn).setOnClickListener(view2 -> {
            if (editText.getText().toString().equals(""))
                return;
            if (mode.equals("Make")) {
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss:SSS", Locale.US);
                String time = df.format(new Date());
                db.insertItem(new Item(0, editText.getText().toString(), "Папка", null, 0, 0, 0, time, 0, 0));
            } else {
                Item item = itemsService.getCurrentItem();
                item.Title = editText.getText().toString();
                db.updateItem(item.Id, item);
            }
            if (mode.equals("Rename") | mode.equals("Make")) {
                itemsService.setInitialData();
                reFillContentPanel(RECYCLER_ADAPTER_EVENT_CHANGE, items);
            } else if (mode.equals("FRename")) {
                CurrentFolderItemsSet = getItemsFromDb(itemsService.getCurrentItem().FolderId);
                reFillContentPanel(RECYCLER_ADAPTER_EVENT_CHANGE, CurrentFolderItemsSet);
            }
            isTitleClicked = false;
            itemsService.setCurrentItem(null);
            makeRenameDialog.dismiss();
        });
        makeRenameDialog.findViewById(R.id.cancel_btn).setOnClickListener(view12 -> {
            itemsService.setCurrentItem(null);
            isTitleClicked = false;
            makeRenameDialog.dismiss();
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        reFillContentPanel(recyclerView, itemsService.getCurrentItemsSet());
    }

    private ArrayList<Item> getItemsFromDb(int id) {
        ArrayList<Item> folderItems = new ArrayList<>();

        Cursor cur = db.getItemsByFolder(id);
        Item item;
        while (cur.moveToNext()) {
            item = new Item(cur.getInt(0),
                    cur.getString(1),
                    cur.getString(2),
                    cur.getString(3),
                    cur.getInt(4),
                    cur.getInt(5),
                    cur.getInt(6),
                    cur.getString(7),
                    cur.getInt(8),
                    cur.getInt(9));
            folderItems.add(item);
        }
        return folderItems;
    }

    private void setViewsTagOff() {
        if (findViewById(R.id.checkTranitionState).getAlpha() == 0.0) {
            findViewById(R.id.flow_button).setTag("off");
            findViewById(R.id.menubar_options).setTag("off");
            (findViewById(R.id.menubar_options)).setEnabled(true);
            (findViewById(R.id.flow_button)).setEnabled(true);
        }
    }

    public void goAccountClick(View view) {
        startActivity(new Intent(MainContentActivity.this, AccountActivity.class));
        overridePendingTransition(R.anim.slide_in_left, R.anim.alpha_out);
    }

    public void goPatternClick(View view) {
        Intent intent = new Intent(MainContentActivity.this, MainPassportPatternActivity.class);
        itemsService.setCurrentItem(null);
        intent.putExtra("item", itemsService.getCurrentItem());
        startActivity(intent);
    }

    public void openSortMenuClick(View view) {
        setViewsTagOff();
        MotionLayout ml = findViewById(R.id.motion_layout);
        if (view.getTag().toString() == "off") {
            isSortMode = true;
            ml.setTransition(R.id.trans3);
            ml.transitionToEnd();
            view.setTag("on");
            (findViewById(R.id.flow_button)).setEnabled(false);
        } else {
            isSortMode = false;
            ml.setTransition(R.id.trans4);
            ml.transitionToEnd();
            view.setTag("off");
            (findViewById(R.id.flow_button)).setEnabled(true);
        }
    }

    public void openAddMenuClick(View view) {
        setViewsTagOff();
        MotionLayout ml = findViewById(R.id.motion_layout);
        if (view.getTag().toString() == "off") {
            ml.setTransition(R.id.trans1);
            ml.transitionToEnd();
            view.setTag("on");
            (findViewById(R.id.menubar_options)).setEnabled(false);
        } else {
            //ml.setTransition(R.id.trans2);
            ml.transitionToStart();
            view.setTag("off");
            (findViewById(R.id.menubar_options)).setEnabled(true);
        }
    }

    public void menuMakeFolderClick(View view) {
        makeRenameDialogMethod(itemsService.getCurrentItemsSet(), "Make");
        makeRenameDialog.show();
    }

    public void menuMakeCardClick(View view) {
        Intent intent = new Intent(MainContentActivity.this, CardPatternActivity.class);
        startActivity(intent);
    }
    public void menuMakeImageClick(View view) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(this,ImageCollectionActivity.class);
            intent.putExtra("userId",CurrentUser.Id);
            intent.putExtra("mode",SESSION_MODE_CREATE);
            startActivity(intent);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
        }
    }


    //сортировка
    public void sortFolderClick(View view) {
        if (itemsService.isFoldersAvailable)
            view.setAlpha(0.5f);
        else {
            itemsService.setInitialData();
            view.setAlpha(1f);
        }
        itemsService.isFoldersAvailable = !itemsService.isFoldersAvailable;
        reFillContentPanel(RECYCLER_ADAPTER_EVENT_ITEMS_CHANGE, itemsService.getSortedCurrentItemsSet());
    }
    //сортировка
    public void sortCardClick(View view) {
        if (itemsService.isCardsAvailable)
            view.setAlpha(0.5f);
        else {
            itemsService.setInitialData();
            view.setAlpha(1f);
        }
        itemsService.isCardsAvailable = !itemsService.isCardsAvailable;
        reFillContentPanel(RECYCLER_ADAPTER_EVENT_ITEMS_CHANGE, itemsService.getSortedCurrentItemsSet());
    }
    //сортировка
    public void sortImageClick(View view) {
        if (itemsService.isImagesAvailable)
            view.setAlpha(0.5f);
        else {
            itemsService.setInitialData();
            view.setAlpha(1f);
        }
        itemsService.isImagesAvailable = !itemsService.isImagesAvailable;
        reFillContentPanel(RECYCLER_ADAPTER_EVENT_ITEMS_CHANGE, itemsService.getSortedCurrentItemsSet());
    }
    //сортировка
    public void sortDocClick(View view) {
        if (itemsService.isDocsAvailable)
            view.setAlpha(0.5f);
        else {
            itemsService.setInitialData();
            view.setAlpha(1f);
        }
        itemsService.isDocsAvailable = !itemsService.isDocsAvailable;
        reFillContentPanel(RECYCLER_ADAPTER_EVENT_ITEMS_CHANGE, itemsService.getSortedCurrentItemsSet());
    }

    void reFillContentPanel(RecyclerView _recyclerView, ArrayList<Item> _items) {
        _recyclerView.removeAllViews();
        adapter = new ItemAdapter(this, _items, isSelectMode);
        // устанавливаем для списка адаптер
        _recyclerView.setAdapter(adapter);
    }
    //прорисовка
    void reFillContentPanel(int mode, ArrayList<Item> _items) {
        adapter.setSelectMode(isSelectMode);
        switch (mode) {
            case RECYCLER_ADAPTER_EVENT_CHANGE:
                for (Item item :
                        _items) {
                    adapter.onItemChanged(item);
                }
                break;
            case RECYCLER_ADAPTER_EVENT_MOVE:
                ArrayList<Item> oldItems = itemsService.getCurrentItemsSet();
                itemsService.setInitialData();
                int oldPosition;
                int newPosition;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    for (Item newItem :
                            itemsService.getCurrentItemsSet()) {
                        Item oldItem = oldItems.stream().filter(item -> item.Id == newItem.Id).findFirst().get();
                        if (oldItem != null) {
                            oldPosition = oldItems.indexOf(oldItem);
                            adapter.onMovedItemChanged(newItem, oldPosition);
                        }
                    }
                    for (Item newItem :
                            itemsService.getCurrentItemsSet()) {
                        Item oldItem = oldItems.stream().filter(item -> item.Id == newItem.Id).findFirst().get();
                        if (oldItem != null) {
                            oldPosition = oldItems.indexOf(oldItem);
                            newPosition = itemsService.getCurrentItemsSet().indexOf(newItem);
                            adapter.onItemMoved(oldPosition, newPosition);
                        }
                    }
                }
                break;
            case (RECYCLER_ADAPTER_EVENT_ITEMS_CHANGE):
                adapter.onItemSetChange(_items);
                break;
        }
    }
    //сортировка
    public void sortCancelClick(View view) {
        itemsService.isCardsAvailable = itemsService.isDocsAvailable = itemsService.isFoldersAvailable = itemsService.isImagesAvailable = true;
        findViewById(R.id.flow_sort_card_btn).setAlpha(1f);
        findViewById(R.id.flow_sort_doc_btn).setAlpha(1f);
        findViewById(R.id.flow_sort_image_btn).setAlpha(1f);
        findViewById(R.id.flow_sort_folder_btn).setAlpha(1f);
        reFillContentPanel(RECYCLER_ADAPTER_EVENT_CHANGE, itemsService.getCurrentItemsSet());
    }

    public void bottomPinClick(View view) {
        for (Item x : itemsService.getCurrentItemsSet()) {
            if (x.isSelected == 1) {
                x.Priority = (x.Priority == 1) ? 0 : 1;
                x.isSelected = 0;
                selectedItemsNum--;
                db.updateItem(x.Id, x);
            }
        }
        reFillContentPanel(RECYCLER_ADAPTER_EVENT_MOVE, itemsService.getCurrentItemsSet());
        ((TextView) findViewById(R.id.top_select_picked_txt)).setText(getString(R.string.selected_string) + " " + selectedItemsNum);
    }

    public void bottomHideClick(View view) {
        for (Item x :
                itemsService.getCurrentItemsSet()) {
            if (x.isSelected == 1) {
                x.isHiden = 1;
                x.isSelected = 0;
                selectedItemsNum--;
                db.updateItem(x.Id, x);
            }
        }
        itemsService.setInitialData();
        reFillContentPanel(RECYCLER_ADAPTER_EVENT_CHANGE, itemsService.getCurrentItemsSet());
        ((TextView) findViewById(R.id.top_select_picked_txt)).setText(getString(R.string.selected_string) + " " + selectedItemsNum);
    }
    public void bottomDeleteClick(View view) {
        for (Item x : itemsService.getCurrentItemsSet()) {
            if (x.isSelected == 1) {
                db.deleteItem(x.Id);
                try {
                    File dir = Environment.getExternalStoragePublicDirectory("Pictures/" + APPLICATION_NAME + "/Item" + x.Id);
                    if (dir.isDirectory()) {
                        String[] children = dir.list();
                        for (int i = 0; i < children.length; i++) {
                            new File(dir, children[i]).delete();
                        }
                    }
                    dir.delete();
                } catch (Exception e) {
                }
                switch (x.Type){
                    case "Папка":
                        deleteFolderContent(x.Id);
                        return;
                    case "Пасспорт":
                        db.deletePassport(x.Id);
                        return;
                    case "Изображение":
                        db.deletePhoto(x.Id);
                        return;
                }
                selectedItemsNum--;
            }
        }
        for (Item delItem :
                selectedItemsSet) {
            adapter.onItemDelete(delItem);
        }
        itemsService.setInitialData();
        ((TextView) findViewById(R.id.top_select_picked_txt)).setText(getString(R.string.selected_string) + " " + selectedItemsNum);
    }
    private void deleteFolderContent(int id) {
        Cursor cur = db.getItemsByFolder(id);
        ArrayList<Item> folderItems = new ArrayList<>();
        Item item;
        while (cur.moveToNext()) {
            item = new Item(cur.getInt(0),
                    cur.getString(1),
                    cur.getString(2),
                    cur.getString(3),
                    cur.getInt(4),
                    cur.getInt(5),
                    cur.getInt(6),
                    cur.getString(7),
                    cur.getInt(8),
                    cur.getInt(8));
            folderItems.add(item);
        }
        for (Item fitem :
                folderItems) {
            db.deleteItem(fitem.Id);
        }
    }

    public void topSelectAllClick(View view) {
        selectedItemsNum = 0;
        for (Item x :
                itemsService.getCurrentItemsSet()) {
            int newItemId = itemsService.getCurrentItemsSet().indexOf(x);
            x.isSelected = 1;
            itemsService.setItem(newItemId, x);
            selectedItemsNum++;
        }
        ((TextView) findViewById(R.id.top_select_picked_txt)).setText(getString(R.string.selected_string) + " " + selectedItemsNum);

        reFillContentPanel(RECYCLER_ADAPTER_EVENT_CHANGE, itemsService.getCurrentItemsSet());
    }

    public void topSelectBackClick(View view) {
        MotionLayout ml = findViewById(R.id.motion_layout);
        ml.setTransition(R.id.transGoSelect);
        ml.transitionToStart();
        isSelectMode = false;
        itemsService.setSelectedPropertyZero();
        itemsService.setInitialData();
        reFillContentPanel(RECYCLER_ADAPTER_EVENT_CHANGE, itemsService.getCurrentItemsSet());
    }

    @Override
    public void onBackPressed() {
        itemsService.setInitialData();
        itemsService.setSelectedPropertyZero();
        if (isTitleClicked) {
            isTitleClicked = false;
            itemsService.setCurrentItem(null);
            reFillContentPanel(RECYCLER_ADAPTER_EVENT_CHANGE, itemsService.getCurrentItemsSet());
            return;
        }
        if (isSelectMode) {
            topSelectBackClick(new View(this));
        } else if (isSortMode) {
            openSortMenuClick(findViewById(R.id.menubar_options));
        } else
            NavUtils.navigateUpFromSameTask(this);
    }

    @Override
    public boolean getIsTitleClicked() {
        return isTitleClicked;
    }

    @Override
    public void setIsTitleClicked(boolean value) {
        this.isTitleClicked = value;
    }

}