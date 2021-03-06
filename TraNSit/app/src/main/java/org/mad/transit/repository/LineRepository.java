package org.mad.transit.repository;

import android.content.ContentResolver;
import android.database.Cursor;
import android.util.Log;

import org.mad.transit.database.DBContentProvider;
import org.mad.transit.model.Line;
import org.mad.transit.model.LineDirection;
import org.mad.transit.model.LineOneDirection;
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

    public Line findById(Long id) {
        Line line = null;
        Cursor cursor = this.contentResolver.query(DBContentProvider.CONTENT_URI_LINE,
                null,
                Constants.ID + " = ?",
                new String[]{id.toString()},
                null);

        if (cursor != null) {
            cursor.moveToFirst();
            String title = cursor.getString(cursor.getColumnIndex(Constants.NAME));
            String number = cursor.getString(cursor.getColumnIndex(Constants.NUMBER));
            String type = cursor.getString(cursor.getColumnIndex(Constants.TYPE));
            line = Line.builder()
                    .id(id)
                    .number(number)
                    .title(title)
                    .type(LineType.valueOf(type))
                    .build();
            cursor.close();
        } else {
            Log.e("Retrieve lines", "Cursor is null");
        }
        return line;
    }

    public List<Line> findAllByStopId(Long stopId) {
        List<Line> lines = new ArrayList<>();
        Cursor cursor = this.contentResolver.query(DBContentProvider.CONTENT_URI_LINE_STOPS,
                new String[]{Constants.LINE, Constants.DIRECTION},
                Constants.STOP + " = ?",
                new String[]{stopId.toString()},
                null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                Long lineId = cursor.getLong(cursor.getColumnIndex(Constants.LINE));
                String direction = cursor.getString(cursor.getColumnIndex(Constants.DIRECTION));
                Line line = this.findById(lineId);
                LineDirection lineDirection = LineDirection.valueOf(direction);
                LineOneDirection lineOneDirection = LineOneDirection.builder().lineDirection(lineDirection).build();
                if (lineDirection == LineDirection.A) {
                    line.setLineDirectionA(lineOneDirection);
                } else {
                    line.setLineDirectionB(lineOneDirection);
                }
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
