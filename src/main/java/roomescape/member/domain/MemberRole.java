package roomescape.member.domain;

import java.util.Objects;

public enum MemberRole {
    GUEST,
    ADMIN;

    public static MemberRole createByName(String name) {
        if (Objects.equals(name, ADMIN.name())) {
            return ADMIN;
        }
        return GUEST;
    }
}
