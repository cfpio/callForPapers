/*
 * Copyright (c) 2016 BreizhCamp
 * [http://breizhcamp.org]
 *
 * This file is part of CFP.io.
 *
 * CFP.io is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package io.cfp.model;

import io.cfp.dto.RateAdmin;
import io.cfp.entity.Event;
import io.cfp.entity.Talk;
import io.cfp.entity.User;

import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Date;

public class Rate {

    private int id;
    private int rate;
    private Date added;
    private boolean love;
    private boolean hate;

    private Talk talk;
    private User adminUser;
    private Event event;

    public int getId() {
        return id;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    @Max(5)
    @Min(0)
    @NotNull
    public int getRate() {
        return rate;
    }

    @NotNull
    public Date getAdded() {
        return added;
    }

    public boolean isLove() {
        return love;
    }

    public boolean isHate() {
        return hate;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposal")
    public Talk getTalk() {
        return talk;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin")
    public User getAdminUser() {
        return adminUser;
    }


    public void setId(int id) {
        this.id = id;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    public void setAdded(Date added) {
        this.added = added;
    }

    public void setLove(boolean love) {
        this.love = love;
    }

    public void setHate(boolean hate) {
        this.hate = hate;
    }

    public void setTalk(Talk talk) {
        this.talk = talk;
    }

    public void setAdminUser(User adminUser) {
        this.adminUser = adminUser;
    }

    public RateAdmin toRateAdmin() {
        RateAdmin result = new RateAdmin();
        result.setId(this.getId());
        result.setAdded(this.getAdded());
        result.setHate(this.isHate());
        result.setLove(this.isLove());
        result.setRate(this.getRate());
        result.setTalkId(this.getTalk().getId());
        result.setUser(this.getAdminUser().toAdminUserDTO());
        return result;
    }
}
