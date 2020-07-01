package org.mad.transit.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.SuperBuilder;

import static org.mad.transit.util.Constants.MILLISECONDS_IN_MINUTE;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class NavigationStop extends Stop {

    private static final DateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.forLanguageTag("sr-RS"));
    private static final long serialVersionUID = -1540331636976984635L;
    private boolean passed;
    private String arriveTime;

    public NavigationStop(Stop stop, boolean passed) {
        this.title = stop.title;
        this.location = stop.location;
        this.passed = passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    @SneakyThrows
    public void setArriveTime(String startTime, int minutes) {
        Date arriveTime = dateFormat.parse(startTime);
        if (arriveTime != null) {
            arriveTime.setTime(arriveTime.getTime() + minutes * MILLISECONDS_IN_MINUTE);
            this.arriveTime = dateFormat.format(arriveTime);
        }
    }
}