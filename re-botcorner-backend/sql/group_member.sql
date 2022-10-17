create table rebc.b_group_member
(
    group_id int unsigned null,
    user_id  int unsigned null,
    constraint b_group_member_b_group_id_fk
        foreign key (group_id) references rebc.b_group (id),
    constraint b_group_member_user_id_fk
        foreign key (user_id) references rebc.user (id)
)
    comment '成员表';