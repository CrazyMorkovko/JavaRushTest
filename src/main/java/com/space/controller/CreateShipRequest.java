package com.space.controller;

import com.space.model.ShipType;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.Calendar;

public class CreateShipRequest {
    @Enumerated(EnumType.STRING)
    private ShipType shipType;
    private Integer crewSize;
    private Double speed;
    private String name;
    private String planet;
    private Boolean isUsed;
    private Long prodDate;

    public CreateShipRequest() {

    }

    public Integer getCrewSize() {
        return crewSize;
    }

    public void setCrewSize(Integer crewSize) {
        this.crewSize = crewSize;
    }

    public Double getSpeed() {
        return speed;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPlanet() {
        return planet;
    }

    public void setPlanet(String planet) {
        this.planet = planet;
    }

    public Boolean getUsed() {
        return isUsed;
    }

    public void setUsed(Boolean used) {
        isUsed = used;
    }

    public Long getProdDate() {
        return prodDate;
    }

    public void setProdDate(Long prodDate) {
        this.prodDate = prodDate;
    }

    public ShipType getShipType() {
        return shipType;
    }

    public void setShipType(ShipType shipType) {
        this.shipType = shipType;
    }

    public boolean isNameValid() {
        return getName().length() > 0 && getName().length() <= 50;
    }

    public boolean isPlanetValid() {
        return getPlanet().length() > 0 && getPlanet().length() <= 50;
    }

    public boolean isSpeedValid() {
        return getSpeed() >= 0.01 && getSpeed() <= 0.99;
    }

    public boolean isCrewValid() {
        return getCrewSize() >= 1 && getCrewSize() <= 9999;
    }

    public boolean isDateValid() {
        if (getProdDate() < 0) {
            return false;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(getProdDate());

        return calendar.get(Calendar.YEAR) >= 2800 && calendar.get(Calendar.YEAR) <= 3019;
    }
    
    public boolean isValid() {
        if (getName() == null || !isNameValid()) {
            return false;
        }

        if (getPlanet() == null || !isPlanetValid()) {
            return false;
        }

        if (getSpeed() == null || !isSpeedValid()) {
            return false;
        }

        if (getCrewSize() == null || !isCrewValid()) {
            return false;
        }

        return getProdDate() != null && isDateValid();
    }
}
