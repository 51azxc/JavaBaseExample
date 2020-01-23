CREATE TABLE user (
    id int NOT NULL PRIMARY KEY AUTO_INCREMENT,
    username varchar(100) NOT NULL UNIQUE,
    password varchar(100) NOT NULL
);
CREATE TABLE role (
    id int NOT NULL PRIMARY KEY AUTO_INCREMENT,
    role_name varchar(50) NOT NULL UNIQUE
);
CREATE TABLE user_role (
    user_id int NOT NULL,
    role_id int NOT NULL,
    PRIMARY KEY(user_id, role_id)
);
CREATE TABLE permission (
    id int NOT NULL PRIMARY KEY AUTO_INCREMENT,
    parent_id int NOT NULL DEFAULT 0,
    name varchar(50) NOT NULL DEFAULT '',
    type varchar(50) NOT NULL DEFAULT '',
    permission varchar(100) NOT NULL DEFAULT '',
    url varchar(200) NOT NULL DEFAULT ''
);
CREATE TABLE role_permission (
    role_id int NOT NULL,
    permission_id int NOT NULL,
    PRIMARY KEY(role_id, permission_id)
);