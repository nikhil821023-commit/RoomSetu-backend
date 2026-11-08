package com.roomsetu.dto;

import com.roomsetu.entity.PartnerOrganization;
import lombok.Data;

@Data
public class PartnerOrganizationResponse {
    private Long id;
    private String name;
    private String type;
    private String signupCode;
    private String locality;
    private long memberCount;

    public static PartnerOrganizationResponse from(PartnerOrganization p, long memberCount) {
        PartnerOrganizationResponse r = new PartnerOrganizationResponse();
        r.setId(p.getId());
        r.setName(p.getName());
        r.setType(p.getType().name());
        r.setSignupCode(p.getSignupCode());
        r.setLocality(p.getLocality());
        r.setMemberCount(memberCount);
        return r;
    }
}