package rey.rssreader;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class RecyclerViewItemDecoration extends RecyclerView.ItemDecoration
{
    private int offset_top;
    private int offset_bottom;
    private int offset_left;
    private int offset_right;

    RecyclerViewItemDecoration(int offset_top, int offset_bottom, int offset_left, int offset_right)
    {
        this.offset_top = offset_top;
        this.offset_bottom = offset_bottom;
        this.offset_left = offset_left;
        this.offset_right = offset_right;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state)
    {
        outRect.top = offset_top;
        outRect.bottom = offset_bottom;
        outRect.left = offset_left;
        outRect.right = offset_right;
    }
}
