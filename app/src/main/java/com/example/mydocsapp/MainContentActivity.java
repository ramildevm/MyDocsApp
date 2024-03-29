package com.example.mydocsapp;

import static com.example.mydocsapp.services.AppService.NULL_UUID;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mydocsapp.models.User;
import com.example.mydocsapp.apputils.RecyclerItemClickListener;
import com.example.mydocsapp.interfaces.IItemAdapterActivity;
import com.example.mydocsapp.models.Item;
import com.example.mydocsapp.models.Template;
import com.example.mydocsapp.models.TemplateDocument;
import com.example.mydocsapp.services.AppService;
import com.example.mydocsapp.services.CurrentItemsService;
import com.example.mydocsapp.services.DBHelper;
import com.example.mydocsapp.services.ItemAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainContentActivity extends AppCompatActivity implements IItemAdapterActivity {
    private static final int DB_IMAGE = 1;
    private static final int SESSION_MODE_CREATE = 1;
    private static final int SESSION_MODE_OPEN = 2;
    RecyclerView recyclerView;
    DBHelper db;
    boolean isSelectMode;
    boolean isSortMode = false;
    private int selectedItemsNum = 0;
    private ActivityResultLauncher<Intent> registerForARFolder;
    private RecyclerView recyclerFolderView;
    Dialog makeRenameDialog;
    ItemAdapter adapter;
    CurrentItemsService itemsService;
    private User CurrentUser;
    private ArrayList<Item> CurrentFolderItemsSet;
    private ArrayList<Item> selectedItemsSet = new ArrayList<>();
    private boolean isTitleClicked = false;
    public static final int RECYCLER_ADAPTER_EVENT_CHANGE = 1;
    public static final int RECYCLER_ADAPTER_EVENT_MOVE = 2;
    private static final int RECYCLER_ADAPTER_EVENT_ITEMS_CHANGE = 3;
    public static final String APPLICATION_NAME = "MyDocs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_content);
        registerForARFolder = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            CurrentFolderItemsSet = itemsService.getFolderItemsFromDb(itemsService.getCurrentItem().Id);
            reFillContentPanel(recyclerFolderView, CurrentFolderItemsSet);
            itemsService.setInitialData();
            reFillContentPanel(recyclerView, itemsService.getCurrentItemsSet());
        });
        db = new DBHelper(this, AppService.getUserId(this));
        setCurrentUserFromDB();
        isSelectMode = false;
        itemsService = new CurrentItemsService(db, AppService.isHideMode());
        itemsService.setInitialData();
        itemsService.setSelectedPropertyZero();
        CurrentFolderItemsSet = null;
        isTitleClicked = false;
        setViewsTagOff();
        recyclerView = findViewById(R.id.container);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new ItemAdapter(this, itemsService.getCurrentItemsSet(), isSelectMode);
        recyclerView.setAdapter(adapter);
        int spanCount = 2; // 2 columns
        int spacing = 20; // 10px
        boolean includeEdge = true;
        recyclerView.addItemDecoration(new com.example.mydocsapp.apputils.GridSpacingItemDecoration(spanCount, spacing, includeEdge));
        setOnClickListeners();
        setWindowParams();
    }

    private void setWindowParams() {
        TextView topTxt = findViewById(R.id.login_txt);
        if (AppService.isHideMode()) {
            topTxt.setText(R.string.hidden_files);
            ((TextView) findViewById(R.id.bottom_hide_return_txt)).setText(R.string.return_string);
            ((ImageView) findViewById(R.id.bottom_hide_return_btn)).setImageResource(R.drawable.return_btn);
            ImageView BtnMaimMenu = ((ImageView) findViewById(R.id.menubar_main_list));
            BtnMaimMenu.setImageResource(R.drawable.left_arrow_white);
            BtnMaimMenu.setScaleX(1.3f);
            BtnMaimMenu.setScaleY(1.3f);
            ((Button) findViewById(R.id.flow_button)).setBackgroundResource(R.drawable.ic_create_new_folder);
            findViewById(R.id.flow_button).setOnClickListener(v -> {
                makeRenameDialogMethod(itemsService.getCurrentItemsSet(), "Make");
                makeRenameDialog.show();
            });
        } else {
            topTxt.setText((CurrentUser.Login));
        }
    }

    private void setCurrentUserFromDB() {
        CurrentUser = db.getUserById(AppService.getUserId(this));
        CurrentUser.Id = AppService.getUserId(this);
        if (CurrentUser.Id == 0)
            CurrentUser.Login = getString(R.string.guest_mode);
    }

    private void setCurrentUserFromAPI() {
        CurrentUser = db.getUserById(1);
        CurrentUser.Id = 0;
    }

    @Override
    protected void onResume() {
        itemsService.setInitialData();
        reFillContentPanel(RECYCLER_ADAPTER_EVENT_ITEMS_CHANGE, itemsService.getCurrentItemsSet());
        super.onResume();
    }

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
                            int unpinCount = 0;
                            for (Item i :
                                    selectedItemsSet) {
                                if (i.Priority == 1)
                                    unpinCount++;
                            }
                            ((TextView) findViewById(R.id.bottom_pin_txt)).setText(unpinCount == selectedItemsSet.size() & unpinCount != 0 ? R.string.unpin_string : R.string.pin_string);
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
                                if (item.Type.equals("Folder")) {
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
                                    CurrentFolderItemsSet = itemsService.getFolderItemsFromDb(item.Id);
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
                                                    } else if (itemsService.getCurrentItem().Type.equals("Passport")) {
                                                        goPassportItemClick(itemsService.getCurrentItem(), "Passport");
                                                    }else if (itemsService.getCurrentItem().Type.equals("SNILS")) {
                                                        goPassportItemClick(itemsService.getCurrentItem(), "SNILS");
                                                    }else if (itemsService.getCurrentItem().Type.equals("INN")) {
                                                        goPassportItemClick(itemsService.getCurrentItem(), "INN");
                                                    }else if (itemsService.getCurrentItem().Type.equals("Polis")) {
                                                        goPassportItemClick(itemsService.getCurrentItem(), "Polis");
                                                    } else if (itemsService.getCurrentItem().Type.equals("CreditCard")) {
                                                        goCreditCardItemClick(itemsService.getCurrentItem());
                                                    } else if (itemsService.getCurrentItem().Type.equals("Collection")) {
                                                        goImageItemClick(itemsService.getCurrentItem());
                                                    } else if (itemsService.getCurrentItem().Type.equals("Template")) {
                                                        goTemplateItemClick(itemsService.getCurrentItem());
                                                    }
                                                }

                                                @Override
                                                public void onLongItemClick(View view, int position) {
                                                }
                                            }));
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
                                } else if (item.Type.equals("Passport"))
                                    goPassportItemClick(item, "Passport");
                                else if (item.Type.equals("SNILS"))
                                    goPassportItemClick(item, "SNILS");
                                else if (item.Type.equals("INN"))
                                    goPassportItemClick(item, "INN");
                                else if (item.Type.equals("Polis"))
                                    goPassportItemClick(item, "Polis");
                                else if (item.Type.equals("CreditCard"))
                                    goCreditCardItemClick(item);
                                else if (item.Type.equals("Collection"))
                                    goImageItemClick(item);
                                else if (item.Type.equals("Template"))
                                    goTemplateItemClick(item);
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
                            if (item.Priority == 1)
                                ((TextView) findViewById(R.id.bottom_pin_txt)).setText(R.string.unpin_string);
                            else
                                ((TextView) findViewById(R.id.bottom_pin_txt)).setText(R.string.pin_string);
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

    private void goCreditCardItemClick(Item item) {
        Intent intent = new Intent(MainContentActivity.this, CardPatternActivity.class);
        intent.putExtra("item", item);
        startActivity(intent);
        overridePendingTransition(R.anim.alpha_in, R.anim.alpha_out);
    }

    private void goTemplateItemClick(Item item) {
        Intent intent = new Intent(MainContentActivity.this, TemplateActivity.class);
        TemplateDocument templateDocument = db.getTemplateDocumentById(item.Id);
        Template template = db.getTemplateById(templateDocument.TemplateId);
        intent.putExtra("template", template);
        intent.putExtra("document", templateDocument);
        if (template != null) {
            startActivity(intent);
            overridePendingTransition(R.anim.alpha_in, R.anim.alpha_out);
        }
    }

    private void goImageItemClick(Item item) {
        Intent intent = new Intent(MainContentActivity.this, ImageCollectionActivity.class);
        intent.putExtra("userId", CurrentUser.Id);
        intent.putExtra("mode", SESSION_MODE_OPEN);
        intent.putExtra("item", item);
        startActivity(intent);
        overridePendingTransition(R.anim.alpha_in, R.anim.alpha_out);
    }

    private void goPassportItemClick(Item item, String type) {
        Intent intent = new Intent(MainContentActivity.this, MainDocumentPatternActivity.class);
        intent.putExtra("item", item);
        intent.putExtra("type", type);
        startActivity(intent);
        overridePendingTransition(R.anim.alpha_in, R.anim.alpha_out);
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
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.US);
                String time = df.format(new Date());
                db.insertItem(new Item(NULL_UUID, editText.getText().toString(), "Folder", null, 0, AppService.isHideMode() ? 1 : 0, 0, time, NULL_UUID, 0, ""), true);
            } else {
                Item item = itemsService.getCurrentItem();
                item.Title = editText.getText().toString();
                db.updateItem(item.Id, item, false);
            }
            if (mode.equals("Rename") | mode.equals("Make")) {
                itemsService.setInitialData();
                reFillContentPanel(RECYCLER_ADAPTER_EVENT_CHANGE, items);
            } else if (mode.equals("FRename")) {
                CurrentFolderItemsSet = itemsService.getFolderItemsFromDb(itemsService.getCurrentItem().FolderId);
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

    private void setViewsTagOff() {
        if (findViewById(R.id.checkTranitionState).getAlpha() == 0.0) {
            findViewById(R.id.flow_button).setTag("off");
            findViewById(R.id.menubar_options).setTag("off");
            (findViewById(R.id.menubar_options)).setEnabled(true);
            (findViewById(R.id.flow_button)).setEnabled(true);
        }
    }

    public void goAccountClick(View view) {
        startActivity(new Intent(MainContentActivity.this, MainMenuActivity.class));
        overridePendingTransition(R.anim.slide_in_left, R.anim.alpha_out);
    }

    public void goPatternClick(View view) {
        Intent intent = new Intent(MainContentActivity.this, MainDocumentPatternActivity.class);
        itemsService.setCurrentItem(null);
        intent.putExtra("item", itemsService.getCurrentItem());
        intent.putExtra("type", "");
        startActivity(intent);
        overridePendingTransition(R.anim.alpha_in, R.anim.alpha_out);
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
        overridePendingTransition(R.anim.alpha_in, R.anim.alpha_out);
    }

    public void menuMakeImageClick(View view) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(this, ImageCollectionActivity.class);
            intent.putExtra("userId", CurrentUser.Id);
            intent.putExtra("mode", SESSION_MODE_CREATE);
            startActivity(intent);
            overridePendingTransition(R.anim.alpha_in, R.anim.alpha_out);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
        }
    }

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
        _recyclerView.setAdapter(adapter);
    }

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
                adapter.updateItems(itemsService.getCurrentItemsSet());
                break;
            case (RECYCLER_ADAPTER_EVENT_ITEMS_CHANGE):
                adapter.onItemSetChange(_items);
                break;
        }
    }

    public void bottomPinClick(View view) {
        int unpinCount = 0;
        for (Item i :
                selectedItemsSet) {
            if (i.Priority == 1)
                unpinCount++;
        }
        for (Item x : selectedItemsSet) {
            if (x.isSelected == 1) {
                x.Priority = (unpinCount == selectedItemsSet.size() & unpinCount != 0) ? 0 : 1;
                x.isSelected = 0;
                selectedItemsNum--;
                db.updateItem(x.Id, x, false);
            }
        }
        reFillContentPanel(RECYCLER_ADAPTER_EVENT_MOVE, (ArrayList<Item>) db.getItemsByFolder0());
        ((TextView) findViewById(R.id.top_select_picked_txt)).setText(getString(R.string.selected_string) + " " + selectedItemsNum);
        topSelectBackClick(new View(this));
    }

    public void bottomHideClick(View view) {
        for (Item x : selectedItemsSet) {
            if (x.Type.equals("Folder")) {
                List<Item> itemList = itemsService.getFolderItemsFromDb(x.Id);
                for (Item item : itemList) {
                    item.IsHidden = item.IsHidden == 1 ? 0 : 1;
                    db.updateItem(item.Id, item, false);
                }
            }
            if (x.isSelected == 1) {
                x.IsHidden = x.IsHidden == 1 ? 0 : 1;
                x.isSelected = 0;
                selectedItemsNum--;
                db.updateItem(x.Id, x, false);
            }
        }
        for (Item delItem :
                selectedItemsSet) {
            adapter.onItemDelete(delItem);
        }
        ((TextView) findViewById(R.id.top_select_picked_txt)).setText(getString(R.string.selected_string) + " " + selectedItemsNum);
        topSelectBackClick(new View(this));
    }

    public void bottomDeleteClick(View view) {
        for (Item x : itemsService.getCurrentItemsSet()) {
            if (x.isSelected == 1) {
                db.deleteItem(x.Id);
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
        } else {
            startActivity(new Intent(MainContentActivity.this, MainMenuActivity.class));
            overridePendingTransition(R.anim.slide_in_left, R.anim.alpha_out);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        reFillContentPanel(recyclerView, itemsService.getCurrentItemsSet());
    }

    @Override
    public void setIsTitleClicked(boolean value) {
        this.isTitleClicked = value;
    }
}