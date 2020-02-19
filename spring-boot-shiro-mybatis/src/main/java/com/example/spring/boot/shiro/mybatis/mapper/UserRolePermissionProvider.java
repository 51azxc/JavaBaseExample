package com.example.spring.boot.shiro.mybatis.mapper;

import org.apache.ibatis.jdbc.SQL;

import java.text.MessageFormat;
import java.util.List;

public class UserRolePermissionProvider {
    public String insertUserRoles(Long uid, List<Long> rids) {
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO user_role (user_id, role_id) VALUES ");
        MessageFormat mf = new MessageFormat("(#'{'uid}, #'{'rids[{0}]})");
        for (int i = 0, len = rids.size(); i < len; i++) {
            sb.append(mf.format(new Object[]{i}));
            if (i < len - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    public String findUserRolesByUserId(Long uid) {
        SQL sql = new SQL();
        sql.SELECT("r.id", "r.role_name");
        sql.FROM("role r");
        sql.LEFT_OUTER_JOIN("user_role ur on r.id = ur.role_id");
        sql.WHERE("ur.user_id = #{uid}");
        return sql.toString();
    }

    public String findPermissionsByRoleId(Long roleId) {
        SQL sql = new SQL();
        sql.SELECT("p.id", "p.name", "p.permission");
        sql.FROM("permission p");
        sql.LEFT_OUTER_JOIN("role_permission rp on p.id = rp.permission_id");
        sql.WHERE("rp.role_id = #{roleId}");
        return sql.toString();
    }
}
