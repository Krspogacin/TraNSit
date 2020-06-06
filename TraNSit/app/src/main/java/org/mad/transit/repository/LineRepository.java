package org.mad.transit.repository;

import android.content.ContentResolver;
import android.database.Cursor;
import android.util.Log;

import org.mad.transit.database.DBContentProvider;
import org.mad.transit.model.Line;
import org.mad.transit.model.LineDirection;
import org.mad.transit.model.LineType;
import org.mad.transit.util.Constants;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class LineRepository {

    private final ContentResolver contentResolver;

    @Inject
    public LineRepository(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
    }

    public List<Line> findAll() {
        List<Line> lines = new ArrayList<>();
        Cursor cursor = this.contentResolver.query(DBContentProvider.CONTENT_URI_LINE,
                null,
                null,
                null,
                null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndex(Constants.ID));
                String title = cursor.getString(cursor.getColumnIndex(Constants.NAME));
                String number = cursor.getString(cursor.getColumnIndex(Constants.NUMBER));
                String type = cursor.getString(cursor.getColumnIndex(Constants.TYPE));
                Line line = Line.builder()
                        .id(id)
                        .number(number)
                        .title(title)
                        .type(LineType.valueOf(type))
                        .build();

                lines.add(line);
            }
            cursor.close();
        } else {
            Log.e("Retrieve lines", "Cursor is null");
        }
        return lines;
    }

    public boolean doesDirectionBExists(Long lineId) {
        Cursor cursor = this.contentResolver.query(DBContentProvider.CONTENT_URI_LINE_STOPS,
                null,
                Constants.LINE + " = ? and " + Constants.DIRECTION + " = ?",
                new String[]{lineId.toString(), LineDirection.B.toString()},
                null);
        if (cursor != null) {
            int count = cursor.getCount();
            cursor.close();
            if (count > 0) {
                return true;
            } else {
                cursor.close();
                return false;
            }
        } else {
            Log.e("Check if B exist", "Cursor is null");
            return false;
        }
    }
}
