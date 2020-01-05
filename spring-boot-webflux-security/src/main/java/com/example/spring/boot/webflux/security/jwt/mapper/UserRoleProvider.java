package com.example.spring.boot.webflux.security.jwt.mapper;

import com.example.spring.boot.webflux.security.jwt.entity.SystemRole;
import org.apache.ibatis.jdbc.SQL;

import java.text.MessageFormat;
import java.util.List;

public class UserRoleProvider {
    public String insertRoles(List<SystemRole> roles) {
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO role (role_type) VALUES ");
        MessageFormat mf = new MessageFormat("(#'{'roles[{0}].roleType})");
        for (int i = 0, len = roles.size(); i < len; i++) {
            sb.append(mf.format(new Object[]{i}));
            if (i < len - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    public String insertUserRoles(Long uid, List<Long> rids) {
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO user_roles (user_id, role_id) VALUES ");
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
        sql.SELECT("r.id", "r.role_type");
        sql.FROM("role r");
        sql.LEFT_OUTER_JOIN("user_roles ur on r.id = ur.role_id");
        sql.WHERE("ur.user_id = #{uid}");
        return sql.toString();
    }
}
