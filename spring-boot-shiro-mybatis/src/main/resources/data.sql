INSERT INTO
    `user` (`username`, `password`)
VALUES
    ('admin', '21232f297a57a5a743894a0e4a801fc3'),  --password: admin
    ('user', 'ee11cbb19052e40b07aac0ca060c23ee');   --password: user

INSERT INTO
    `role` (`id`, `role_name`)
VALUES
    (1, 'ROLE_ADMIN'),
    (2, 'ROLE_USER');

INSERT INTO
    `user_role` (`user_id`, `role_id`)
VALUES
    (1, 1),
    (1, 2),
    (2, 2);

INSERT INTO
    `permission` (`id`, `name`, `permission`)
VALUES
    (1, 'get user', 'user:search'),
    (2, 'add user', 'user:add'),
    (3, 'edit user', 'user:edit'),
    (4, 'delete user', 'user:delete');

INSERT INTO
    `role_permission` (`role_id`, `permission_id`)
VALUES
    (1, 1),
    (1, 2),
    (1, 3),
    (1, 4),
    (2, 1);