package com.example.mydocsapp.apputils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mydocsapp.models.ItemAdapter;

public class ItemMoveCallback extends ItemTouchHelper.Callback {

    private final ItemTouchHelperContract mAdapter;

    public ItemMoveCallback(ItemTouchHelperContract adapter) {
        mAdapter = adapter;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return false;
    }



    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT| ItemTouchHelper.RIGHT;
        return makeMovementFlags(dragFlags, 0);
    }
    int finalFromPosition = -1;
    int finalToPosition = -1;
    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                          RecyclerView.ViewHolder target) {
        finalFromPosition = viewHolder.getAdapterPosition();
        finalToPosition = target.getAdapterPosition();
        mAdapter.onRowMoved(recyclerView, viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder,
                                  int actionState) {
        super.onSelectedChanged(viewHolder, actionState);
         if(actionState != ItemTouchHelper.ACTION_STATE_IDLE){
                if (viewHolder instanceof ItemAdapter.ViewHolder) {
                    ItemAdapter.ViewHolder myViewHolder=
                            (ItemAdapter.ViewHolder) viewHolder;
                    mAdapter.onRowSelected(myViewHolder);
            }
        }

    }
    @Override
    public void clearView(RecyclerView recyclerView,
                          RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);

        if (viewHolder instanceof ItemAdapter.ViewHolder) {
            ItemAdapter.ViewHolder myViewHolder=
                    (ItemAdapter.ViewHolder) viewHolder;
            mAdapter.onRowClear(myViewHolder,finalFromPosition, finalToPosition);
        }
    }

    public interface ItemTouchHelperContract {
        void onRowMoved(RecyclerView recyclerView, int fromPosition, int toPosition);
        void onRowSelected(ItemAdapter.ViewHolder myViewHolder);
        void onRowClear(ItemAdapter.ViewHolder myViewHolder, int fromPosition, int toPosition);

    }

}