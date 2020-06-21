package org.mad.transit.repository;

import android.content.ContentResolver;
import android.database.Cursor;

import org.mad.transit.database.DBContentProvider;
import org.mad.transit.model.PriceList;
import org.mad.transit.util.Constants;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class PriceListRepository {

    private final ContentResolver contentResolver;

    @Inject
    public PriceListRepository(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
    }

    public List<PriceList> findAll() {
        List<PriceList> prices = new ArrayList<>();
        Cursor cursor = this.contentResolver.query(DBContentProvider.CONTENT_URI_PRICE_LIST,
                null,
                null,
                null,
                null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndex(Constants.ID));
                long startZoneId = cursor.getLong(cursor.getColumnIndex(Constants.START_ZONE));
                long endZoneId = cursor.getLong(cursor.getColumnIndex(Constants.END_ZONE));
                int price = cursor.getInt(cursor.getColumnIndex(Constants.PRICE));

                prices.add(PriceList.builder()
                        .id(id)
                        .startZoneId(startZoneId)
                        .endZoneId(endZoneId)
                        .price(price)
                        .build());
            }
            cursor.close();
        }

        return prices;
    }
}
