/*
 * Copyright Erik Costlow.
 * Not authorized for use or view by others.
 */
package com.costlowcorp.fx.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Erik Costlow
 */
public class DateApproximator {

    private DateApproximator() {
    }

    public static String between(Date a, Date b) {
        final LocalDateTime ldA = LocalDateTime.ofInstant(a.toInstant(), ZoneId.systemDefault());
        final LocalDateTime ldB = LocalDateTime.ofInstant(b.toInstant(), ZoneId.systemDefault());

        if (a.compareTo(b) > 0) {
            return internalBetween(ldB, ldA);
        } else {
            return internalBetween(ldA, ldB);
        }
    }

    public static String between(LocalDateTime a, LocalDateTime b) {
        return a.compareTo(b) > 0 ? internalBetween(b, a) : internalBetween(a, b);
    }

    private static String internalBetween(LocalDateTime smaller, LocalDateTime larger) {
        long years = 0, months = 0, weeks = 0;
        LocalDateTime tempDateTime = LocalDateTime.from(smaller);
        years = smaller.until(larger, ChronoUnit.YEARS);
        tempDateTime = tempDateTime.plusYears(years);

        months = tempDateTime.until(larger, ChronoUnit.MONTHS);
        if (months > 11) {
            years++;
            months=0;
        } else {
            tempDateTime = tempDateTime.plusMonths(months);

            weeks = tempDateTime.until(larger, ChronoUnit.WEEKS);
            if(weeks>2){
                months++;
                weeks=0;
            }else{
                //tempDateTime = tempDateTime.plusDays(days);
            }
        }
        final List<String> terms = new ArrayList<>();
        if(years==1){
            terms.add(years + " year");
        }else if(years>1){
            terms.add(years + " years");
        }
        
        if(months==1){
            terms.add(months + " month");
        }else if(months>1){
            terms.add(months + " months");
        }
        
        if(weeks==1){
            terms.add(weeks + " week");
        }else if(weeks>1){
            terms.add(weeks + " weeks");
        }
        
        final StringBuilder sb=new StringBuilder();
        
        if(terms.size()==1){
            sb.append(terms.get(0));
        }else{
            for(int i=0; i<terms.size()-1; i++){
                sb.append(terms.get(i));
                sb.append(", ");
            }
            sb.append("and ");
            sb.append(terms.get(terms.size()-1));
        }
        return sb.toString();
    }
}
