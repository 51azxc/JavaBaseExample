INSERT INTO
    `role` (`id`, `role_type`)
VALUES
    (1, 'ROLE_ADMIN'),
    (2, 'ROLE_USER');

INSERT INTO
    `permission` (`id`, `parent_id`, `name`, `type`, `permission`, `url`)
VALUE
    (1, 0, 'user management', 'GET', 'user:search', '/users'),
    (2, 1, 'add user', 'POST', 'user:add', '/users'),
    (3, 1, 'edit user', 'PUT', 'user:edit', '/users'),
    (4, 1, 'delete user', 'DELETE', 'user:delete', '/users');

INSERT INTO
    `role_permission` (`role_id`, `permission_id`)
VALUES
    (1, 1),
    (1, 2),
    (1, 3),
    (1, 4),
    (2, 1);