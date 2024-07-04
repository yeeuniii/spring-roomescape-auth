package roomescape.member.domain;

public enum MemberRole {
    GUEST("게스트"),
    ADMIN("관리자");

    private final String name;

    MemberRole(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static MemberRole findByName(String name) {
        if (name.equals(ADMIN.name)) {
            return ADMIN;
        }
        return GUEST;
    }
}
