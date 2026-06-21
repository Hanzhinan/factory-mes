package com.wangziyang.mes.basedata.request;

import com.wangziyang.mes.common.BasePageReq;

public class SpTeamPageReq extends BasePageReq {

    private String teamCode;
    private String teamName;

    public String getTeamCode() {
        return teamCode;
    }

    public void setTeamCode(String teamCode) {
        this.teamCode = teamCode;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }
}
