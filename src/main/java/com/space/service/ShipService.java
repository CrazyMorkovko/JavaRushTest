package com.space.service;

import com.space.model.Ship;
import org.springframework.stereotype.Service;
import java.util.Calendar;

@Service
public class ShipService {
    public Double getRating(Ship ship) {
        double coef = ship.getUsed() ? 0.5 : 1;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(ship.getProdDate());

        return Math.round((80 * ship.getSpeed() * coef) / (3019 - calendar.get(Calendar.YEAR) + 1) * 100) / 100.0;
    }
}
