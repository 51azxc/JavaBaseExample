CREATE TABLE user (
    id int NOT NULL PRIMARY KEY AUTO_INCREMENT,
    username varchar(100) NOT NULL UNIQUE,
    password varchar(100) NOT NULL
);
CREATE TABLE role (
    id int NOT NULL PRIMARY KEY AUTO_INCREMENT,
    role_type varchar(50) NOT NULL UNIQUE
);
CREATE TABLE user_roles (
    user_id int NOT NULL,
    role_id int NOT NULL,
    PRIMARY KEY(user_id, role_id)
);