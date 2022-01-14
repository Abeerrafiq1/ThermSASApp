package com.example.thermsasapp;
import android.widget.TextView;
import android.graphics.Typeface;
import java.util.List;
import android.view.View;
import android.view.ViewGroup;
import android.view.Gravity;
import android.content.Context;
import android.text.TextUtils;
import de.codecrafters.tableview.TableDataAdapter;

public final class SSimpleTableDataAdapter extends TableDataAdapter<String[]> {

    private int textSize = 18;

    public SSimpleTableDataAdapter(final Context context, final List<String[]> data) {
        super(context, data);
    }

    @Override
    public View getCellView(final int rowIndex, final int columnIndex, final ViewGroup parentView) {
        TextView textView = new TextView(getContext());
        String textToShow = getItem(rowIndex)[columnIndex];
        textView.setText(textToShow);
        textView.setGravity(Gravity.START);
        textView.setSingleLine();
        textView.setTypeface(textView.getTypeface(), Typeface.NORMAL);
        textView.setPadding(20, 15, 20, 15);
        textView.setEllipsize(TextUtils.TruncateAt.END);
        textView.setTextSize(textSize);
        textView.setTextColor(0x90000000);

        return textView;
    }
    public void setTextSize(int textSize) {
        this.textSize = textSize;
    }

}